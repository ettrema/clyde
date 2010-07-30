package com.bradmcevoy.web.component;

import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.security.ClydeAuthoriser;

/**
 *
 * @author brad
 */
public class PayPalIpnAuthoriser implements ClydeAuthoriser {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( PayPalIpnAuthoriser.class );

    private String payPalIpnComponentName = "payPalIpn";

    public String getName() {
        return this.getClass().getCanonicalName();
    }

    public Boolean authorise( Resource resource, Request request, Method method ) {
        log.trace( "authorise: " + resource.getClass() );

        if( request.getMethod().equals( Request.Method.POST)) {
            if( resource instanceof CommonTemplated ) {
                CommonTemplated commonTemplated = (CommonTemplated) resource;
                Component c = commonTemplated.getComponent( payPalIpnComponentName );
                if( c != null ) {
                    if( request.getParams().containsKey( PayPalIpnComponent.RECEIVER_EMAIL_PARAM ) ) {
                        log.debug( "found ipn param, returning true" );
                        return Boolean.TRUE;
                    } else {
//                        log.debug( "no receiver param, dont care");
                    }
                } else {
//                    log.debug( "no paypalipn component, so dont care");
                }
            } else {
//                log.debug( "not a ct, dont care");
            }
        } else {
//            log.debug( "not a post, so dont care");
        }

        return null;
    }

    public String getPayPalIpnComponentName() {
        return payPalIpnComponentName;
    }

    public void setPayPalIpnComponentName( String payPalIpnComponentName ) {
        this.payPalIpnComponentName = payPalIpnComponentName;
    }

    
}
