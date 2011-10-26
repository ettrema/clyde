package com.bradmcevoy.web.security;

import com.bradmcevoy.web.User;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Very simple implementation which just holds data in memory
 *
 * @author brad
 */
public class MemoryPasswordRecoveryService implements PasswordRecoveryService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MemoryPasswordRecoveryService.class );
    private final Map<UUID, TokenAndTime> mapOfTokens = new ConcurrentHashMap<UUID, TokenAndTime>();
    private long expiresTimeMillis = 1000 * 60 * 60 * 24;

    public String createToken( User user ) {
        int iToken = (int) ( Math.random() * 10000 );
        String token = iToken + "";
        TokenAndTime tokenAndTime = new TokenAndTime( token, System.currentTimeMillis() );
        mapOfTokens.put( user.getNameNodeId(), tokenAndTime );
        return token;
    }

    public boolean isTokenValid( String token, User user ) {
        TokenAndTime tokenAndTime = mapOfTokens.get( user.getNameNodeId() );
        if( tokenAndTime == null ) {
            log.trace( "token not found: " + token );
            return false;
        } else {
            long delta = System.currentTimeMillis() - tokenAndTime.time;
            if( delta > expiresTimeMillis ) {
                log.trace( "token has expired" );
                return false;
            } else {
                log.trace( "token is valid" );
                return true;
            }
        }
    }

    /**
     * defaults to 1 day
     * 
     * @return
     */
    public long getExpiresTimeMillis() {
        return expiresTimeMillis;
    }

    public void setExpiresTimeMillis( long expiresTimeMillis ) {
        this.expiresTimeMillis = expiresTimeMillis;
    }

    public class TokenAndTime {

        private final String token;
        private final long time;

        public TokenAndTime( String token, long time ) {
            this.token = token;
            this.time = time;
        }

        public long getTime() {
            return time;
        }

        public String getToken() {
            return token;
        }
    }
}
