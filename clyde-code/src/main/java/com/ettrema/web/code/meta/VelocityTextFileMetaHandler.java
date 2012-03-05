package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.TextFile;
import com.ettrema.web.VelocityTextFile;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class VelocityTextFileMetaHandler implements MetaHandler<VelocityTextFile> {

    public static final String ALIAS = "velocitytextfile";

    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public VelocityTextFileMetaHandler( BaseResourceMetaHandler baseResourceMetaHandler ) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return VelocityTextFile.class;
    }

    @Override
    public boolean supports( Resource r ) {
        return r instanceof VelocityTextFile;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public Element toXml( VelocityTextFile r ) {
        Element el = new Element( ALIAS, CodeMeta.NS );
        populateXml( el, r );
        return el;
    }

    @Override
    public VelocityTextFile createFromXml( CollectionResource parent, Element d, String name ) {
        VelocityTextFile f = new VelocityTextFile( (Folder) parent, name );
        updateFromXml( f, d );
        return f;
    }

    private void populateXml( Element el, VelocityTextFile page ) {
        baseResourceMetaHandler.populateXml( el, page );
    }

    @Override
    public void updateFromXml( VelocityTextFile r, Element d ) {
        baseResourceMetaHandler.updateFromXml( r, d );
        r.save();
    }

    @Override
    public void applyOverrideFromXml(VelocityTextFile r, Element el) {
        baseResourceMetaHandler.applyOverrideFromXml(r, el);
        r.save();
    }
    
    
}
