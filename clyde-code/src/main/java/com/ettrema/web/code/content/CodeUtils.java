package com.ettrema.web.code.content;

import com.ettrema.web.ITemplate;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Page;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.ComponentValue;
import com.bradmcevoy.xml.XmlHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.MyXmlOutputter;

import com.ettrema.vfs.VfsTransactionManager;

/**
 *
 * @author brad
 */
public class CodeUtils {

    public static void appendValue(Page page, ComponentValue cv, Document doc) {
        String v = getFormattedValue(page, cv);
        List content = XmlHelper.getContent(v);
        if (content == null || content.isEmpty()) {
            Element elRoot = new Element("html");
            doc.setRootElement(elRoot);
        } else if (content.size() == 1) {
            doc.setRootElement((Element) content.get(0));
        } else {
            Element elRoot = new Element("multipleRootElements-EEK");
            elRoot.addContent(content);
            doc.setRootElement(elRoot);
        }
    }

    public static void appendValue(Page page, ComponentValue cv, Element e2) {
        String v = getFormattedValue(page, cv);
        List content = XmlHelper.getContent(v);
        e2.setContent(content);
    }

    public static String getFormattedValue(CommonTemplated container, ComponentValue cv) {
        Object v = cv.getValue();
        ComponentDef def = cv.getDef(container);
        return getFormattedValue(v, def);
    }

    public static String getFormattedValue(Object v, ComponentDef def) {
        if (v instanceof String) {
            return v.toString();
        }
        if (def == null) {
            if (v == null) {
                return "";
            }
            return v.toString();
        }
        return def.formatValue(v);
    }

    static void saveValue(CommonTemplated page, String param, String value) {
        ComponentValue cv = page.getValues().get(param);
        ComponentDef def = null;
        ITemplate template = page.getTemplate();
        if (template != null) {
            def = template.getComponentDef(param);
        }
        if (cv == null) {
            if (def != null) {
                cv = def.createComponentValue(page);
            } else {
                cv = new ComponentValue(param, page);
            }
            page.getValues().add(cv);
        }
        Object oVal = value;
        if (def != null) {
            oVal = def.parseValue(cv, page, value);
        }
        cv.setValue(oVal);
    }

    public static void formatDoc(Document doc, OutputStream out) throws IOException {
        Format format = Format.getPrettyFormat();
        format.setIndent("\t");
        format.setLineSeparator("\n");
        MyXmlOutputter op = new MyXmlOutputter(format);
        op.output(doc, out);
    }

    public static void commit() {
        VfsTransactionManager.commit();
    }
}
