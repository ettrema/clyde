package com.ettrema.facebook;

/**
 *
 * @author brad
 */
public class FaceBookCredentials {
    private final String apiKey;
    private final String apiSecret;
    private final String secret;
    private final String sessionId;
    private final Long userId;

    public FaceBookCredentials( String apiKey, String apiSecret, String secret, String sessionId, Long userId ) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.secret = secret;
        this.sessionId = sessionId;
        this.userId = userId;
    }




    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }
        
    public String getSecret() {
        return secret;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    
    
}
