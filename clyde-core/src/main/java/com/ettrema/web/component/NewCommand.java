
package com.ettrema.web.component;

import org.jdom.Element;

public class NewCommand extends SaveCommand{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NewCommand.class);
    
    private static final long serialVersionUID = 1L;
    
    public NewCommand(Addressable container, Element el) {
        super(container,el);
    }

    

}
