package com.bradmcevoy.utils;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Host;
import java.util.List;

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

    public SecureRedirectService( RequestService requestService, HrefService hrefService ) {
        this.requestService = requestService;
        this.hrefService = hrefService;
    }

    public String checkRedirect( Resource res, Request request ) {
        if( requestService.isSecure( request ) ) {
            return null;
        } else {
            if( res instanceof CommonTemplated ) {
                CommonTemplated resource = (CommonTemplated) res;
                if( requiresSecure( resource ) ) {
                    String redir = hrefService.getHref( resource, true );
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
}
