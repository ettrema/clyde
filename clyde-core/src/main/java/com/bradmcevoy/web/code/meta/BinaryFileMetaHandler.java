package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.MetaHandler;
import java.util.Arrays;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class BinaryFileMetaHandler implements MetaHandler<BinaryFile> {

    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public BinaryFileMetaHandler( BaseResourceMetaHandler baseResourceMetaHandler ) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    public Class getInstanceType() {
        return BinaryFile.class;
    }

    public boolean supports( Resource r ) {
        return r instanceof BinaryFile;
    }

    public Iterable<String> getAliases() {
        return Arrays.asList( "binary" );
    }

    public Element toXml( BinaryFile r ) {
        Element el = new Element( "binary", CodeMeta.NS );
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
    }
}
