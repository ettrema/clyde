
package com.bradmcevoy.web;

import com.bradmcevoy.utils.XmlUtils2;
import java.io.InputStream;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.SAXException;

public class MyTextRenderer extends ITextRenderer {
    public void setDocument(InputStream in, String url) throws SAXException {
        XmlUtils2 utils = new XmlUtils2();
        Document doc = null;
        doc = utils.getDomDocument(in);
        this.setDocument(doc, url);
        
    }
}
