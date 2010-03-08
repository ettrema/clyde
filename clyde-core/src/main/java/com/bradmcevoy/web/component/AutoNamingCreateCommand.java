package com.bradmcevoy.web.component;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.RenderContext;
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
