
package com.ettrema.process;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.process.ActionHandler;
import com.bradmcevoy.process.ProcessContext;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Folder;
import com.ettrema.web.ITemplate;
import com.ettrema.web.Web;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.InitUtils;
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
        for( Object o : el.getChildren() ) {
            if( o instanceof Element ) {
                Element elAtt = (Element) o;
                String text = XmlHelper.getAllText( elAtt );
                attributes.put(elAtt.getName(), text);
            }
        }
    }
    
    @Override
    public void populateXml(Element el) {
        InitUtils.setString( el, "template", templateName);
        InitUtils.setString(el,"path", path.toString());
        for( Map.Entry<String,String> e : attributes.entrySet() ) {
            Element elAtt = new Element(e.getKey());
            el.addContent(elAtt);
            List content = XmlHelper.getContent( e.getValue() );
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
