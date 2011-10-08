package com.bradmcevoy.web.code;

import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
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
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import java.util.Date;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class AbstractCodeResource<T extends Resource> implements Resource, DigestResource, PropFindableResource, LockableResource, DeletableResource {
    protected final CodeResourceFactory rf;
    private final String name;
    protected T wrapped;

    public AbstractCodeResource(CodeResourceFactory rf, String name, T wrapped ) {
        this.rf = rf;
        this.name = name;
        this.wrapped = wrapped;
    }

	@Override
    public String getUniqueId() {
        return null;
    }

	@Override
    public String getName() {
        return name;
    }

	@Override
    public Object authenticate( String user, String password ) {
        return wrapped.authenticate( user, password );
    }

	@Override
    public boolean authorise( Request request, Method method, Auth auth ) {
        return _(PermissionChecker.class).hasRole( Role.ADMINISTRATOR, wrapped, auth);
    }

	@Override
    public String getRealm() {
        return wrapped.getRealm();
    }

	@Override
    public Date getModifiedDate() {
        return wrapped.getModifiedDate();
    }

	@Override
    public String checkRedirect( Request request ) {
        return null;
    }

	@Override
    public Object authenticate( DigestResponse digestRequest ) {
        return ((DigestResource)wrapped).authenticate(digestRequest );
    }

	@Override
    public boolean isDigestAllowed() {
        if(wrapped instanceof DigestResource) {
            return ((DigestResource)wrapped).isDigestAllowed();
        } else {
            return false;
        }
    }

	@Override
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

	@Override
	public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException, PreConditionFailedException, LockedException {
		if( wrapped instanceof LockableResource) {
			LockableResource lr = (LockableResource) wrapped;
			return lr.lock(timeout, lockInfo);
		} else {
			throw new PreConditionFailedException(wrapped);
		}
	}

	@Override
	public LockResult refreshLock(String token) throws NotAuthorizedException, PreConditionFailedException {
		if( wrapped instanceof LockableResource) {
			LockableResource lr = (LockableResource) wrapped;
			return lr.refreshLock(token);
		} else {
			throw new PreConditionFailedException(wrapped);
		}
	}

	@Override
	public void unlock(String tokenId) throws NotAuthorizedException, PreConditionFailedException {
		if( wrapped instanceof LockableResource) {
			LockableResource lr = (LockableResource) wrapped;
			lr.unlock(tokenId);
		} else {
			throw new PreConditionFailedException(wrapped);
		}
	}

	@Override
	public LockToken getCurrentLock() {
				if( wrapped instanceof LockableResource) {
			LockableResource lr = (LockableResource) wrapped;
			return lr.getCurrentLock();
		} else {
			throw new RuntimeException("Not implemented");
		}
	}

	@Override
	public final void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        if( wrapped instanceof DeletableResource ) {
            try {
                ( (DeletableResource) wrapped ).delete();
            } catch( NotAuthorizedException ex ) {
                throw new RuntimeException( ex );
            } catch( ConflictException ex ) {
                throw new RuntimeException( ex );
            } catch( BadRequestException ex ) {
                throw new RuntimeException( ex );
            }
        } else {
            throw new RuntimeException( "Need to replace current resource, but its not deletable: " + wrapped.getClass() );
        }
	}
}
