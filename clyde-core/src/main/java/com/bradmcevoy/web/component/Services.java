package com.bradmcevoy.web.component;

import static com.ettrema.context.RequestContext.*;
import com.ettrema.mail.send.MailSender;

/**
 * Used by the MvelCommand class so that templates can access services
 * using bean syntax
 * 
 *
 * @author brad
 */
public class Services {
    public MailSender getEmail() {
        return _( MailSender.class);
    }
}
