package com.bradmcevoy.web.security;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.EmailAddress;
import com.bradmcevoy.web.Host;
import com.bradmcevoy.web.IUser;
import com.bradmcevoy.web.User;
import com.ettrema.context.RequestContext;
import com.ettrema.mail.MailboxAddress;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.List;

/**
 * If the user name is in the form of an email address, this will attempt to
 * locate a matching user record which is the most specifically associated
 * with the domain name in the email address
 *
 * If the user name is not in the form of an email address, processing falls
 * through to the wrapped authenticator
 *
 * @author brad
 */
public class EmailAuthenticator implements ClydeAuthenticator {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmailAuthenticator.class);
    private final ClydeAuthenticator wrapped;

    public EmailAuthenticator(ClydeAuthenticator wrapped) {
        this.wrapped = wrapped;
    }

    public IUser authenticate(Resource resource, String userName, String password) {
        if (resource instanceof CommonTemplated) {
            MailboxAddress email = parse(userName);
            if (email != null) {
                User user = findUser(email, (CommonTemplated) resource);
                if (user == null) {
                    log.trace("user not found");
                    return null;
                } else {
                    if (user.checkPassword(password)) {
                        log.trace("authentication ok");
                        return user;
                    } else {
                        log.trace("user found, but passwords don't match");
                        return null;
                    }
                }
            }
        }
        return wrapped.authenticate(resource, userName, password);
    }

    public IUser authenticate(Resource resource, DigestResponse digestRequest) {
        if (resource instanceof CommonTemplated) {
            MailboxAddress email = parse(digestRequest.getUser());
            if (email != null) {
                User user = findUser(email, (CommonTemplated) resource);
                if (user == null) {
                    return null;
                } else {
                    if (user.checkPassword(digestRequest)) {
                        return user;
                    } else {
                        return null;
                    }
                }
            }
        }

        return wrapped.authenticate(resource, digestRequest);
    }

    private MailboxAddress parse(String user) {
        if (user.contains("@")) {
            try {
                MailboxAddress add = MailboxAddress.parse(user);
                return add;
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Given a starting (ie current) resource and an email address, attempt to
     * locate a matching user. The user account must be defined on the same domain
     * as the given resource
     *
     * @param add
     * @param currentResource
     * @return
     */
    public User findUser(MailboxAddress add, CommonTemplated currentResource) {
        List<User> foundUsers = findMatchingUsers(add);
        for (User user : foundUsers) {
            if (isMatchingDomain(user, currentResource.getHost())) {
                return user;
            }
        }
        return null;
    }

    private List<User> findMatchingUsers(MailboxAddress add) {
        VfsSession vfs = RequestContext.getCurrent().get(VfsSession.class);
        List<NameNode> list = vfs.find(EmailAddress.class, add.toPlainAddress());
        List<User> foundUsers = new ArrayList<User>();
        if (list == null || list.isEmpty()) {
            log.debug("no nodes found");
        } else {
            for (NameNode node : list) {
                NameNode nUser = node.getParent().getParent(); // the first parent is just a holder
                DataNode dnUser = nUser.getData();
                if (dnUser != null && dnUser instanceof User) {
                    User user = (User) dnUser;
                    foundUsers.add(user);
                } else {
                    log.warn("parent is not a user: " + dnUser.getClass());
                }
            }
        }
        return foundUsers;
    }

    private boolean isMatchingDomain(User user, Host h) {
        if (h == null) {
            return false;
        }
        if (user.getHost().getNameNodeId().equals(h.getNameNodeId())) {
            return true;
        }
        return isMatchingDomain(user, h.getParentHost());
    }
}
