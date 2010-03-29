package com.bradmcevoy.web.component;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import com.ettrema.mail.MailboxAddress;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class EmailDef extends TextDef{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmailDef.class);

    private static final long serialVersionUID = 1L;

    public EmailDef(Addressable container,String name) {
        super(container,name);
    }

    public EmailDef(Addressable container, Element el) {
        super(container,el);
    }

    @Override
    public String render(ComponentValue c,RenderContext rc) {
        BaseResource res = (BaseResource) c.getContainer();
        return res.getExternalEmailTextV2( "default");
//        return formatValue(c.getValue());
    }

    @Override
    public String parseValue(ComponentValue cv, Templatable ct,String s) {
        return s;
    }

    @Override
    public String formatValue(Object v) {
        if( v == null ) {
            return "";
        } else {
            return v.toString();
        }
    }

    @Override
    public ComponentValue createComponentValue(Templatable newPage) {
        log.debug( "createComponentValue");
        EmailVal cv = new EmailVal(name.getValue(), newPage);
        return cv;
    }

    @Override
    public boolean validate( ComponentValue c, RenderContext rc ) {
        log.debug( "validate");
        boolean b = super.validate( c, rc );
        if( !b ) return false;
        String s = (String) c.getValue();
        if( s == null ) return true;
        s = s.trim();
        if( s.length() == 0 ) return true;

        try {
            MailboxAddress.parse( s );
            log.debug( "ok");
            return true;
        } catch( IllegalArgumentException illegalArgumentException ) {
            log.debug( "invalid");
            c.setValidationMessage( "Not a valid email address");
            return false;
        }
    }



}
