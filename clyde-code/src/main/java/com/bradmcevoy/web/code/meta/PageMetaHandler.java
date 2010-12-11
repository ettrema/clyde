package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Page;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.MetaHandler;
import java.util.Arrays;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class PageMetaHandler implements MetaHandler<Page> {

    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public PageMetaHandler( BaseResourceMetaHandler baseResourceMetaHandler ) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    public Class getInstanceType() {
        return Page.class;
    }

    public boolean supports( Resource r ) {
        return r instanceof Page;
    }

    public Iterable<String> getAliases() {
        return Arrays.asList( "folder" );
    }

    public Element toXml( Page page ) {
        Element elRoot = new Element( "page", CodeMeta.NS );
        populateXml( elRoot, page );
        return elRoot;
    }

    public Page createFromXml(CollectionResource parent, Element d, String name ) {
        Page page = new Page((Folder) parent,name);
        updateFromXml( page, d );
        return page;
    }

    public void updateFromXml( Page r, Element d ) {
        baseResourceMetaHandler.updateFromXml( r, d, false );
    }



    public void populateXml( Element elRoot, Page page ) {
        baseResourceMetaHandler.populateXml( elRoot, page );
    }

}
