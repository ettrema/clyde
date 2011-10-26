package com.ettrema.utils;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Host;
import java.util.List;
import java.util.Map;

/**
 * Implements redirecting to https if needed
 *
 * @author brad
 */
public class SecureRedirectService implements RedirectService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( SecureRedirectService.class );
    private final RequestService requestService;
    private final HrefService hrefService;
    private List<String> secureHosts;
    private List<String> secureDomains;
    private List<String> contentTypes;

    public SecureRedirectService( RequestService requestService, HrefService hrefService ) {
        this.requestService = requestService;
        this.hrefService = hrefService;
    }

    public String checkRedirect( Resource res, Request request ) {
        log.trace( "checkRedirect" );
        if( requestService.isSecure( request ) ) {
            return null;
        } else {
            if( res instanceof CommonTemplated ) {
                log.trace( "is common templated" );
                CommonTemplated resource = (CommonTemplated) res;
                if( requiresSecure( resource ) ) {
                    log.trace( "requires secure" );
                    String redir = hrefService.getHref( resource, true );
                    if( request.getParams() != null && request.getParams().size() > 0 ) {
                        log.trace( "has params, so append to redirect url" );
                        String params = "";
                        for( Map.Entry<String, String> entry : request.getParams().entrySet() ) {
                            if( params.length() > 0 ) {
                                params += "&";
                            }
                            params += entry.getKey() + "=" + entry.getValue();
                        }
                        redir += "?" + params;
                    }
                    return redir;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private boolean requiresSecure( CommonTemplated r ) {
        Host h = r.getHost();
        if( h == null ) {
            return false;
        } else {
            // If content types are given, but does not match, then do not require secure
            if( contentTypes != null && contentTypes.size() > 0 ) {
                boolean found = false;
                String thisCt = r.getContentType();
                for( String ct : contentTypes ) {
                    if( thisCt.contains( ct ) ) {
                        found = true;
                    }
                }
                if( !found ) {
                    log.trace( "request resource type does not match specified content types, so not secure" );
                    return false;
                }

            }

            // check for exact match in secure hosts
            String reqHostName = h.getName();
            if( secureHosts != null ) {
                if( secureHosts.contains( reqHostName ) ) {
                    return true;
                }
            }
            // Check to see if the host name is a subdomain of any secure domain
            if( secureDomains != null ) {
                for( String s : secureDomains ) {
                    if( reqHostName.endsWith( "." + s ) ) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public List<String> getSecureDomains() {
        return secureDomains;
    }

    public void setSecureDomains( List<String> secureDomains ) {
        this.secureDomains = secureDomains;
    }

    public List<String> getSecureHosts() {
        return secureHosts;
    }

    public void setSecureHosts( List<String> secureHosts ) {
        this.secureHosts = secureHosts;
    }

    public List<String> getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes( List<String> contentTypes ) {
        this.contentTypes = contentTypes;
    }
}
