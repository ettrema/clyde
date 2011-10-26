package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.TextFile;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class TextFileMetaHandler implements MetaHandler<TextFile> {

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

    public Element toXml( TextFile r ) {
        Element el = new Element( ALIAS, CodeMeta.NS );
        populateXml( el, r );
        return el;
    }

    public TextFile createFromXml( CollectionResource parent, Element d, String name ) {
        TextFile f = new TextFile( (Folder) parent, name );
        updateFromXml( f, d );
        return f;
    }

    private void populateXml( Element el, TextFile page ) {
//        InitUtils.set( el, "contentLength", page.getContentLength());
//        el.setAttribute( "crc", page.getCrc() + "" );
//        el.setAttribute( "firstVersionDone", page.isFirstVersionDone() + "" );

        baseResourceMetaHandler.populateXml( el, page );
    }

    public void updateFromXml( TextFile r, Element d ) {
        baseResourceMetaHandler.updateFromXml( r, d );
        r.save();
    }
}
