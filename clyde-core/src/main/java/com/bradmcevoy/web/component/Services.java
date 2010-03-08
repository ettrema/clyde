package com.bradmcevoy.web.component;

import com.bradmcevoy.context.RequestContext;
import com.ettrema.mail.send.MailSender;

/**
 *
 * @author brad
 */
public class Services {
    public MailSender getEmail() {
        return RequestContext.getCurrent().get( MailSender.class);
    }
}
