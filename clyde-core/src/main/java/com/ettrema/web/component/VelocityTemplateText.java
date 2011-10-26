package com.ettrema.web.component;

import com.bradmcevoy.http.FileItem;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.RenderContext;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class VelocityTemplateText extends Text implements WrappableComponent {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VelocityTemplateText.class);

    private static final long serialVersionUID = 1L;

    public VelocityTemplateText(Addressable container,String name) {
        super(container,name);
        setValue("");
    }

    public VelocityTemplateText(Addressable container, Element el) {
        super(container,el);
    }

    @Override
    public String render(RenderContext rc) {
        String t = this.getValue();
        if( t == null || t.length() == 0 ) {
            log.warn("no template");
            return "";
        }

        VelocityContext vc = velocityContext(rc, this);
        return _render(t, vc);
    }

    @Override
    public void onPreProcess( Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {

    }

    @Override
    public String onProcess( Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files ) {
        return null;
    }

    @Override
    public String render( Addressable container, RenderContext rc ) {
        return this.render( rc );
    }

    @Override
    public String renderEdit( Addressable container, RenderContext rc ) {
        return "";
    }

    @Override
    public boolean validate( Addressable container, RenderContext rc ) {
        return true;
    }

    @Override
    public Object getValue( Addressable container ) {
        return getFormattedValue( container );
    }

    @Override
    public String getFormattedValue( Addressable container ) {
        CommonTemplated ct = (CommonTemplated) container;
        RenderContext rc = new RenderContext( ct.getTemplate(), ct, null, false);
        return this.render( rc );
    }
}
