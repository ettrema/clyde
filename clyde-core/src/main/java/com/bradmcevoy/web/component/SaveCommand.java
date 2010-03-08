package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jdom.Element;

public class SaveCommand extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SaveCommand.class );
    private static final long serialVersionUID = 1L;

    public SaveCommand( Addressable container, Element el ) {
        super( container, el );
    }

    public SaveCommand( Addressable container, String name ) {
        super( container, name );
    }

    @Override
    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
        // TODO: validate
        if( !isApplicable( parameters ) ) {
            return null; // not this command
        } else {
            return doProcess( rc, parameters, files );
        }
    }

    @Override
    protected String doProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
        log.debug( "..saving" );
        PermissionChecker permissionChecker = requestContext().get( PermissionChecker.class );
        if( !permissionChecker.hasRole( Role.AUTHOR, rc.getTargetPage(), RequestParams.current().getAuth() ) ) {
            throw new NotAuthorizedException(rc.getTargetPage());
        }
        Templatable ct = doProcess( rc, parameters );
        return ct.getHref();// if all ok, force redirect to ensure changes saved
    }

    @Override
    public boolean validate( RenderContext rc ) {
        return true;
    }

    protected boolean isApplicable( Map<String, String> parameters ) {
        String s = parameters.get( this.getName() );
        return ( s != null );
    }

    protected Templatable doProcess( RenderContext rc, Map<String, String> parameters ) {
        RenderContext rcTarget = rc.getTarget();
        Set<BaseResource> pages = new HashSet<BaseResource>();
        if( rcTarget.page instanceof BaseResource ) {
            pages.add( (BaseResource) rcTarget.page );
        }
        for( String paramName : parameters.keySet() ) {
            Path path = Path.path( paramName );
            Component c = rcTarget.findComponent( path );
            if( c != null ) {
                if( !( c instanceof Command ) ) {
                    BaseResource res = findPage( c );
                    if( res != null ) {
                        log.debug( ".. component: " + c.getName() + " mapped to page: " + res.getPath() );
                        pages.add( res );
                    }
                }
            } else {
                log.debug( "Failed to find component: " + path );
            }
        }

        for( BaseResource page : pages ) {
            page.save();
        }
        commit();

        return rcTarget.page;
    }

    /**
     * The page to save for the given container. May be the container itself
     * 
     * @param container
     * @return
     */
    private BaseResource findPage( Addressable container ) {
        if( container instanceof BaseResource ) {
            return (BaseResource) container;
        } else if( container instanceof Component ) {
            return findPage( (Component) container );
        } else if( container instanceof Templatable ) {
            Templatable ct = (Templatable) container;
            Templatable parent = ct.getParent();
            return findPage( parent );
        } else {
            log.warn( "Unhandled type of Addressable: " + container.getClass().getName() );
            return null;
        }
    }

    /**
     * Find the page to save for the given component
     * 
     * @param c
     * @return - the first physical resource in this components parent hierarchy
     */
    private BaseResource findPage( Component c ) {
        Addressable container = c.getContainer();
        if( container == null ) {
            return null;
        }
        return findPage( container );
    }
}
