package com.bradmcevoy.web.component;

import com.bradmcevoy.web.RenderContext;
import com.ettrema.mail.MailboxAddress;
import org.jdom.Element;

public class EmailInput extends Text {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmailInput.class);
    
    private static final long serialVersionUID = 1L;
    
    public EmailInput(Addressable container, String name) {
        super(container, name);
    }

    public EmailInput(Addressable container, Element el) {
        super(container, el);
    }

    @Override
    public boolean validate(RenderContext rc) {
        log.debug( "validate: " + getValue());
        try {
            boolean b = super.validate(rc);
            if (!b) {
                return b;
            }
            String s = getValue();
            log.debug( "validating: " + s);
            if (s != null) {
                s = s.trim();
                if (s.length() > 0) {
                    MailboxAddress add = MailboxAddress.parse( s );
                    log.debug( "address ok: " + add);
                }
            } else {
                log.debug("value is null");
            }
            return true;
        } catch (IllegalArgumentException ex) {
            log.debug("invalid address");
            this.validationMessage = "Not a valid email address";
            return false;
        }
    }
}
