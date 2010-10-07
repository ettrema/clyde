package com.bradmcevoy.web.error;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import java.io.IOException;

/**
 *
 * @author brad
 */
public class LoginResponseHandler extends DefaultWebDavResponseHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( LoginResponseHandler.class );

    private String loginPage = "/login.html";
    private ResourceFactory resourceFactory;

    public LoginResponseHandler( AuthenticationService authenticationService ) {
        super( authenticationService );
    }

    @Override
    public void respondUnauthorised( Resource resource, Response response, Request request ) {
        if( isPage( resource ) ) {
            Resource rLogin = resourceFactory.getResource( request.getHostHeader(), loginPage );
            if( rLogin == null || !(rLogin instanceof GetableResource) ) {
                log.warn( "Couldnt find login resource: " + request.getHostHeader() + "/" + loginPage);
                wrapped.respondUnauthorised( resource, response, request );
            } else {
                log.trace("respond with 403 to suppress login prompt");
                try {
                    response.setStatus( Response.Status.SC_FORBIDDEN );
                    GetableResource gr = (GetableResource) rLogin;
                    gr.sendContent( response.getOutputStream(), null, null, "text/html" );
                } catch( IOException ex ) {
                    throw new RuntimeException( ex );
                } catch( NotAuthorizedException ex ) {
                    throw new RuntimeException( ex );
                } catch( BadRequestException ex ) {
                    throw new RuntimeException( ex );
                }
            }
        } else {
            log.trace("respond with normal 401");
            wrapped.respondUnauthorised( resource, response, request );
        }
    }

    private boolean isPage( Resource resource ) {
        if( resource instanceof GetableResource ) {
            GetableResource gr = (GetableResource) resource;
            String ct = gr.getContentType( "text/html" );
            return ( ct != null && ct.contains( "html" ) );
        } else {
            return false;
        }
    }

    public String getLoginPage() {
        return loginPage;
    }

    public void setLoginPage( String loginPage ) {
        this.loginPage = loginPage;
    }

    public ResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public void setResourceFactory( ResourceFactory resourceFactory ) {
        this.resourceFactory = resourceFactory;
    }
}
