package com.ettrema.web.component;

import org.jdom.Element;

/**
 *
 * @author brad
 */
public class AutoNamingCreateCommand extends CreateCommand{

    public AutoNamingCreateCommand(Addressable container, String name) {
        super(container,name);
    }

    public AutoNamingCreateCommand(Addressable container, Element el) {
        super(container, el);
    }








}
