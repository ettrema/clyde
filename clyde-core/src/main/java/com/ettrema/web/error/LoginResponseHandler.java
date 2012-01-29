package com.ettrema.web.error;

import com.bradmcevoy.http.AbstractWrappingResponseHandler;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.webdav.WebDavResponseHandler;
import com.ettrema.web.ajax.AjaxResourceFactory.AjaxPostResource;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.CollectionUtils;

/**
 *
 * @author brad
 */
public class LoginResponseHandler extends AbstractWrappingResponseHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( LoginResponseHandler.class );
    private String loginPage = "/login.html";
    private ResourceFactory resourceFactory;
    private List<String> excludePaths;

    public LoginResponseHandler( WebDavResponseHandler wrapped ) {
        super( wrapped );
    }

    /**
     * If responding with a login page, the request attribute "authReason" is set
     * to either "required", indicating that the user must login; or "notPermitted"
     * indicating that the user is currently logged in but does not have permission
     *
     * @param resource
     * @param response
     * @param request
     */
    @Override
    public void respondUnauthorised( Resource resource, Response response, Request request ) {
        log.trace("respondUnauthorised");
        String ctHeader = request.getContentTypeHeader();
        if( isPage( resource, ctHeader ) && !excluded( request ) && isGetOrPost( request ) ) {
            Resource rLogin;
            try {
                rLogin = resourceFactory.getResource( request.getHostHeader(), loginPage );
            } catch (NotAuthorizedException ex) {
                throw new RuntimeException(ex);
            } catch (BadRequestException ex) {
                throw new RuntimeException(ex);
            }
            if( rLogin == null || !( rLogin instanceof GetableResource ) ) {
                log.trace( "Couldnt find login resource: " + request.getHostHeader() + "/" + loginPage );
                wrapped.respondUnauthorised( resource, response, request );
            } else {
                log.trace( "respond with 200 to suppress login prompt" );
                try {
                    response.setStatus( Response.Status.SC_OK );
                    response.setCacheControlNoCacheHeader();
                    GetableResource gr = (GetableResource) rLogin;

                    // set request attribute so rendering knows it authorisation failed, or authentication is required
                    Auth auth = request.getAuthorization();
                    if( auth != null && auth.getTag() != null ) {
                        // no authentication was attempted,
                        request.getAttributes().put( "authReason", "notPermitted" );
                    } else {
                        request.getAttributes().put( "authReason", "required" );
                    }
                    gr.sendContent( response.getOutputStream(), null, null, "text/html" );
                } catch (NotFoundException ex) {
					throw new RuntimeException( ex );
				} catch( IOException ex ) {
                    throw new RuntimeException( ex );
                } catch( NotAuthorizedException ex ) {
                    throw new RuntimeException( ex );
                } catch( BadRequestException ex ) {
                    throw new RuntimeException( ex );
                }
            }
        } else if ( resource instanceof AjaxPostResource && !excluded( request ) && isGetOrPost( request )) {
            log.trace("ajax post resource, so suppress login prompt");
            wrapped.respondForbidden( resource, response, request );
        } else {
            log.trace( "respond with normal 401" );
            wrapped.respondUnauthorised( resource, response, request );
        }
    }

    private boolean isPage( Resource resource, String ctHeader ) {
        if( resource instanceof GetableResource ) {
            GetableResource gr = (GetableResource) resource;
            String ctResource = gr.getContentType( "text/html" );
            if( ctResource == null ) {
                if( ctHeader != null ) {
                    boolean b = ctHeader.contains( "html" );
                    log.trace( "isPage: resource has no content type, depends on requested content type: " + b );
                    return b;
                } else {
                    log.trace( "isPage: resource has no content type, and no requeted content type, so assume false" );
                    return false;
                }
            } else {
                boolean b = ctResource.contains( "html" );
                log.trace( "isPage: resource has content type. is html? " + b );
                return b;
            }
        } else {
            log.trace( "isPage: resource is not getable" );
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

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths( List<String> excludePaths ) {
        this.excludePaths = excludePaths;
    }

    private boolean excluded( Request request ) {
        if( CollectionUtils.isEmpty( excludePaths ) ) {
            return false;
        }
        for( String s : excludePaths ) {
            if( request.getAbsolutePath().startsWith( s ) ) {
                return true;
            }
        }
        return false;
    }

    private boolean isGetOrPost( Request request ) {
        return request.getMethod().equals( Method.GET ) || request.getMethod().equals( Method.POST );
    }
}
