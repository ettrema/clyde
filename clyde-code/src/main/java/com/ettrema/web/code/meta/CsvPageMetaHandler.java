package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.web.Folder;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.csv.CsvPage;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CsvPageMetaHandler implements MetaHandler<CsvPage> {

    public static final String ALIAS = "csvpage";
    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public CsvPageMetaHandler(BaseResourceMetaHandler baseResourceMetaHandler) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    public Class getInstanceType() {
        return CsvPage.class;
    }

    public boolean supports(Resource r) {
        return r instanceof CsvPage;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml(CsvPage r) {
        Element elRoot = new Element(ALIAS, CodeMeta.NS);
        populateXml(elRoot, r);
        return elRoot;
    }

    public CsvPage createFromXml(CollectionResource parent, Element d, String name) {
        CsvPage f = new CsvPage((Folder) parent, name);
        updateFromXml(f, d);
        return f;
    }

    private void populateXml(Element el, CsvPage page) {
        InitUtils.set(el, "sourceFolder", page.getSourceFolderPath());
        page.populateFieldsInXml(el);
        baseResourceMetaHandler.populateXml(el, page);
    }

    public void updateFromXml(CsvPage r, Element d) {
        baseResourceMetaHandler.updateFromXml(r, d, false);
        r.setSourceFolderPath(InitUtils.getPath(d, "sourceFolder"));
        r.loadFieldsFromXml(d);
        r.save();
    }
}
