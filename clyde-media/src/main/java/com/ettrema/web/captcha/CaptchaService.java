package com.bradmcevoy.web.captcha;

/**
 *
 * @author brad
 */
public interface CaptchaService {

    String getChallenge();

    boolean validateResponse( String challenge, String response );

    /**
     * For a given challenge, return the correct response
     *
     * @param challenge
     * @return
     */
    String getResponse( String challenge );

    /**
     *
     * @param ch
     * @return - true if the given challenge is currently valid
     */
    boolean isValidChallenge( String ch );

}
