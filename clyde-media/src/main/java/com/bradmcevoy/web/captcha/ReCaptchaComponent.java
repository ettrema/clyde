package com.bradmcevoy.web.captcha;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.ValidatingComponent;
import java.io.Serializable;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class ReCaptchaComponent implements Component, Serializable, ValidatingComponent {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ReCaptchaComponent.class );
    private static final long serialVersionUID = 1L;
    private String name;
    private Addressable container;
    private String privateKey;
    private String publicKey;

    public ReCaptchaComponent( Addressable container, Element el ) {
        this.container = container;
        this.name = InitUtils.getValue( el, "name" );
        this.privateKey = InitUtils.getValue( el, "privateKey" );
        this.publicKey = InitUtils.getValue( el, "publicKey" );
    }

    public ReCaptchaComponent( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }

    public void init( Addressable container ) {
        this.container = container;
    }

    public Addressable getContainer() {
        return container;
    }

    public boolean validate( RenderContext rc ) {
        ReCaptchaHelper helper = new ReCaptchaHelper( this, rc, HttpManager.request().getParams() );
        boolean b = helper.validate();
        if( log.isTraceEnabled() ) {
            log.debug( "validate: " + b );
        }
        return b;
    }

    public void setValidationMessage( String s ) {
        RequestParams params = RequestParams.current();
        params.attributes.put( this.getName() + "_validation", s );
    }

    public String getValidationMessage() {
        RequestParams params = RequestParams.current();
        return (String) params.attributes.get( this.getName() + "_validation" );
    }

    public String render( RenderContext rc ) {
        log.debug( "render" );
        return "";
    }

    public String renderEdit( RenderContext rc ) {
        return "";
    }

    public String getName() {
        return name;
    }

    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        return null;
    }

    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        log.debug( "onPreProces" );
    }

    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "component" );
        el.addContent( e2 );
        InitUtils.setString( e2, "name", name );
        InitUtils.setString( e2, "privateKey", privateKey );
        InitUtils.setString( e2, "publicKey", publicKey );
        InitUtils.setString( e2, "class", this.getClass().getName() );
        return e2;

    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey( String privateKey ) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey( String publicKey ) {
        this.publicKey = publicKey;
    }
}
