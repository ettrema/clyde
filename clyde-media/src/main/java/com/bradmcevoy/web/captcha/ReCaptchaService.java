package com.bradmcevoy.web.captcha;

import com.bradmcevoy.http.Request;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author brad
 */
public class ReCaptchaService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ReCaptchaService.class );
    private String paramChallenge = "challenge";
    private String paramResponse = "response";
    private String recaptchaUrl = "http://www.google.com/recaptcha/api/verify";

    public boolean isValid( String privateKey, Map<String, String> parameters, Request request ) throws IOException {
        String challenge = parameters.get( paramChallenge );
        String response = parameters.get( paramResponse );
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod( recaptchaUrl );
        post.addParameter( "privatekey", privateKey );
        post.addParameter( "remoteip", request.getRemoteAddr() );
        post.addParameter( "challenge", challenge );
        post.addParameter( "response", response );
        int respCode = httpClient.executeMethod( post );
        if( respCode >= 200 && respCode < 300 ) {
            if( log.isTraceEnabled() ) {
                log.trace( "isValid. Got response code: " + respCode );
            }
            String body = post.getResponseBodyAsString();
            if( StringUtils.isEmpty( body ) ) {
                throw new RuntimeException( "Error response from recaptcha server. Body is empty. Response code: " + respCode );
            } else {
                body = body.trim();
                if( body.startsWith( "true" ) ) {
                    return true;
                } else {
                    String[] arr = body.split( "\n" );
                    if( log.isTraceEnabled() ) {
                        if( arr.length < 2 ) {
                            log.trace( "validation result is false, but no reason given" );
                        } else {
                            log.trace( "validaion result is false, reason: " + arr[1] );
                        }
                    }
                    return false;
                }
            }
        } else {
            throw new RuntimeException( "Error response from recaptcha server. HTTP Response code it: " + respCode );
        }
    }

    public String getParamChallenge() {
        return paramChallenge;
    }

    public void setParamChallenge( String paramChallenge ) {
        this.paramChallenge = paramChallenge;
    }

    public String getParamResponse() {
        return paramResponse;
    }

    public void setParamResponse( String paramResponse ) {
        this.paramResponse = paramResponse;
    }

    public String getRecaptchaUrl() {
        return recaptchaUrl;
    }

    public void setRecaptchaUrl( String recaptchaUrl ) {
        this.recaptchaUrl = recaptchaUrl;
    }


}
