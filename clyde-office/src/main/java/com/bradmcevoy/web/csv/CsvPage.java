package com.bradmcevoy.web.csv;

import com.bradmcevoy.utils.JDomUtils;
import java.util.ArrayList;
import java.util.List;
import com.bradmcevoy.web.*;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.component.InitUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.jdom.Element;


public class CsvPage extends com.bradmcevoy.web.File implements Replaceable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CsvPage.class );
    private static final long serialVersionUID = 1L;
    public Path sourceFolder;
    private Select rootSelect;

    public CsvPage( String contentType, Folder parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    public CsvPage( Folder parentFolder, String newName ) {
        this( "text/csv", parentFolder, newName );
    }

    @Override
    public String getDefaultContentType() {
        return "text/csv";
    }


    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        InitUtils.set(e2, "sourceFolder", sourceFolder);
        populateFieldsInXml(e2);

    }

    public void populateFieldsInXml(Element e2) {
        populateSelect(e2, rootSelect);
    }

    private void populateSelect(Element elParent, Select select) {
        Element elSelect = new Element("select");
        elParent.addContent(elSelect);
        InitUtils.set(elSelect, "type", select.getType());
        Element elFields = new Element("viewfields");
        elSelect.addContent(elFields);
        if (select.getFields() != null) {
            for (Field f : select.getFields()) {
                Element elField = new Element("viewfield");
                elFields.addContent(elField);
                elField.setAttribute("name", f.getName());
            }
        }
        if (select.getSubSelect() != null) {
            populateSelect(elSelect, select.getSubSelect());
        }
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        String s = InitUtils.getValue(el, "sourceFolder", ".");
        loadFieldsFromXml(el);
    }

    public void loadFieldsFromXml(Element el) {
        Element elSelect = el.getChild("select");
        if (elSelect != null) {
            rootSelect = selectFromXml(elSelect);
        } else {
            elSelect = null;
        }
    }

    private Select selectFromXml(Element elSelect) {
        String type = InitUtils.getValue(elSelect, "type");
        List<Field> fields = new ArrayList<Field>();
        for (Element elField : JDomUtils.childrenOf(elSelect, "viewfields")) {
            fields.add(new Field(elField.getAttributeValue("name")));
        }
        Element elChildSelect = elSelect.getChild("select");
        Select subSelect;
        if( elChildSelect != null) {
            subSelect = selectFromXml(elChildSelect);
        } else {
            subSelect = null;
        }

        Select select = new Select(type, subSelect, fields);
        return select;
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new CsvPage( parent, newName );
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        log.trace("sendContent: direct");
        ViewRecord rootRec = ViewRecordHelper.getInstance().toRecords(rootSelect, getSourceFolder());
        ViewOutputHelper.getInstance().toCsv(out, rootRec.getChildren());
    }

    public void replaceContent( InputStream in, Long length ) {
        log.trace("replaceContent");
        Folder folder = getSourceFolder();
        try {
            ViewUpdateHelper.getInstance().fromCsv(in, folder, rootSelect);
            commit();
        } catch (IOException ex) {
            rollback();
            log.error("exception updating", ex);
        }
    }

    private Folder getSourceFolder() {
        if( sourceFolder == null ) {
            throw new RuntimeException("sourceFolder is null");
        }
        Resource r = ExistingResourceFactory.findChild(this.getParentFolder(), sourceFolder);
        if( r == null ) {
            throw new RuntimeException("From resource not found: path: " + sourceFolder);
        } else if (r instanceof Folder) {
            return (Folder) r;
        } else {
            throw new RuntimeException("Not a folder: " + r.getClass());
        }
    }

    @Override
    public boolean isIndexable() {
        return false;
    }

    public void setSourceFolderPath( Path sourceFolder ) {
        this.sourceFolder = sourceFolder;
    }

    public Path getSourceFolderPath() {
        return sourceFolder;
    }
}