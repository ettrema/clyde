package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.CsvView;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.MetaHandler;
import com.bradmcevoy.web.component.InitUtils;
import java.util.Arrays;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CsvViewMetaHandler implements MetaHandler<CsvView> {

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

    public Iterable<String> getAliases() {
        return Arrays.asList( "csv" );
    }

    public Element toXml( CsvView r ) {
        Element elRoot = new Element( "csv", CodeMeta.NS );
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
    }
}
