package com.ettrema.web.component;

import com.ettrema.web.BaseResource;
import com.ettrema.web.SystemComponent;

public class NameInput extends Text implements SystemComponent {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NameInput.class);
    private static final long serialVersionUID = 1L;

    public NameInput(Addressable container) {
        super(container, "name");
    }

    @Override
    public String getValue() {
        return ((BaseResource) this.container).getName();
    }

    @Override
    public void setValue(String t) {
        BaseResource page = (BaseResource) this.container;
        String nm = page.getName();
        if( nm.equals(t)) return ;
        page.rename(t);
    }

    @Override
    protected void initValue() {
        // do nothing
    }
    
    
}
