package com.bradmcevoy.web.component;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.User;
import com.bradmcevoy.web.security.EmailAuthenticator;
import com.ettrema.mail.MailboxAddress;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class EmailDef extends TextDef {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( EmailDef.class );
    private static final long serialVersionUID = 1L;

    public EmailDef( Addressable container, String name ) {
        super( container, name );
    }

    public EmailDef( Addressable container, Element el ) {
        super( container, el );
    }

    @Override
    public String render( ComponentValue c, RenderContext rc ) {
        BaseResource res = (BaseResource) c.getContainer();
        return res.getExternalEmailTextV2( "default" );
//        return formatValue(c.getValue());
    }

    @Override
    public String parseValue( ComponentValue cv, Templatable ct, String s ) {
        return s;
    }

    @Override
    public String formatValue( Object v ) {
        if( v == null ) {
            return "";
        } else {
            return v.toString();
        }
    }

    @Override
    public ComponentValue createComponentValue( Templatable newPage ) {
        EmailVal cv = new EmailVal( name.getValue(), newPage );
        return cv;
    }

    @Override
    public boolean validate( ComponentValue c, RenderContext rc ) {
        boolean b = super.validate( c, rc );
        if( !b ) return false;
        String s = (String) c.getValue();
        if( s == null ) return true;
        s = s.trim();
        if( s.length() == 0 ) return true;

        try {
            MailboxAddress add = MailboxAddress.parse( s );
            User currentUser = (User) c.getContainer();
            User foundUser = _( EmailAuthenticator.class ).findUser( add, currentUser );
            if( foundUser == null ) {
                return true;
            } else {
                boolean isSameUser = foundUser.is( currentUser );
                if( !isSameUser){
                    c.setValidationMessage( "That email address is already associated with a user account. Please use the lost password function or enter a new address");
                }
                return isSameUser;
            }
        } catch( IllegalArgumentException illegalArgumentException ) {
            c.setValidationMessage( "Not a valid email address" );
            return false;
        }
    }
}
