package com.ettrema.web.security;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.IUser;
import com.ettrema.web.User;
import com.ettrema.mail.MailboxAddress;

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
    private final UserLocator userLocator = new UserLocator();
    private final ClydeAuthenticator wrapped;

    public EmailAuthenticator(ClydeAuthenticator wrapped) {
        this.wrapped = wrapped;
    }

    public IUser authenticate(Resource resource, String userName, String password) {
        if (resource instanceof CommonTemplated) {
            MailboxAddress email = userLocator.parse(userName);
            if (email != null) {
                User user = userLocator.findUserByEmail(email, (CommonTemplated) resource);
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
            MailboxAddress email = userLocator.parse(digestRequest.getUser());
            if (email != null) {
                User user = userLocator.findUserByEmail(email, (CommonTemplated) resource);
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

    public UserLocator getUserLocator() {
        return userLocator;
    }        
}
