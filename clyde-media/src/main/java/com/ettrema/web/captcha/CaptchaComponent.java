package com.ettrema.web.captcha;

import static com.ettrema.context.RequestContext.*;
import com.bradmcevoy.http.FileItem;
import com.ettrema.web.Component;
import com.ettrema.web.RenderContext;
import com.ettrema.web.RequestParams;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.ValidatingComponent;
import java.io.Serializable;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CaptchaComponent implements Component, Serializable, ValidatingComponent {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CaptchaComponent.class );
    private static final long serialVersionUID = 1L;
    static final String PARAM_CHALLENGE = "captcha_challenge";
    static final String PARAM_RESPONSE = "captcha_response";
    private String name;
    private Addressable container;
    private String cssClass;
    private String cssStyle;

    public CaptchaComponent( Addressable container, Element el ) {
        this.container = container;
        this.name = InitUtils.getValue( el, "name" );
        this.cssClass = InitUtils.getValue(el,"cssClass");
        this.cssStyle = InitUtils.getValue(el,"cssStyle");
    }

    public CaptchaComponent( Addressable container, String name ) {
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
        log.debug( "validate" );
        RequestParams params = RequestParams.current();
        String ch = (String) params.attributes.get( PARAM_CHALLENGE );
        if( ch == null ) {
            setValidationMessage( "no challenge" );
            return false;
        }
        String resp = (String) params.attributes.get( PARAM_RESPONSE );
        if( resp == null ) {
            setValidationMessage( "please enter the captcha number" );
            return false;
        }

        CaptchaService svc = _( CaptchaService.class );
        if( svc == null ) {
            throw new RuntimeException( "no captachservice is configured" );
        }

        if( !svc.validateResponse( ch, resp ) ) {
            setValidationMessage( "failed to validate the captcha number. please try again" );
            return false;
        }
        return true;
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
        CaptchaService svc = _( CaptchaService.class );
        if( svc == null ) {
            throw new RuntimeException( "no captachservice is configured" );
        }
        RequestParams params = RequestParams.current();
        String ch = (String) params.attributes.get( PARAM_CHALLENGE );
        String resp = (String) params.attributes.get( PARAM_RESPONSE );
        if( ch == null ) {
            ch = svc.getChallenge();
        } else {
            if( !svc.isValidChallenge(ch) ) {
                ch = svc.getChallenge(); 
            }
        }

        rc.addAttribute( PARAM_CHALLENGE, ch );
        if( resp == null ) resp = "";
        String html = "<input type='hidden' name='" + PARAM_CHALLENGE + "' id='" + PARAM_CHALLENGE + "' value='" + ch + "'/>";
        html += "<input type='text' name='" + PARAM_RESPONSE + "' id='" + PARAM_RESPONSE + "' value='" + resp + "' ";
        if( cssClass != null ) {
            html += " class='" + cssClass + "' ";
        }
        if( cssStyle != null ) {
            html += " style='" + cssStyle + "' ";
        }
        html += "/>";
        String v = getValidationMessage();
        if( v != null ) {
            html += "<font color='red'>" + v + "</font>";
        }
        return html;
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
        RequestParams params = RequestParams.current();
        String ch = parameters.get( PARAM_CHALLENGE );
        params.attributes.put( PARAM_CHALLENGE, ch );
        String resp = parameters.get( PARAM_RESPONSE );
        params.attributes.put( PARAM_RESPONSE, resp );
    }

    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "component" );
        el.addContent( e2 );
        InitUtils.setString( e2, "name", name );
        InitUtils.setString( e2, "cssClass", cssClass );
        InitUtils.setString( e2, "style", cssStyle );
        InitUtils.setString( e2, "class", this.getClass().getName() );
        return e2;

    }

    private String href( String challenge ) {
        return container.getName() + "/" + name + "?" + PARAM_CHALLENGE + "=" + challenge;
    }
}
