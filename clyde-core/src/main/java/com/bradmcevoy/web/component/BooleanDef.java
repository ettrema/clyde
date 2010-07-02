package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import java.util.Map;
import org.jdom.Element;

public class BooleanDef extends TextDef {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BooleanDef.class);
    private static final long serialVersionUID = 1L;
    private String type;

    public BooleanDef(Addressable container, String name) {
        super(container, name);
    }

    public BooleanDef(Addressable container, Element el) {
        super(container, el);
        type = InitUtils.getValue(el, "type");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean validate( ComponentValue c, RenderContext rc ) {
        return true;
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = super.toXml(container, el);
        InitUtils.set(e2, "type", type);
        return e2;
    }

    @Override
    public String render(ComponentValue c, RenderContext rc) {
        return formatValue(c.getValue());
    }

    @Override
    public void onPreProcess(ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        log.debug("onPreProcess");
        Path compPath = getPath( rc );
        String key = compPath.toString();
        if( !parameters.containsKey( key ) ) { // look for the hidden one because if not checked boolean wont be posted
            log.debug("onPreProcess: no hidden");
            return;
        }
        String s = parameters.get( key + "_val" );
        if( s == null || s.length() == 0) s = "false";
        log.debug("onPreProcess: s: " + s);
        Object value = parseValue( componentValue, rc.page, s );
        log.debug("onPreProcess: v: " + value);
        componentValue.setValue( value );

    }



    @Override
    public Boolean parseValue(ComponentValue cv, Templatable ct, String s) {
        if (s == null || s.trim().length() == 0) {
            return null;
        }
        try {
            return Boolean.parseBoolean(s);
        } catch (Exception e) {
            log.warn("Couldnt parse boolean: " + s);
            return null;
        }
    }

    public static Boolean parse(ComponentValue cv) {
        Object o = cv.getValue();
        if (o == null) {
            return null;
        }
        try {
            if( o instanceof Boolean ) {
                return (Boolean)o;
            } else {
                return Boolean.parseBoolean(o.toString());
            }
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public String formatValue(Object v) {
        if (v == null) {
            return "";
        } else if (v instanceof Boolean) {
            Boolean b = (Boolean) v;
            return b ? "yes" : "no";
        } else {
            String s = v.toString();
            return s;
        }
    }

    @Override
    protected String editChildTemplate() {
        String template = "<input type=\"checkbox\" name=\"${path}_val\" id=\"${path}\" value=\"true\" $val.checked />\n";
        template += "<input type='hidden' name=\"${path}\" id=\"${path}\" value=\"imhere\"/>";
        template += "#if($cv.validationMessage)";
        template += "<div class='validationError'>${cv.validationMessage}</div>";
        template += "#end";
        return template;
    }
}
