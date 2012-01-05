
package com.ettrema.web;

import com.bradmcevoy.http.Request;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VirtualFolder extends Folder {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VirtualFolder.class);

    private static final long serialVersionUID = 1L;

    private final List<Templatable> children = new ArrayList<Templatable>();
    
    public VirtualFolder(Folder parentFolder, String newName) {
        super(parentFolder, newName);
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("Cant save a virtual folder");
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Cant delete a virtual folder");
    }

    @Override
    public List<Templatable> getChildren() {
        return children;
    }
    
    @Override
    public List<Templatable> getChildren(String template) {
        List<Templatable> list = new ArrayList<Templatable>();
        for( Templatable ct : children ) {
            if( template == null || ct.is(template)) {
                list.add(ct);
            }
        }
        Collections.sort(list);
        return list;
    }
    

    @Override
    public Templatable child(String name) {
        Templatable ct1 = super.childRes(name);
        if( ct1 != null ) return ct1;
        
        for( Templatable ct : getChildren() ) {
            if( ct.getName().equals(name)) return ct;
        }
        return null;
    }
    
    public void add(Templatable newChild) {
        children.add(newChild);
    }    
}
