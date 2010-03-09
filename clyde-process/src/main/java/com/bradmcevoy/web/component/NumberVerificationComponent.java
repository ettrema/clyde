package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.process.ProcessDef;
import com.bradmcevoy.process.TokenValue;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.Templatable;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class NumberVerificationComponent extends VfsCommon implements Component, Addressable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( NumberVerificationComponent.class );
    private static final long serialVersionUID = 1L;
    private String name;
    private Addressable container;
    /**
     * The name of the process variable containing the actual verification number
     */
    private String actualNumberVarName;
    /**
     * The name of the process variable to set when verified
     */
    private String verifiedVarName;

    public NumberVerificationComponent( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }

    public NumberVerificationComponent( Addressable container, Element el ) {
        this.container = container;
        name = InitUtils.getValue( el, "name" );
        actualNumberVarName = InitUtils.getValue( el, "actualNumberVarName" );
        verifiedVarName = InitUtils.getValue( el, "verifiedVarName" );
    }

    public void populateXml( Element e2 ) {
        e2.setAttribute( "class", getClass().getName() );
        e2.setAttribute( "name", name );
        InitUtils.setString( e2, "actualNumberVarName", actualNumberVarName );
        InitUtils.setString( e2, "verifiedVarName", verifiedVarName );
    }

    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        log.debug( "process" );
        if( !parameters.containsKey( "verifyNumber" ) ) return null;

        log.debug( "process - number verification" );
        String sRequestedVerifyNumber = parameters.get( "verifyNumber" );
        int requestedVerifyNumber = 0;
        try {
            requestedVerifyNumber = Integer.parseInt( sRequestedVerifyNumber );
        } catch( NumberFormatException numberFormatException ) {
            log.debug( "verification failed type conversion: " + sRequestedVerifyNumber);
            RequestParams.current().getAttributes().put( "verificationFailed", Boolean.TRUE );
            return null;
        }
        Templatable targetPage = rc.getTargetPage();
        TokenValue token = (TokenValue) targetPage.getParent().getParent();
        int actualVerifyNumber = (Integer) token.getVariables().get( actualNumberVarName );
        if( requestedVerifyNumber == actualVerifyNumber ) {
            token.getVariables().put( verifiedVarName, Boolean.TRUE );
            token.save();
            if( !ProcessDef.scan( targetPage.getHost() ) ) {
                log.error( "Verified ok, but no transition occurred!!! - targetPage: " + targetPage.getHref() );
            } else {
                log.debug( "Verified ok, and process transitioned" );
                commit();
            }
            return token.getHref();
        } else {
            log.debug( "verification failed. actual: " + actualVerifyNumber + " requested: " + requestedVerifyNumber );
            RequestParams.current().getAttributes().put( "verificationFailed", Boolean.TRUE );
            return null;
        }
    }

    public void init( Addressable container ) {
        this.container = container;
    }

    public Addressable getContainer() {
        return container;
    }

    public boolean validate( RenderContext rc ) {
        return true;
    }

    public String render( RenderContext rc ) {
        return "";
    }

    public String renderEdit( RenderContext rc ) {
        return "";
    }

    public String getName() {
        return name;
    }

    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
    }

    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "component" );
        el.addContent( e2 );
        populateXml( e2 );
        return e2;
    }

    public Path getPath() {
        return container.getPath().child( name );
    }
}
