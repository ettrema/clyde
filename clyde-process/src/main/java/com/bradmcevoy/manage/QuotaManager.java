package com.bradmcevoy.manage;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.quota.QuotaDataAccessor;
import com.bradmcevoy.http.quota.StorageChecker;
import com.bradmcevoy.process.TokenValue;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.HostFinder;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.ComponentValue;

/**
 *
 * @author brad
 */
public class QuotaManager extends VfsCommon implements StorageChecker, QuotaDataAccessor {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( QuotaManager.class );
    private final String processName;
    private final String limitVarName;
    private final String usageVarName;
    private HostFinder hostFinder = new HostFinder();

    public QuotaManager( String processName, String limitVarName, String usageVarName ) {
        this.processName = processName;
        this.limitVarName = limitVarName;
        this.usageVarName = usageVarName;
    }

    public Integer getUsagePercentage( Host host ) {
        TokenValue token = getTokenValue( host );
        Object oPerc = token.getVariables().get( usageVarName + "Perc");
        Long val = toLong( oPerc);
        if( val == null ) {
            return 0;
        } else {
            return val.intValue();
        }
    }


    private TokenValue getTokenValue( Host host ) {
        ComponentValue cv = host.getValues().get( processName );
        if( cv == null ) {
            log.warn( "no value called: " + processName );
            return null;
        }

        Object oProcess = cv.getValue();
        if( oProcess == null ) {
            log.warn( "Did not find a process token at value name: " + processName );
            return null;
        }

        if( !( oProcess instanceof TokenValue ) ) {
            log.warn( "Process name did not locate a token. Got a: " + oProcess.getClass() );
            return null;
        }

        TokenValue token = (TokenValue) oProcess;
        return token;

    }

    private Long getLimit( TokenValue token ) {
        if( token == null ) {
            log.debug( "token is null");
            return null;
        }
        if( token.getVariables() == null ) {
            log.debug( "token variables is null");
            return null;
        }
        Object oLimit = token.getVariables().get( limitVarName );
        Long limit = toLong( oLimit );

        return limit;
    }

    private Long getUsage(TokenValue token) {
        if( token == null ) {
            log.debug( "token is null");
            return null;
        }
        if( token.getVariables() == null ) {
            log.debug( "token variables is null");
            return null;
        }
        Object oUsage = token.getVariables().get( usageVarName );
        Long currentUsage = toLong( oUsage );
        return currentUsage;
    }

    /**
     * Recalculation takes too long. Almost 4 minutes for 20Gig of files
     *
     * @param host
     */
    public void invalidateUsage( Host host ) {
        TokenValue token = getTokenValue( host );
        if( token == null ) {
            log.warn( "Couldnt locate token for host: " + host.getName() );
            return;
        }
        log.debug( "clearing cached usage: " + usageVarName );
        token.getVariables().remove( usageVarName );
        host.save();
    }

    /**
     * 
     * @param host
     * @param amount - negative for a delete, positive for a put
     */
    public void incrementUsage( Host host, long amount ) {
        log.debug( "incrementUsage: " + host.getName() + " amount: " + amount);
        TokenValue token = getTokenValue( host );
        if( token == null ) {
            log.warn( "Couldnt locate token for host: " + host.getName() );
            return;
        }
        Long currentUsage = getUsage( token );
        if( currentUsage == null ) currentUsage = 0l;
        Long newUsage = currentUsage + amount;        
        token.getVariables().put( usageVarName, newUsage);
        long limit = getLimit( token );
        long perc = (newUsage * 100) / limit;
        token.getVariables().put( usageVarName + "Perc", perc);
        log.debug( "new usage is: " + newUsage + "   : " + perc + "%");

        host.save();
    }


    private Long toLong( Object oLimit ) throws RuntimeException {
        Long limit;
        if( oLimit == null ) {
            limit = null;
        } else if( oLimit instanceof Long ) {
            limit = (Long) oLimit;
        } else if( oLimit instanceof Integer ){
            int i = (Integer)oLimit;
            limit = (long)i;
        } else if( oLimit instanceof String ) {
            String s = (String) oLimit;
            limit = Long.parseLong( s );
        } else {
            throw new RuntimeException( "unsupported limit class: " + oLimit.getClass() );
        }
        return limit;
    }

    public StorageErrorReason checkStorageOnReplace( Request request, CollectionResource parent, Resource replaced, String host ) {
        return checkStorageOnAdd(request, parent, Path.root, host );
    }

    public StorageErrorReason checkStorageOnAdd( Request request, CollectionResource nearestParent, Path parentPath, String host ) {
        Host theHost = hostFinder.getHost( host );
        if( theHost.isDisabled() ) {
            return StorageErrorReason.SER_QUOTA_EXCEEDED;
        } else {
            return null;
        }
    }
    


    public Long getQuotaAvailable( Resource r ) {
        if(r instanceof Templatable ) {
            Templatable ct = (Templatable) r;
            Host host = ct.getHost();
            TokenValue token = getTokenValue( host );
            if( token == null ) {
                log.debug( "no token");
                return null;
            } else {
                Long limit = getLimit( token );
                Long currentUsage = getUsage( token );
                if( limit == null || currentUsage == null ) {
                    log.warn("Couldnt determine quota available: " + host.getName());
                    return null;
                } else {
                    return limit - currentUsage;
                }
            }
        } else {
            log.warn( "not a Templatable");
            return null;
        }
    }

    public Long getQuotaUsed( Resource r ) {
        if(r instanceof Templatable ) {
            Templatable ct = (Templatable) r;
            Host host = ct.getHost();
            TokenValue token = getTokenValue( host );
            Long currentUsage = getUsage( token );
            return currentUsage;
        } else {
            log.warn( "not a Templatable");
            return null;
        }
    }

}
