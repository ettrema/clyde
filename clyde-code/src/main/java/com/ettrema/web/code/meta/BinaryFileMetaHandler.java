package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.BinaryFile;
import com.ettrema.web.Folder;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class BinaryFileMetaHandler implements MetaHandler<BinaryFile> {

    public static final String ALIAS = "binary";

    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public BinaryFileMetaHandler( BaseResourceMetaHandler baseResourceMetaHandler ) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return BinaryFile.class;
    }

    @Override
    public boolean supports( Resource r ) {
        return r instanceof BinaryFile;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public Element toXml( BinaryFile r ) {
        Element el = new Element( ALIAS, CodeMeta.NS );
        populateXml( el, r );
        return el;
    }

    @Override
    public BinaryFile createFromXml( CollectionResource parent, Element d, String name ) {
        BinaryFile f = new BinaryFile( (Folder) parent, name );
        updateFromXml( f, d );
        return f;
    }

    public void populateXml( Element el, BinaryFile page ) {
//        InitUtils.set( el, "contentLength", page.getContentLength());
//        el.setAttribute( "crc", page.getCrc() + "" );
//        el.setAttribute( "firstVersionDone", page.isFirstVersionDone() + "" );

        baseResourceMetaHandler.populateXml( el, page );
    }

    @Override
    public void updateFromXml( BinaryFile r, Element d ) {
        baseResourceMetaHandler.updateFromXml( r, d );
        r.save();
    }

    @Override
    public void applyOverrideFromXml(BinaryFile r, Element d) {
        baseResourceMetaHandler.applyOverrideFromXml( r, d );
        r.save();
    }
}
