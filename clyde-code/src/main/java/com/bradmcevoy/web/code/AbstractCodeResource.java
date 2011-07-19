package com.bradmcevoy.web.code;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import com.bradmcevoy.web.security.PermissionChecker;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.util.Date;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class AbstractCodeResource<T extends Resource> implements Resource, DigestResource, PropFindableResource, LockableResource {
    protected final CodeResourceFactory rf;
    private final String name;
    protected T wrapped;

    public AbstractCodeResource(CodeResourceFactory rf, String name, T wrapped ) {
        this.rf = rf;
        this.name = name;
        this.wrapped = wrapped;
    }

    public String getUniqueId() {
        return null;
    }

    public String getName() {
        return name;
    }

    public Object authenticate( String user, String password ) {
        return wrapped.authenticate( user, password );
    }

    public boolean authorise( Request request, Method method, Auth auth ) {
        return _(PermissionChecker.class).hasRole( Role.ADMINISTRATOR, wrapped, auth);
    }

    public String getRealm() {
        return wrapped.getRealm();
    }

    public Date getModifiedDate() {
        return wrapped.getModifiedDate();
    }

    public String checkRedirect( Request request ) {
        return null;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return ((DigestResource)wrapped).authenticate(digestRequest );
    }

    public boolean isDigestAllowed() {
        if(wrapped instanceof DigestResource) {
            return ((DigestResource)wrapped).isDigestAllowed();
        } else {
            return false;
        }
    }

    public Date getCreateDate() {
        if( wrapped instanceof PropFindableResource) {
            return ((PropFindableResource)wrapped).getCreateDate();
        } else {
            return null;
        }
    }

    public T getWrapped() {
        return wrapped;
    }

	public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException, PreConditionFailedException, LockedException {
		if( wrapped instanceof LockableResource) {
			LockableResource lr = (LockableResource) wrapped;
			return lr.lock(timeout, lockInfo);
		} else {
			throw new PreConditionFailedException(wrapped);
		}
	}

	public LockResult refreshLock(String token) throws NotAuthorizedException, PreConditionFailedException {
		if( wrapped instanceof LockableResource) {
			LockableResource lr = (LockableResource) wrapped;
			return lr.refreshLock(token);
		} else {
			throw new PreConditionFailedException(wrapped);
		}
	}

	public void unlock(String tokenId) throws NotAuthorizedException, PreConditionFailedException {
		if( wrapped instanceof LockableResource) {
			LockableResource lr = (LockableResource) wrapped;
			lr.unlock(tokenId);
		} else {
			throw new PreConditionFailedException(wrapped);
		}
	}

	public LockToken getCurrentLock() {
				if( wrapped instanceof LockableResource) {
			LockableResource lr = (LockableResource) wrapped;
			return lr.getCurrentLock();
		} else {
			throw new RuntimeException("Not implemented");
		}
	}
}
