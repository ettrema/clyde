package com.ettrema.web.component;

import com.ettrema.logging.LogUtils;
import com.ettrema.web.RenderContext;
import com.ettrema.web.velocity.VelocityInterpreter;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

public class HtmlInput extends Text {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HtmlInput.class);
    
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
            LogUtils.trace(log, "render: templating disabled so use content as is", template);
            return template;
        } else {
            VelocityContext vc = new VelocityContext();
            vc.put( "rc", rc );
            LogUtils.trace(log, "render: using template", template);
            String s = VelocityInterpreter.evalToString( template, vc );
            LogUtils.trace(log, "render: evaluated template to", s);
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
