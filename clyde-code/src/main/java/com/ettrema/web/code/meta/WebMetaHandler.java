package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.Web;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class WebMetaHandler implements MetaHandler<Web> {
    
    public static final String ALIAS = "web";

    private final FolderMetaHandler folderMetaHandler;

    public WebMetaHandler( FolderMetaHandler folderMetaHandler ) {
        this.folderMetaHandler = folderMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return Web.class;
    }

    @Override
    public boolean supports( Resource r ) {
        return r instanceof Web;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }


    @Override
    public Element toXml( Web r ) {
        Element elRoot = new Element( ALIAS, CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    @Override
    public Web createFromXml( CollectionResource parent, Element d, String name ) {
        Web f = new Web( (Folder) parent, name );
        updateFromXml( f, d );
        return f;
    }

    public void populateXml( Element el, Web page ) {
        folderMetaHandler.populateXml( el, page );
    }

    @Override
    public void updateFromXml( Web web, Element el ) {
        _updateFromXml(web, el);
        web.save();
    }
    
    public void _updateFromXml( Web web, Element el ) {
        folderMetaHandler._updateFromXml( web, el);
    }    

    @Override
    public void applyOverrideFromXml(Web r, Element el) {
        folderMetaHandler.applyOverrideFromXml(r, el);
        r.save();
    }
    
    
}
