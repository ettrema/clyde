package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.Pdf;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class PdfMetaHandler implements MetaHandler<Pdf> {

    public static final String ALIAS = "pdf";

    private final BinaryFileMetaHandler binaryFileMetaHandler;

    public PdfMetaHandler( BinaryFileMetaHandler binaryFileMetaHandler ) {
        this.binaryFileMetaHandler = binaryFileMetaHandler;
    }

    public Class getInstanceType() {
        return Pdf.class;
    }

    public boolean supports( Resource r ) {
        return r instanceof Pdf;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml( Pdf r ) {
        Element el = new Element( ALIAS, CodeMeta.NS );
        populateXml( el, r );
        return el;
    }

    public Pdf createFromXml( CollectionResource parent, Element d, String name ) {
        Pdf f = new Pdf( (Folder) parent, name );
        updateFromXml( f, d );
        return f;
    }

    private void populateXml( Element el, Pdf page ) {
        binaryFileMetaHandler.populateXml( el, page );
    }

    public void updateFromXml( Pdf r, Element d ) {
        binaryFileMetaHandler.updateFromXml( r, d );
        r.save();
    }

    @Override
    public void applyOverrideFromXml(Pdf r, Element el) {
        binaryFileMetaHandler.applyOverrideFromXml(r, el);
        r.save();
    }
    
    
}
