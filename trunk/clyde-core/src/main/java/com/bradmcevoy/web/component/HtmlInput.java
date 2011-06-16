package com.bradmcevoy.web.component;

import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.velocity.VelocityInterpreter;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

public class HtmlInput extends Text {

    private static final long serialVersionUID = 1L;
    private Boolean disAllowTemplating;

    public HtmlInput( Addressable container, String name ) {
        super( container, name );
        this.disAllowTemplating = Boolean.FALSE;
    }

    public HtmlInput( Addressable container, String name, String value ) {
        this( container, name );
        setValue( value );
    }

    public HtmlInput( Addressable container, Element el ) {
        super( container, el );
    }

    @Override
    protected String editTemplate() {
        String template;
        template = "<textarea name='${path}' rows='${input.rows}' cols='${input.cols}'>${formattedValue}</textarea>";
        return template;
    }

    @Override
    public String render( RenderContext rc ) {
        String template = getValue();
        // String s = TemplateInterpreter.evalToString(template,rc);
        if( isDisAllowTemplating() ) {
            return template;
        } else {
            VelocityContext vc = new VelocityContext();
            vc.put( "rc", rc );
            String s = VelocityInterpreter.evalToString( template, vc );
            return s;
        }
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = super.toXml( container, el );
        InitUtils.setBoolean( e2, "disAllowTemplating", isDisAllowTemplating() );
        return e2;
    }

    @Override
    public void fromXml( Element el ) {
        super.fromXml( el );
        disAllowTemplating = InitUtils.getBoolean( el, "disAllowTemplating" );
    }

    public boolean isDisAllowTemplating() {
        if(disAllowTemplating == null ) {
            return false;
        } else {
            return disAllowTemplating;
        }
    }

    public void setDisAllowTemplating( boolean disAllowTemplating ) {
        this.disAllowTemplating = disAllowTemplating;
    }
}
