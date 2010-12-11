package com.bradmcevoy.web.code.content;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Page;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.xml.XmlHelper;
import com.ettrema.vfs.VfsSession;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.MyXmlOutputter;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class CodeUtils {

    public static void appendValue(Page page, ComponentValue cv, Document doc) {
        String v = getFormattedValue( page, cv );
        XmlHelper helper = new XmlHelper();
        List content = helper.getContent( v );
        if( content == null || content.isEmpty() ) {
            Element elRoot = new Element("html");
            doc.setRootElement( elRoot );
        } else if( content.size() == 1) {
            doc.setRootElement( (Element) content.get(0));
        } else {
            Element elRoot = new Element("multipleRootElements-EEK");
            elRoot.addContent( content );
            doc.setRootElement( elRoot );
        }
    }

    public static void appendValue(Page page, ComponentValue cv, Element e2) {
        String v = getFormattedValue( page, cv );
        XmlHelper helper = new XmlHelper();
        List content = helper.getContent( v );
        e2.setContent( content );
    }

    public static  String getFormattedValue( CommonTemplated container, ComponentValue cv ) {
        Object v = cv.getValue();
        ComponentDef def = cv.getDef( container );
        return getFormattedValue( v, def );
    }

    public static  String getFormattedValue( Object v, ComponentDef def ) {

        if( def == null ) {
            if( v == null ) return "";
            return v.toString();
        }
        return def.formatValue( v );
    }

    static void saveValue( CommonTemplated page, String param, String value ) {
        ComponentValue cv = page.getValues().get( param );
        ComponentDef def = page.getTemplate().getComponentDef( param );
        if( cv == null ) {
            if( def != null ) {
                cv = def.createComponentValue( page );
            } else {
                cv = new ComponentValue( value, page );
            }
        }
        Object oVal = value;
        if( def != null ) {
            oVal = def.parseValue( cv, page, value );
        }
        cv.setValue( oVal );
    }
    
    public static void formatDoc( Document doc, OutputStream out ) throws IOException {
        Format format = Format.getPrettyFormat();
        format.setIndent( "\t" );
        MyXmlOutputter op = new MyXmlOutputter( format );
        op.output( doc, out );
    }

    public static void commit() {
        _(VfsSession.class).commit();
    }
}
