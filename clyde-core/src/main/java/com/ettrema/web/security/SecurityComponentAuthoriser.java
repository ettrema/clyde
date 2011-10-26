
package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.TemplateInput;
import com.bradmcevoy.web.velocity.VelocityInterpreter;
import org.apache.velocity.VelocityContext;

/**
 * Implements page level security by delegating to a component
 *
 *
 * @author brad
 */
public class SecurityComponentAuthoriser implements ClydeAuthoriser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SecurityComponentAuthoriser.class );

    @Override
    public String getName() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public Boolean authorise( Resource resource, Request request, Method method, Auth auth ) {
        if( !( resource instanceof CommonTemplated ) ) {
            return null;
        }
        log.debug( "authorise: " + resource.getName());
        CommonTemplated commonTemplated = (CommonTemplated) resource;
        Boolean bb = checkSecurityComponent( request, request.getMethod(), auth, commonTemplated );
        if( bb != null ) {
            log.debug( "got answer from security component: " + bb);
            return bb.booleanValue();
        }
        log.debug( "no answer from checkSecurityComponent, go ask folder" );
        Folder parent = commonTemplated.getParentFolder();
        if( parent != null ) {
            boolean b = parent.authorise( request, method, auth );
            log.debug( "parent said: " + b);
            return b;
        } else {
            return null;
        }

    }

    private Boolean checkSecurityComponent( Request request, Request.Method method, Auth auth, CommonTemplated commonTemplated ) {
        Component c = commonTemplated.getComponent( "security" );
        if( c == null ) {
            return null;
        }

        String s;
        if( c instanceof TemplateInput ) {
            String tmpl = ( (TemplateInput) c ).getValue();
            s = evaluateSecurity( tmpl, auth, method, request.getAbsolutePath() );
        } else {
            RenderContext rc = new RenderContext( commonTemplated.getTemplate(), commonTemplated, null, false );
            rc.addAttribute( "request", request );
            s = c.render( rc );
        }

        if( s == null || s.trim().length() == 0 ) {
            return null;
        }
        if( s.equalsIgnoreCase( "yes" ) || s.equalsIgnoreCase( "true" ) || s.contains( "true" ) ) {
            return true;
        } else {
            return false;
        }
    }

    private String evaluateSecurity( String tmpl, Auth auth, Request.Method method, String actualHref ) {
        log.debug( "evaluateSecurity: " + tmpl + " - auth:" + auth );
        VelocityContext vc = new VelocityContext();
        vc.put( "auth", auth );
        vc.put( "page", this );
        vc.put( "method", method );
        vc.put( "actualHref", actualHref );
        String s = VelocityInterpreter.evalToString( tmpl, vc );
        if( s != null ) {
            s = s.trim();
        }
        log.debug( "   s: " + s );
        return s;
    }
}
