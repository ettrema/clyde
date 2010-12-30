package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.utils.AuthoringPermissionService;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.ComponentMap;
import com.bradmcevoy.web.Expression;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

public class SaveCommand extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SaveCommand.class );
    private static final long serialVersionUID = 1L;
    protected Expression afterScript;

    public SaveCommand( Addressable container, Element el ) {
        super( container, el );
    }

    public SaveCommand( Addressable container, String name ) {
        super( container, name );
        afterScript = new Expression( this, "afterScript" );
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
        log.trace( "..saving" );
        boolean isNew = false;
        if( rc.getTargetPage() instanceof BaseResource ) {
            BaseResource bres = (BaseResource) rc.getTargetPage();
            isNew = bres.isNew();
        }
        log.trace( "isNew: " + isNew);
        Role requiredRole;
        if( isNew ) {
            Folder folder = rc.getTargetPage().getParentFolder();
            ITemplate template = rc.getTargetPage().getTemplate();
            requiredRole = _( AuthoringPermissionService.class ).getCreateRole( folder, template );
        } else {
            requiredRole = _( AuthoringPermissionService.class ).getEditRole( rc.getTargetPage() );
        }
        log.trace( "required role: " + requiredRole);
        if( !_( PermissionChecker.class ).hasRole( requiredRole, rc.getTargetPage(), RequestParams.current().getAuth() ) ) {
            throw new NotAuthorizedException( rc.getTargetPage() );
        }
        Templatable ct = doProcess( rc, parameters );
        if( ct != null ) {
            return ct.getHref();// if all ok, force redirect to ensure changes saved
        } else {
            return null;
        }
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
        boolean valid = true;
        for( String paramName : parameters.keySet() ) {
            Path path = Path.path( paramName );
            Component c = rcTarget.findComponent( path );
            if( c != null ) {
                if( !( c instanceof Command ) ) {
                    BaseResource res = findPage( c );
                    if( res != null ) {
                        //log.debug( ".. component: " + c.getName() + " mapped to page: " + res.getPath() );
                        pages.add( res );
                    }
                    if( !c.validate( rc ) ) {
                        log.trace("not valid: " + c.getName());
                        valid = false;
                    } else {
                        log.trace("is valid: " + c.getName());
                    }
                } else {
                    log.trace("do not validate command: " + c.getName());
                }
            } else {
                log.debug( "Failed to find component: " + path );
            }
        }

        if( valid ) {
            log.debug( "valid, save pages and commit" );
            for( BaseResource page : pages ) {
                execAfterScript( page, rc );
            }
            for( BaseResource page : pages ) {
                page.save();
            }
            commit();
            return rcTarget.page;
        } else {
            log.debug( "not valid, not saving" );
            return null;
        }
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
        Addressable a = c.getContainer();
        if( a == null ) {
            return null;
        }
        return findPage( a );
    }

    @Override
    public void fromXml( Element el ) {
        super.fromXml( el );
        ComponentMap components = new ComponentMap();
        components._fromXml( this, el );
        fromXml( components );
        log.debug( "fromXml: " + afterScript );
    }

    protected void fromXml( ComponentMap map ) {
        Component c;
        c = consume( map, "afterScript" );
        if( c == null ) {
            afterScript = new Expression( this, "afterScript" );
        } else {
            afterScript = (Expression) c;
        }
    }

    protected Component consume( ComponentMap map, String name ) {
        Component c = map.get( name );
        return c;
    }

    protected void execAfterScript( BaseResource newlyCreated, RenderContext rc ) {
        if( afterScript == null ) return;
        log.debug( "execAfterScript" );
        Map map = new HashMap();
        map.put( "created", newlyCreated );
        map.put( "rc", rc );
        map.put( "command", this );
        Templatable ct = (Templatable) this.getContainer();
        afterScript.calc( ct, map );
        log.debug( "done execAfterScript" );
    }
}
