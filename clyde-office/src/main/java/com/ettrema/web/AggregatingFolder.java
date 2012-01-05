
package com.ettrema.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.velocity.VelocityInterpreter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

public class AggregatingFolder extends Folder {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AggregatingFolder.class);

    private static final long serialVersionUID = 1L;
    
    public AggregationSpec aggregationSpec;
    public Path sourceFolder;
    public String isType;   // only inlcude items of this type
    
    public AggregatingFolder(Folder parentFolder, String newName) {
        super(parentFolder, newName);
    }

    @Override
    protected BaseResource copyInstance(Folder parent, String newName) {
        Folder fNew = (Folder) super.copyInstance(parent, newName);
        return fNew;
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new AggregatingFolder(parent, newName);
    }

    @Override
    public void populateXml(Element el) {
        super.populateXml(el);
        if( aggregationSpec != null ) {
            aggregationSpec.toXml(el);
        }
        InitUtils.set(el, "sourceFolder", sourceFolder);
        InitUtils.setString(el, "type", isType);
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        this.aggregationSpec = AggregationSpec.loadFromXml(el);
        String s = InitUtils.getValue(el, "sourceFolder", ".");
        sourceFolder = Path.path(s);
        isType = InitUtils.getValue(el, "type");
    }

    
    
    @Override
    public List<Templatable> getChildren() {
        log.debug("getChildren: " + isType);
        Map<String,VirtualFolder> map = new HashMap<String,VirtualFolder>();
        Folder source = (Folder) this.find(sourceFolder);
        for( Templatable res : source.getChildren(isType) ) {
            groupBy(res, map);
        }
        List<Templatable> list = new ArrayList<Templatable>(map.values());
        return list;
    }

    @Override
    public Resource child(String name) {
//        log.debug("child: " + name); 
        for( Templatable ct : getChildren() ) {
            if( ct.getName().equals(name)) return ct;
        }
//        log.debug("..no such child");
        return null;
    }

    


    protected void groupBy(Templatable res, Map<String,VirtualFolder> map) {
        VelocityContext vc = new VelocityContext();
        vc.put("page", res);
        String s = VelocityInterpreter.evalToString(aggregationSpec.expr, vc); // calculate the group by value
        VirtualFolder f = map.get(s);
        if( f == null ) {
            f = new VirtualFolder(this, s);
            f.setTemplateName(aggregationSpec.template);
            map.put(s, f);
        }
        
        add(f, res, aggregationSpec.child);        
    }


    private void add(VirtualFolder f, Templatable res, AggregationSpec spec) {
        if( spec == null ) {
            f.add(res);
        } else {
            VelocityContext vc = new VelocityContext();
            vc.put("page", res);
            String s = VelocityInterpreter.evalToString(spec.expr, vc); // calculate the group by value
            VirtualFolder childFolder = (VirtualFolder) f.child(s);
            if( childFolder == null ) {
                childFolder = new VirtualFolder(f, s);
                childFolder.setTemplateName(spec.template);
                f.add(childFolder);
            }
            add(childFolder,res,spec.child);
        }
    }    
}
