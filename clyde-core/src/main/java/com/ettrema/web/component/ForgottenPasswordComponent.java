package com.ettrema.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.Component;
import com.ettrema.web.RenderContext;
import com.ettrema.web.RequestParams;
import com.ettrema.web.security.ForgottenPasswordHelper;
import java.util.Map;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * This component supports 2 styles of password recovery: a) sending the password
 * in clear text in an email and b) sending the user a link to a page which
 * allows them to reset their password
 *
 * Note that a) is simpler and might be suited to low security sites, but b)
 * is generally a better solution.
 *
 * @author brad
 */
public class ForgottenPasswordComponent implements Component {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ForgottenPasswordComponent.class );
    private static final long serialVersionUID = 1L;
    public static final String ATT_NAME_PARSEDEMAIL = "parsedEmail";
    private String name;
    private Addressable container;
    private String fromAdd;
    private String replyTo;
    private String subject;
    private String bodyTemplate;
    private String bodyTemplateHtml;
    private String thankyouPage;
    private boolean useToken;
    private String recaptchaComponent;

    public ForgottenPasswordComponent( Addressable container, String name ) {
        this.container = container;
        this.name = name;
    }

    public ForgottenPasswordComponent( Addressable container, Element el ) {
        this.container = container;
        fromXml( el );
    }

    @Override
    public void init( Addressable container ) {
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return container;
    }

    @Override
    public boolean validate( RenderContext rc ) {
        // Note: not used by this component
        return true;
    }

    @Override
    public String render( RenderContext rc ) {
        return "";
    }

    @Override
    public String renderEdit( RenderContext rc ) {
        return "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String onProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) throws NotAuthorizedException {
        log.debug( "onProcess" );
        ForgottenPasswordHelper helper = new ForgottenPasswordHelper();
        return helper.onProcess( this, rc, parameters, files );
    }

    @Override
    public void onPreProcess( RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        String email = parameters.get( "email" );
        log.debug( "email: " + email );
        rc.addAttribute( name + "_email", email );
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "component" );
        el.addContent( e2 );
        populateXml( e2 );
        return e2;
    }

    public final void fromXml( Element el ) {
        name = el.getAttributeValue( "name" );
        fromAdd = el.getAttributeValue( "from" );
        replyTo = el.getAttributeValue( "replyTo" );
        thankyouPage = el.getAttributeValue( "thankyouPage" );
        subject = el.getChildText( "subject" );
        bodyTemplate = el.getChildText( "body" );
        bodyTemplateHtml = JDomUtils.valueOf( el, name, Namespace.NO_NAMESPACE );
    }

    public void populateXml( Element e2 ) {
        e2.setAttribute( "class", getClass().getName() );
        populateXml_NoClass( e2 );
    }

    public void populateXml_NoClass( Element e2 ) {
        e2.setAttribute( "name", name );
        InitUtils.setString( e2, "from", fromAdd );
        InitUtils.setString( e2, "replyTo", replyTo );
        InitUtils.setString( e2, "thankyouPage", thankyouPage );

        Element elSubject = new Element( "subject" );
        e2.addContent( elSubject );
        elSubject.setText( subject );

        Element elBody = new Element( "body" );
        e2.addContent( elBody );
        elBody.setText( bodyTemplate );

        InitUtils.setElementString( e2, "html", bodyTemplateHtml );


    }

    public String getFromAdd() {
        return fromAdd;
    }

    public void setFromAdd( String fromAdd ) {
        this.fromAdd = fromAdd;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo( String replyTo ) {
        this.replyTo = replyTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject( String subject ) {
        this.subject = subject;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate( String bodyTemplate ) {
        this.bodyTemplate = bodyTemplate;
    }

    public String getThankyouPage() {
        return thankyouPage;
    }

    public void setThankyouPage( String thankyouPage ) {
        this.thankyouPage = thankyouPage;
    }

    /**
     * When true, users will be required to reset their password, rather then have
     * it emailed to them
     * @return
     */
    public boolean isUseToken() {
        return useToken;
    }

    public void setUseToken( boolean useToken ) {
        this.useToken = useToken;
    }

    public String getBodyTemplateHtml() {
        return bodyTemplateHtml;
    }

    public void setBodyTemplateHtml( String bodyTemplateHtml ) {
        this.bodyTemplateHtml = bodyTemplateHtml;
    }

    public String getRecaptchaComponent() {
        return recaptchaComponent;
    }

    public void setRecaptchaComponent( String recaptchaComponent ) {
        this.recaptchaComponent = recaptchaComponent;
    }

    public final void setValidationMessage( String s ) {
        RequestParams params = RequestParams.current();
        params.attributes.put( this.getName() + "_validation", s );
    }

    @Override
    public final String getValidationMessage() {
        RequestParams params = RequestParams.current();
        if( params != null ) {
            return (String) params.attributes.get( this.getName() + "_validation" );
        } else {
            return null;
        }
    }
}
