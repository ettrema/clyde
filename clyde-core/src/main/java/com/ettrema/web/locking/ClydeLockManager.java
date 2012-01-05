package com.ettrema.web.locking;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import com.ettrema.web.BaseResource;

/**
 *
 * @author brad
 */
public interface ClydeLockManager {
    LockResult lock(LockTimeout timeout, LockInfo lockInfo, BaseResource resource) throws NotAuthorizedException, LockedException;

    LockResult refresh(String token, BaseResource resource) throws NotAuthorizedException, PreConditionFailedException;

    void unlock(String tokenId, BaseResource resource) throws NotAuthorizedException, PreConditionFailedException;

    LockToken getCurrentLock(BaseResource resource);

}
