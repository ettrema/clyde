package com.bradmcevoy.web.captcha;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.web.RenderContext;
import java.io.IOException;
import java.util.Map;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class ReCaptchaHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ReCaptchaHelper.class );
    private final ReCaptchaComponent comp;
    private final RenderContext rc;
    private final Map<String, String> parameters;

    public ReCaptchaHelper( ReCaptchaComponent comp, RenderContext rc, Map<String, String> parameters ) {
        this.comp = comp;
        this.rc = rc;
        this.parameters = parameters;
    }

    public boolean validate() {
        boolean valid;
        try {
            valid = _( ReCaptchaService.class ).isValid( comp.getPrivateKey(), parameters, HttpManager.request() );
            if( !valid ) {
                comp.setValidationMessage( "The captcha value given was incorrect, please retry it" );
            }
        } catch( IOException ex ) {
            valid = true;
            log.error( "exception accessing recaptcha server", ex );
        }
        return valid;
    }
}
