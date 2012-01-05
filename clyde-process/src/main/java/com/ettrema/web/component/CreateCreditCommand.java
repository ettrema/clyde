package com.ettrema.web.component;

import com.ettrema.pay.CreditManager;
import com.bradmcevoy.http.FileItem;
import com.ettrema.web.Host;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import java.math.BigDecimal;
import java.util.Map;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 *
 */
public class CreateCreditCommand extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CreateCreditCommand.class );
    private static final long serialVersionUID = 1L;
    private BigDecimal amount;

    public CreateCreditCommand( Addressable container, String name ) {
        super( container, name );
    }

    public CreateCreditCommand( Addressable container, Element el ) {
        super( container, el );
        amount = InitUtils.getBigDecimal( el, "amount" );
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = super.toXml( container, el );
        InitUtils.set( e2, name, amount );
        return e2;
    }

    public boolean validate( RenderContext rc ) {
        return ComponentUtils.validateComponents( this, rc );
    }

    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        String s = parameters.get( this.getName() );
        if( s == null ) {
            return null; // not this command
        }
        createCredit( rc.getTargetPage() );

        return null;
    }

    @Override
    protected String doProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    private void createCredit( Templatable targetPage ) {
        Host host = targetPage.getHost();
        if( host == null ) {
            throw new RuntimeException( "no host for: " + targetPage.getName() );
        }
		_(CreditManager.class).createCredit(host, amount, "TEST",  "test description");
		host.commit();
    }
}
