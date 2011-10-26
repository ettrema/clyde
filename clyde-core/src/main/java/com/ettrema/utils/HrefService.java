package com.ettrema.utils;

import com.bradmcevoy.http.Utils;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import com.ettrema.web.Templatable;

/**
 * Utility methods for constructing href's and URL's for
 * resources
 *
 * Note that the naming may be misleading, but is kept consistent with the
 * property methods on resources. Eg getUrl returns a path but no protocol
 * or host name so isnt really a URL.
 *
 * And getHref always returns host and protocol which is not strictly needed
 * for an href
 *
 * @author brad
 */
public class HrefService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( HrefService.class );

    private final RequestService requestService;

    public HrefService( RequestService requestService ) {
        this.requestService = requestService;
    }

    /**
     * Get the fully qualified href of the given resource. If the current
     * request is secure (https) the given href will also be https
     *
     * @return
     */
    public String getHref( CommonTemplated ct ) {
        boolean isSecure = requestService.isSecure();
        return getHref( ct, isSecure );
    }

    public String getHref( CommonTemplated ct, boolean isSecure ) {
        Host h = ct.getHost();
        if( h == null ) {
            throw new NullPointerException( "No host for resource: " + ct.getName() );
        }
        String path = h.getName() + getUrl( ct );
        if( isSecure ) {
            return "https://" + path;
        } else {
            return "http://" + path;
        }
    }

    /**
     *
     * @return - the absolute path of this resource. does not include server
     */
    public String getUrl( Templatable ct ) {
        if( ct == null ) {
            return "";
        } else if( ct instanceof Host) {
            return "/";
        } else {
            Templatable parent = ct.getParent();
            String url = getUrl(parent) + Utils.percentEncode( ct.getName() );
            if( ct instanceof Folder ) {
                url = url + "/";
            }
            return url;
        }
    }
}
