package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.ettrema.utils.JDomUtils;
import com.bradmcevoy.utils.XmlUtils2;
import com.ettrema.web.Component;
import com.bradmcevoy.xml.XmlHelper;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

public class InitUtils {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InitUtils.class);

    public static boolean getBoolean(Element el, String name) {
        String s = el.getAttributeValue(name);
        if (s == null) {
            return false;
        }
        return s.equals("true");
    }

    public static Boolean getNullableBoolean(Element el, String name) {
        String s = el.getAttributeValue(name);
        if (s == null) {
            return null;
        }
        return s.equals("true");
    }

    public static int getInt(Element el, String name) {
        String s = getValue(el, name);
        if (s == null || s.length() == 0) {
            return 0;
        }
        return Integer.parseInt(s);
    }

    public static Integer getInteger(Element el, String name) {
        String s = getValue(el, name);
        if (s == null || s.length() == 0) {
            return null;
        }
        return Integer.parseInt(s);
    }

    public static void set(Element el, String name, Path p) {
        if (p == null) {
            set(el, name, (String) null);
        } else {
            set(el, name, p.toString());
        }
    }

    public static Path getPath(Element el, String name) {
        String s = getValue(el, name);
        if (s == null || s.length() == 0) {
            return null;
        }
        return Path.path(s);
    }

    public static void init(BooleanInput in, Element el) {
        String s = el.getAttributeValue(in.getName());
        if (s == null) {
            in.setValue(false);
        } else {
            in.setValue(s.equals("true"));
        }
    }

    public static void set(Element el, String name, int anInt) {
        el.setAttribute(name, anInt + "");
    }

    public static void set(Element el, String name, Integer anInteger) {
        if (anInteger == null) {
            el.removeAttribute(name);
        } else {
            el.setAttribute(name, anInteger.toString());
        }
    }

    public static void set(Element el, String name, Long anInteger) {
        if (anInteger == null) {
            el.removeAttribute(name);
        } else {
            el.setAttribute(name, anInteger.toString());
        }
    }

    public static void set(Element el, String name, BigDecimal v) {
        if (v == null) {
            el.removeAttribute(name);
        } else {
            el.setAttribute(name, v.toPlainString());
        }
    }

    public static void set(Element e2, String name, Boolean b) {
        setBoolean(e2, name, b);
    }

    public static void setBoolean(Element e2, String name, Boolean b) {
        if (b == null) {
            e2.removeAttribute(name);
        } else {
            e2.setAttribute(name, b + "");
        }
    }

    public static void setLong(Element e2,String name, Long l) {
        if (l == null) {
            e2.removeAttribute(name);
        } else {
            e2.setAttribute(name, l + "");
        }        
    }    
    
    public static String getValue(Element el, String name, String def) {
        String s = getValue(el, name);
        if (s == null) {
            return def;
        }
        return s;
    }

    public static String getValue(Element el, String name) {
        Attribute att = el.getAttribute(name);
        if (att == null) {
            return null;
        }
        return att.getValue().trim();
    }

    public static void setString(Element el, AbstractInput input) {
        String s = "";
        if (input != null) {
            s = input.getFormattedValue();
        }
        setString(el, input.getName(), s);

    }

    public static void set(Element el, String name, String val) {
        setString(el, name, val);
    }

    public static void setString(Element el, String name, String val) {
        if (StringUtils.isEmpty(val)) {
            el.removeAttribute(name);
        } else {
            el.setAttribute(name, val);
        }
    }

    public static void toXml(String name, BooleanInput in, Element e2) {
        String s = "";
        if (in != null && in.getValue() != null) {
            s = in.getValue() + "";
        } else {
            s = "false";
        }
        e2.setAttribute(name, s);
    }

    public static void componentFieldsToXml(Object parent, Element e2) {
        Map<String, Field> mapOfFields = componentFields(parent);
        log.debug("componentFieldsToXml: " + mapOfFields.size());
        for (Field f : mapOfFields.values()) {
            Component c = getComponent(f, parent);
            c.toXml((Addressable) parent, e2);
        }
    }

    public static List<String> getList(Element el, String name) {
        String s = getValue(el, name);
        if (s == null || s.length() == 0) {
            return null;
        }
        String[] arr = s.split(",");
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(arr));
        return list;
    }

    public static Element addChild(Element e2, String elementName, String text) {
        Element el = new Element(elementName);
        e2.addContent(el);
        el.setText(text);
        return el;
    }

    /**
     *
     * @param el
     * @param name
     * @return - the text of the given child
     */
    public static String getChild(Element el, String name) {
        Element e2 = el.getChild(name);
        if (e2 == null) {
            return null;
        }
        return e2.getText();
    }

    public static Element getChildElement(Element elParent, String name, Namespace ns) {
        Element el;
        if (ns == null) {
            el = elParent.getChild(name);
        } else {
            el = elParent.getChild(name, ns);
        }
        return el;
    }

    public static String getValueOf(Element el, String name) {
        Element elChild = JDomUtils.getChild(el, name);
        if (elChild == null) {
            return null;
        } else {
            return getValue(elChild);
        }
    }

    /**
     * Gets all child content as xml, or the value attribute if present
     *
     * @param el
     * @return
     */
    public static String getValue(Element el) {
        Attribute att = el.getAttribute("value");
        String v = null;
        if (att != null) {
            v = att.getValue();
        } else {
            v = XmlHelper.getAllText(el);
        }
        if (v == null) {
            return null;
        }
        return v.trim();
    }

    static Map<String, Component> initChildComponents(Element el, Addressable parent) {
        Map<String, Component> map = new HashMap<String, Component>();
        for (Object o : el.getChildren()) {
            Element elComp = (Element) o;
            Component c = (Component) XmlUtils2.restoreObject(elComp, parent);
            map.put(c.getName(), c);
        }
        return map;
    }

    static void initComponentFields(Element el, Addressable parent) {
        Map<String, Component> map = initChildComponents(el, parent);
        Map<String, Field> mapOfFields = componentFields(parent);
        for (String s : map.keySet()) {
            Field f = mapOfFields.get(s);
            if (f != null) {
                setField(f, map.get(s), parent);
            }
        }
    }

    static Map<String, Field> componentFields(Object parent) {
        Map<String, Field> map = new HashMap<String, Field>();
        for (Field f : parent.getClass().getDeclaredFields()) {
            if (Component.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                map.put(f.getName(), f);
            }
        }
        return map;
    }

    static void setField(Field f, Component c, Object parent) {
        try {
            f.set(parent, c);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Failed set component on field: " + f.getName(), ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Failed set component on field: " + f.getName(), ex);
        }
    }

    public static Component getComponent(Field f, Object parent) {
        try {
            return (Component) f.get(parent);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Failed set component on field: " + f.getName(), ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Failed set component on field: " + f.getName(), ex);
        }
    }

    public static void setList(Element e2, String name, List<String> choices) {
        StringBuilder sb = new StringBuilder();
        if (choices != null) {
            boolean first = true;
            for (String s : choices) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(s);
            }
        }
        setString(e2, name, sb.toString());
    }

    public static BigDecimal getBigDecimal(Element el, String name) {
        String s = getValue(el, name);
        if (s == null || s.length() == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(s);
    }

    /**
     * sets the given text into a child element of the given name
     *
     * the value element is first attempted to be parsed into an xml representation
     * which is set into the xml dom.
     *
     * If that fails the entire text is wrapped in a CDATA element.
     *
     * @param el
     * @param name
     * @param value
     */
    public static void setElementString(Element el, String name, String value) {
        Element child = el.getChild(name);
        if (child != null) {
            el.removeChild(name);
        }

        child = new Element(name);
        List content = XmlHelper.getContent(value);
        child.setContent(content);

        el.addContent(child);
    }

    /**
     * returns the textual value associated with the child element of the given name
     * @param el
     * @param name
     * @return
     */
    public static String getElementValue(Element elParent, String name) {
        Element el = elParent.getChild(name);
        if (el == null) {
            return null;
        }
        return XmlHelper.getAllText(el);
    }



}
