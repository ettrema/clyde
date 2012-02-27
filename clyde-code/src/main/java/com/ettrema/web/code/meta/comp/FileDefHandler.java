package com.ettrema.web.code.meta.comp;

import com.ettrema.web.Template;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.EmailDef;
import com.ettrema.web.component.FileDef;
import com.ettrema.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class FileDefHandler implements ComponentDefHandler{

    private final static String ALIAS = "file";


    public FileDefHandler() {
    }

    @Override
    public Element toXml( ComponentDef def, Template template ) {
        FileDef ddef = (FileDef) def;
        Element el = new Element(ALIAS, CodeMeta.NS);
        populateXml( el, ddef);
        return el;
    }

    private void populateXml( Element el, FileDef ddef ) {
        InitUtils.set(el, "required", ddef.isRequired());
        InitUtils.set(el, "name", ddef.getName());
        InitUtils.set(el, "description", ddef.getDescription());
        InitUtils.set(el, "storeInternally", ddef.isStoreInternally());
    }

    @Override
    public Class getDefClass() {
        return FileDef.class;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public ComponentDef fromXml( Template res, Element el ) {
        String name = el.getAttributeValue( "name");
        FileDef def = new FileDef( res, name);
        boolean required = InitUtils.getBoolean(el, "required");
        boolean storeInternally = InitUtils.getBoolean(el, "storeInternally");
        String desc = InitUtils.getValue(el, "description");
        def.setRequired(required);
        def.setDescription(desc);
        def.setStoreInternally(storeInternally);
        return def;
    }
    
}
