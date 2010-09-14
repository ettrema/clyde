package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.property.PropertyAuthoriser;
import com.bradmcevoy.utils.AuthoringPermissionService;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author brad
 */
public class PermissionsAuthoriser implements ClydeAuthoriser, PropertyAuthoriser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PermissionsAuthoriser.class );
    private final PermissionChecker permissionChecker;
    private final AuthoringPermissionService authoringPermissionService;

    /**
     *
     * @param permissionChecker - used to check if a user has the required permission
     * @param authoringPermissionService - used to determine what permission is required for a certain action
     */
    public PermissionsAuthoriser( PermissionChecker permissionChecker, AuthoringPermissionService authoringPermissionService ) {
        this.permissionChecker = permissionChecker;
        this.authoringPermissionService = authoringPermissionService;
    }

    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method ) {
        Role requiredRole = findRole( resource, method );
        if( requiredRole == null ) {
            return null;
        } else {
            Boolean bb = permissionChecker.hasRole( requiredRole, resource, request.getAuthorization() );
            if( bb != null && !bb.booleanValue() ) {
                log.warn( "denying access due to permissionChecker: " + permissionChecker.getClass() + " for role: " + requiredRole.name() );
            }
            return bb;
        }
    }

    private Role findRole( Resource resource, Method method ) {
        if( resource instanceof Templatable ) {
            boolean isEdit = isMethod( method, new Method[]{Method.PROPPATCH, Method.COPY, Method.DELETE, Method.MOVE, Method.LOCK, Method.PUT, Method.UNLOCK, Method.MKCOL} );
            Templatable t = (Templatable) resource;
            if( isEdit ) {
                return authoringPermissionService.getEditRole( t );
            } else {
                if( isMethod( method, new Method[]{Method.PROPFIND, Method.GET, Method.HEAD, Method.OPTIONS, Method.POST} ) ) {
                    return Role.VIEWER;
                }
                throw new RuntimeException( "Unhandled method in permissionsauthoriser: " + method );
            }
        } else {
            Method m = method;

            if( isMethod( method, new Method[]{Method.PROPPATCH, Method.COPY, Method.DELETE, Method.MOVE, Method.LOCK, Method.PUT, Method.UNLOCK, Method.MKCOL} ) )
                return Role.AUTHOR;
            // allow post for viewers, so they can add comments etc. actual permissions must be
            // checked by components
            if( isMethod( method, new Method[]{Method.PROPFIND, Method.GET, Method.HEAD, Method.OPTIONS, Method.POST} ) )
                return Role.VIEWER;
            throw new RuntimeException( "Unhandled method in permissionsauthoriser: " + m );
        }
    }

    private boolean isMethod( Method m, Method[] methods ) {
        for( Method m1 : methods ) {
            if( m1.equals( m ) ) return true;
        }
        return false;
    }

    public Set<CheckResult> checkPermissions( Request request, PropertyPermission perm, Set<QName> fields, Resource resource ) {
        Set<CheckResult> results = null;
        for( QName name : fields ) {
            if( isClydeNs( name ) ) {
                if( !checkField( name, request, perm, resource ) ) {
                    if( results == null ) {
                        results = new HashSet<CheckResult>();
                    }
                    results.add( new CheckResult( name, Status.SC_UNAUTHORIZED, "Not authorised to edit field: " + name.getLocalPart(), resource ) );
                }
            }
        }
        return results;
    }

    private boolean isClydeNs( QName name ) {
        return name.getNamespaceURI() != null && name.getNamespaceURI().equals( "clyde" );
    }

    private boolean checkField( QName name, Request request, PropertyPermission propertyPermission, Resource resource ) {
        Role role = getRequiredRole( name, resource, propertyPermission );
        return permissionChecker.hasRole( role, resource, request.getAuthorization() );
    }

    private Role getRequiredRole( QName name, Resource resource, PropertyPermission propertyPermission ) {

        BeanProperty anno = getAnnotation( resource );
        if( anno == null ) {
            return defaultRole(resource, propertyPermission);
        }

        PropertyDescriptor pd = getPropertyDescriptor( resource, name.getLocalPart() );
        if( pd == null || pd.getReadMethod() == null ) {
            return defaultRole(resource, propertyPermission);
        } else {
            if( propertyPermission == PropertyPermission.READ ) {
                return anno.readRole();
            } else {
                return anno.writeRole();
            }
        }
    }

    private BeanProperty getAnnotation( Resource r ) {
        return r.getClass().getAnnotation( BeanProperty.class );
    }

    private PropertyDescriptor getPropertyDescriptor( Resource r, String name ) {
        try {
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( r, name );
            return pd;
        } catch( IllegalAccessException ex ) {
            throw new RuntimeException( ex );
        } catch( InvocationTargetException ex ) {
            throw new RuntimeException( ex );
        } catch( NoSuchMethodException ex ) {
            return null;
        }

    }

    private Role defaultRole( Resource resource, PropertyPermission propertyPermission ) {
            if( propertyPermission == PropertyPermission.READ ) {
                return Role.VIEWER;
            } else {
                if( resource instanceof Templatable ) {
                    return authoringPermissionService.getEditRole( (Templatable) resource);
                } else {
                    return Role.SYSADMIN;
                }
            }

    }
}
