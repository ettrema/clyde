package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.Organisation;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class OrganisationMetaHandler implements MetaHandler<Organisation> {
    
    public static final String ALIAS = "organisation";

    private final HostMetaHandler hostMetaHandler;

    public OrganisationMetaHandler( HostMetaHandler hostMetaHandler ) {
        this.hostMetaHandler = hostMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return Organisation.class;
    }

    @Override
    public boolean supports( Resource r ) {
        return r instanceof Organisation;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }


    @Override
    public Element toXml( Organisation r ) {
        Element elRoot = new Element( ALIAS, CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    @Override
    public Organisation createFromXml( CollectionResource parent, Element d, String name ) {
        Folder fParent = (Folder) parent;
        Organisation f = new Organisation( fParent, name );
        System.out.println("------------- creating org: " + name + " - in parent: " + fParent.getPath());
        System.out.println("parent id: " + fParent.getNameNodeId());
        
        updateFromXml( f, d );
        return f;
    }

    @Override
    public void applyOverrideFromXml(Organisation r, Element el) {
        hostMetaHandler.applyOverrideFromXml(r, el);
        r.save();
    }
       
    private void populateXml( Element el, Organisation host ) {
        hostMetaHandler.populateXml( el, host );
    }

    @Override
    public void updateFromXml( Organisation host, Element el ) {
        hostMetaHandler.updateFromXml( host, el);
    }
}
