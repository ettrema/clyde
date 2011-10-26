package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import java.util.Map;
import org.jdom.Element;

public class BooleanDef extends TextDef {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BooleanDef.class);
    private static String MARKER_VALUE = "Bool";
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

    /**
     * If required, this means that the value must be true
     *
     * @param c
     * @param rc
     * @return
     */
    @Override
    public boolean validate(ComponentValue c, RenderContext rc) {
        if (isRequired()) {
            Boolean bVal = parse(c);
            if( bVal == null ) {
                setValidationMessage(c,"A value is required");
                return false;
            } else if( !bVal.booleanValue() ) {
                setValidationMessage(c,"Must be checked");
                return false;
            }
        }
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
        Path compPath = getPath(rc);
        String key = compPath.toString();
        if (!parameters.containsKey(key)) { // look for the hidden one because if not checked boolean wont be posted
            log.debug("onPreProcess: no hidden");
            return;
        }
        String s = parameters.get(key); // if not using checkbox we can get the value directly
        if( s.length() == 0 || s.equals(MARKER_VALUE) ) { // if marker has been sent then look for other value
            s = parameters.get(key + "_val");
        }
        if (s == null || s.length() == 0) {
            s = "false";
        }
        Object value = parseValue(componentValue, rc.page, s);
        componentValue.setValue(value);

    }

    @Override
    public Boolean parseValue(ComponentValue cv, Templatable ct, String s) {
        if (s == null || s.trim().length() == 0) {
            log.trace("parseValue: empty text");
            return null;
        }
        try {
            Boolean b = Boolean.parseBoolean(s);
            if (log.isTraceEnabled()) {
                log.trace("parsed: " +s  + " --> " + b);
            }

            return b;
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
            if (o instanceof Boolean) {
                return (Boolean) o;
            } else {
                Boolean b = Boolean.parseBoolean(o.toString());
                if (log.isTraceEnabled()) {
                    log.trace("parsed: " + o.toString() + " --> " + b);
                }
                return b;
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
            return b ? "true" : "false";
        } else {
            String s = v.toString();
            return s;
        }
    }

    @Override
    protected String editChildTemplate() {
        String template = "<input type=\"checkbox\" name=\"${path}_val\" id=\"${path}\" value=\"true\" $val.checked />\n";
        template += "<input type='hidden' name=\"${path}\" id=\"${path}\" value=\"" + MARKER_VALUE + "\"/>";
        template += "#if($cv.validationMessage)";
        template += "<div class='validationError'>${cv.validationMessage}</div>";
        template += "#end";
        return template;
    }
}
