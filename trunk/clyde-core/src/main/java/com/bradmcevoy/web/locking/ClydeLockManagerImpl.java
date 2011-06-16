package com.bradmcevoy.web.locking;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockTimeout.DateAndSeconds;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.security.CurrentUserService;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.Date;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class ClydeLockManagerImpl implements ClydeLockManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ClydeLockManagerImpl.class );
    private static final String NODE_NAME = "_sys_lock";
    private static final long MAX_LOCK_SECONDS = 60 * 60 * 24; // 1 day
    private final CurrentUserService currentUserService;

    public ClydeLockManagerImpl( CurrentUserService currentUserService ) {
        this.currentUserService = currentUserService;
    }

    public LockResult lock( LockTimeout timeout, LockInfo lockInfo, BaseResource resource ) throws NotAuthorizedException, LockedException {
        log.info( "lock: " + resource.getHref() );
        // need to record: uid, timeoutdate, who locked it
        NameNode itemNode = resource.getNameNode();
        NameNode lockNode = itemNode.child( NODE_NAME );
        if( lockNode != null ) {
            ClydeLock lock = (ClydeLock) lockNode.getData();
            if( isExpired( lock ) ) {
                lockNode.delete();
                lockNode = null;
            } else {
                log.warn( "already locked" );
                throw new LockedException( resource );
            }
        }
        ClydeLock lock = new ClydeLock();
        IUser user = currentUserService.getOnBehalfOf();
        if( user == null ) {
            log.warn( "no current user" );
            throw new NotAuthorizedException( resource );
        }
        lock.setLockedByUserId( user.getNameNodeId() );

        DateAndSeconds das = timeout.getLockedUntil( timeout.getSeconds(), MAX_LOCK_SECONDS );
        lock.setLockedUntil( das.date );

        lockNode = resource.getNameNode().add( NODE_NAME, lock );
        lockNode.save();
        LockToken token = new LockToken();
        token.tokenId = lock.getTokenId();
        token.info = new LockInfo( LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, user.getName(), LockInfo.LockDepth.ZERO );
        token.timeout = new LockTimeout( das.seconds );
        return LockResult.success( token );

    }

    public LockResult refresh( String currentToken, BaseResource resource ) throws NotAuthorizedException, PreConditionFailedException {
        log.info( "refresh: " + resource.getHref() );
        IUser user = currentUserService.getOnBehalfOf();
        if( user == null ) {
            log.warn( "no current user" );
            throw new NotAuthorizedException( resource );
        }

        NameNode itemNode = resource.getNameNode();
        NameNode lockNode = itemNode.child( NODE_NAME );
        if( lockNode == null ) {
            log.warn( "no current lock" );
            throw new PreConditionFailedException( resource );
        }
        ClydeLock lock = (ClydeLock) lockNode.getData();
        checkLockOwner( user, lock, resource );
        checkToken( currentToken, lock, resource );
        long duration = lock.getDuration();
        Date newExpiry = LockTimeout.addSeconds( duration ).date;
        lock.setLockedUntil( newExpiry );
        lock.save();

        LockToken token = new LockToken();
        token.tokenId = lock.getTokenId();
        token.info = new LockInfo( LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, user.getName(), LockInfo.LockDepth.ZERO );
        token.timeout = new LockTimeout( duration );
        return LockResult.success( token );

    }

    public void unlock( String requestedToken, BaseResource resource ) throws PreConditionFailedException, NotAuthorizedException {
        log.info( "unlock: " + resource.getHref() );
        IUser user = currentUserService.getOnBehalfOf();
        if( user == null ) {
            log.warn( "no current user" );
            throw new NotAuthorizedException( resource );
        }

        NameNode itemNode = resource.getNameNode();
        NameNode lockNode = itemNode.child( NODE_NAME );
        if( lockNode == null ) {
            log.warn( "no current lock" );
            return ;
        }
        ClydeLock lock = (ClydeLock) lockNode.getData();
        checkLockOwner( user, lock, resource );
        checkToken( requestedToken, lock, resource );
        lockNode.delete();
    }

    public LockToken getCurrentLock( BaseResource resource ) {
        log.debug( "getCurrentLock: " + resource.getHref() );
        NameNode itemNode = resource.getNameNode();
        NameNode lockNode = itemNode.child( NODE_NAME );
        if( lockNode == null ) {
            return null;
        }
        ClydeLock lock = (ClydeLock) lockNode.getData();
        if( isExpired( lock ) ) {
            log.info( "deleting expired lock" );
            lockNode.delete();
            return null;
        }

        IUser user = findUser( lock );
        if( user == null ) {
            log.warn( "Couldnt find user for lock on resource: " + resource.getHref() );
            return null;
        }
        String userHref = user.getName();

        log.info( "locked by user: " + userHref );
        LockToken token = new LockToken();
        token.tokenId = lock.getTokenId();
        token.info = new LockInfo( LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, userHref, LockInfo.LockDepth.ZERO );
        token.timeout = new LockTimeout( lock.getDuration() );
        return token;
    }

    private void checkLockOwner( IUser user, ClydeLock lock, Resource target ) throws PreConditionFailedException {
        if( !user.getNameNodeId().equals( lock.getLockedByUserId() ) ) {
            log.warn( "current user down not own the current lock" );
            throw new PreConditionFailedException( target );
        }
    }

    private void checkToken( String currentToken, ClydeLock lock, BaseResource resource ) throws NotAuthorizedException, PreConditionFailedException {
        if( !lock.getTokenId().equals( currentToken ) ) {
            log.warn( "given token does not match current: req: " + currentToken + " actual:" + lock.getId().toString() );
            throw new PreConditionFailedException( resource );
        }
    }

    private boolean isExpired( ClydeLock lock ) {
        if( lock.getLockedUntil().before( new Date() ) ) {
            log.debug( "expired lock" );
            return true;
        } else {
            return false;
        }
    }

    private IUser findUser( ClydeLock lock ) {
        NameNode nnUser = _( VfsSession.class ).get( lock.getLockedByUserId() );
        DataNode o = nnUser.getData();
        if( o == null ) {
            return null;
        } else if( o instanceof IUser ) {
            return (IUser) o;
        } else {
            log.warn( "lock target is not a user: " + lock.getLockedByUserId() + " is a: " + o.getClass().getCanonicalName() );
            return null;
        }
    }
}
