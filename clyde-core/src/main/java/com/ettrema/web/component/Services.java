package com.ettrema.web.component;

import static com.ettrema.context.RequestContext.*;
import com.ettrema.mail.send.MailSender;
import com.ettrema.web.Formatter;

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
	public Formatter getFormatter() {
		return _( Formatter.class);
	}
}
