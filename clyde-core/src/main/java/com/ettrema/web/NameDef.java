package com.ettrema.web;

import com.bradmcevoy.http.FileItem;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.TextDef;
import java.util.Map;

public class NameDef extends TextDef {
    
    private static final long serialVersionUID = 1L;
    
    public NameDef(Addressable container) {
        super(container,"name");
    }
    
    @Override
    public void onPreProcess(ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        String oldName = (String) componentValue.getValue();
        super.onPreProcess(componentValue,rc,parameters,files);
        String newName = (String) componentValue.getValue();
        
        if( !newName.equals(oldName) ) {
            Page page = (Page) this.container;
            page.rename(newName);
        }
    }
}
