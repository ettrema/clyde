package com.bradmcevoy.web.captcha;

/**
 *
 * @author brad
 */
public class CaptchaServiceImpl implements CaptchaService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CaptchaServiceImpl.class );
    private static final long MAX_TIME_MS = 60 * 60 * 1000;

    private final SystemTimeService systemTimeService;

    public CaptchaServiceImpl( SystemTimeService systemTimeService ) {
        this.systemTimeService = systemTimeService;
    }

    public CaptchaServiceImpl() {
        this.systemTimeService = new SystemTimeServiceImpl();
    }



    public String getChallenge() {
        return systemTimeService.getCurrentTimeInMillis() + "";
    }

    public boolean validateResponse( String challenge, String response ) {
        try {
            Long l = Long.parseLong( challenge );
            long now = systemTimeService.getCurrentTimeInMillis();
            if( now - l > MAX_TIME_MS ) {
                log.debug( "challenge has expired" );
                return false;
            } else {
                String h = challenge.hashCode() + "";
                h = h.substring( h.length() - 6, h.length() - 1 );
                return response.equals( h );
            }
        } catch( NumberFormatException e ) {
            log.debug( "invalid number: " + e.getMessage() );
            return false;
        }
    }

    public String getResponse( String challenge ) {
        try {
            Long l = Long.parseLong( challenge );
            long now = systemTimeService.getCurrentTimeInMillis();
            if( now - l > MAX_TIME_MS ) {
                throw new RuntimeException( "challenge has expired" );
            } else {
                String h = challenge.hashCode() + "";
                h = h.substring( h.length() - 6, h.length() - 1 );
                return h;
            }
        } catch( NumberFormatException numberFormatException ) {
            throw new RuntimeException( "invalid number: " + challenge );
        }
    }

    public boolean isValidChallenge( String challenge ) {
        try {
            Long l = Long.parseLong( challenge );
            long now = systemTimeService.getCurrentTimeInMillis();
            if( now - l > MAX_TIME_MS || l > now ) {
                return false;
            } else {
                return true;
            }
        } catch( NumberFormatException e ) {
            log.debug( "invalid number: " + e.getMessage() );
            return false;
        }

    }
}
