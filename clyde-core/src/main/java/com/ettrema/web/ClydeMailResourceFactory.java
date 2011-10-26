package com.ettrema.web;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.mail.MailResourceFactory;
import com.ettrema.mail.Mailbox;
import com.ettrema.mail.MailboxAddress;

/**
 *
 * @author brad
 */
public class ClydeMailResourceFactory implements MailResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeMailResourceFactory.class);

    private final ResourceFactory wrapped;

    public ClydeMailResourceFactory( ResourceFactory wrapped ) {
        this.wrapped = wrapped;
        log.debug( "created ClydeMailResourceFactory, wrapping: " + wrapped.getClass().getCanonicalName());
    }

    


    @Override
    public Mailbox getMailbox( MailboxAddress add ) {
        log.debug("getMailbox: " + add);
        Resource res = wrapped.getResource(add.domain, "users/" + add.user);
        if( res == null ) {
            log.debug("mailbox not found: " + add);
            return null;
        }

        if( res instanceof Mailbox ) {
            Mailbox user = (Mailbox) res;
            return user;
        } else {
            log.warn("resource exists but does not implement Mailbox: " + res.getClass());
            return null;
        }
    }

}
