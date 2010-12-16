package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.TextFile;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.MetaHandler;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class TextFileMetaHandler implements MetaHandler<BinaryFile> {

    public static final String ALIAS = "textfile";

    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public TextFileMetaHandler( BaseResourceMetaHandler baseResourceMetaHandler ) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    public Class getInstanceType() {
        return TextFile.class;
    }

    public boolean supports( Resource r ) {
        return r instanceof TextFile;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml( BinaryFile r ) {
        Element el = new Element( ALIAS, CodeMeta.NS );
        populateXml( el, r );
        return el;
    }

    public BinaryFile createFromXml( CollectionResource parent, Element d, String name ) {
        BinaryFile f = new BinaryFile( (Folder) parent, name );
        updateFromXml( f, d );
        return f;
    }

    private void populateXml( Element el, BinaryFile page ) {
//        InitUtils.set( el, "contentLength", page.getContentLength());
//        el.setAttribute( "crc", page.getCrc() + "" );
//        el.setAttribute( "firstVersionDone", page.isFirstVersionDone() + "" );

        baseResourceMetaHandler.populateXml( el, page );
    }

    public void updateFromXml( BinaryFile r, Element d ) {
        baseResourceMetaHandler.updateFromXml( r, d );
        r.save();
    }
}
