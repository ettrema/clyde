package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.CsvView;
import com.ettrema.web.Folder;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import com.ettrema.web.component.InitUtils;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CsvViewMetaHandler implements MetaHandler<CsvView> {

    public static final String ALIAS = "csv";

    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public CsvViewMetaHandler( BaseResourceMetaHandler baseResourceMetaHandler ) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }



    public Class getInstanceType() {
        return CsvView.class;
    }

    public boolean supports( Resource r ) {
        return r instanceof CsvView;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml( CsvView r ) {
        Element elRoot = new Element( ALIAS, CodeMeta.NS );
        populateXml( elRoot, r );
        return elRoot;
    }

    public CsvView createFromXml( CollectionResource parent, Element d, String name ) {
        CsvView f = new CsvView( (Folder) parent, name );
        updateFromXml(f, d );
        return f;
    }



    private void populateXml( Element el, CsvView page ) {
        InitUtils.setString(el, "type", page.getIsType());
        InitUtils.set(el, "sourceFolder", page.getSourceFolderPath());

        baseResourceMetaHandler.populateXml( el, page );
    }


    public void updateFromXml( CsvView r, Element d ) {
        baseResourceMetaHandler.updateFromXml(r, d, false);

        r.setIsType( InitUtils.getValue( d, "type"));
        r.setSourceFolderPath( InitUtils.getPath( d, "sourceFolder"));
        r.save();
    }
}
