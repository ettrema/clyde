
package com.bradmcevoy.process;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.Web;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.xml.XmlHelper;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;


public class Create implements ActionHandler, Serializable{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Create.class);
    
    private static final long serialVersionUID = 1L;
    
    public String templateName;

    private Path path;
    
    private final Map<String,String> attributes = new HashMap<String, String>();
    
    public Create(Element el) {
        templateName = el.getAttributeValue("template");
        String sPath = el.getAttributeValue("path");
        path = Path.path(sPath);
        attributes.clear();
        XmlHelper helper = new XmlHelper();
        for( Object o : el.getChildren() ) {
            if( o instanceof Element ) {
                Element elAtt = (Element) o;
                String text = helper.getAllText( elAtt );
                attributes.put(elAtt.getName(), text);
            }
        }
    }
    
    @Override
    public void populateXml(Element el) {
        InitUtils.setString( el, "template", templateName);
        InitUtils.setString(el,"path", path.toString());
        XmlHelper helper = new XmlHelper();
        for( Map.Entry<String,String> e : attributes.entrySet() ) {
            Element elAtt = new Element(e.getKey());
            el.addContent(elAtt);
            List content = helper.getContent( e.getValue() );
            elAtt.setContent( content );
        }
    }

    @Override
    public void process(ProcessContext processContext) {
        BaseResource res = (BaseResource) processContext.getAttribute("res");
        Web web = res.getWeb();
        
        Folder folderToCreateIn = (Folder) res.find(path.getParent());
        
        ITemplate t = web.getTemplate(templateName);
        if( t == null ) throw new NullPointerException("No template: " + templateName);
        
        log.debug("---- Creating: " + path.getName());
        BaseResource newRes = t.createPageFromTemplate(folderToCreateIn, path.getName());
        for( Map.Entry<String,String> e : attributes.entrySet() ) {
            log.debug("..setting attribute: " + e.getKey() + " = " + e.getValue());
            ComponentDef def = t.getComponentDef(e.getKey());
            ComponentValue cv = def.createComponentValue(newRes);
            cv.setValue(e.getValue());
            newRes.getValues().add(cv);
        }
        newRes.save();
        log.debug("created: " + newRes.getHref());
    }
}
