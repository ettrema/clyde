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

    public Class getInstanceType() {
        return Web.class;
    }

    public boolean supports( Resource r ) {
        return r instanceof Web;
    }

    public String getAlias() {
        return ALIAS;
    }


    public Element toXml( Web r ) {
        Element elRoot = new Element( ALIAS, CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    public Web createFromXml( CollectionResource parent, Element d, String name ) {
        Web f = new Web( (Folder) parent, name );
        updateFromXml( f, d );
        return f;
    }

    private void populateXml( Element el, Web page ) {
        folderMetaHandler.populateXml( el, page );
    }

    public void updateFromXml( Web group, Element el ) {

        folderMetaHandler._updateFromXml( group, el);

        group.save();
    }
}
