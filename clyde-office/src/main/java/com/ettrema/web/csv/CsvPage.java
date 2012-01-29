package com.ettrema.web.csv;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.*;
import com.ettrema.web.component.ComponentUtils;
import com.ettrema.web.component.EvaluatableComponent;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.query.Field;
import com.ettrema.web.query.FieldSource;
import com.ettrema.web.query.Selectable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import org.jdom.Namespace;

public class CsvPage extends com.ettrema.web.File implements Replaceable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CsvPage.class);
    private static final long serialVersionUID = 1L;
    public static final Namespace NS = Namespace.getNamespace("c", "http://clyde.ettrema.com/ns/core");
    public Path sourceFolder;
    private Select rootSelect;
    /**
     * The page will either use a selectable or the properties above
     */
    private Path selectablePath;


    public CsvPage(String contentType, Folder parentFolder, String newName) {
        super(contentType, parentFolder, newName);
    }

    public CsvPage(Folder parentFolder, String newName) {
        this("text/csv", parentFolder, newName);
    }

    @Override
    public String getDefaultContentType() {
        return "text/csv";
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        populateFieldsInXml(e2);

    }

    public void populateFieldsInXml(Element e2) {
        InitUtils.set(e2, "sourceFolder", sourceFolder);
        InitUtils.set(e2, "selectablePath", selectablePath);
        if (rootSelect != null) {
            populateSelect(e2, rootSelect);
        }
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
        loadFieldsFromXml(el);
    }

    public void loadFieldsFromXml(Element el) {
        selectablePath = InitUtils.getPath(el, "selectablePath");
        Element elSelect = el.getChild("select");
        if (elSelect != null) {
            rootSelect = selectFromXml(elSelect);
        } else {
            rootSelect = null;
        }
    }

    private Select selectFromXml(Element elSelect) {
        String type = InitUtils.getValue(elSelect, "type");
        List<Field> fields = new ArrayList<>();
        for (Element elField : JDomUtils.childrenOf(elSelect, "viewfields")) {
            Field f = Field.fromXml(elField,this, NS); 
            fields.add(f);
        }
        Element elChildSelect = elSelect.getChild("select");
        Select subSelect;
        if (elChildSelect != null) {
            subSelect = selectFromXml(elChildSelect);
        } else {
            subSelect = null;
        }

        Select select = new Select(type, subSelect, fields);
        return select;
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new CsvPage(parent, newName);
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {        
        List<FieldSource> rows;
        if (rootSelect != null) {
            log.trace("sendContent: direct: using view");
            ViewRecord rootRec = ViewRecordHelper.getInstance().toRecords(rootSelect, getSourceFolder());
            ViewOutputHelper.getInstance().toCsv(out, rootRec.getChildren());
        } else if(selectablePath != null) {
            log.trace("sendContent: direct: using selectable: " + selectablePath);
            Component c = ComponentUtils.findComponent(selectablePath, this);
            if( c == null ) {
                throw new RuntimeException("No component: " + selectablePath);
            } else if( c instanceof EvaluatableComponent ) {
                EvaluatableComponent ec = (EvaluatableComponent) c;
                if( ec.getEvaluatable() == null ) {
                    throw new RuntimeException("Evaluatable is empty");
                } else if( ec.getEvaluatable() instanceof Selectable ) {
                    Selectable selectable = (Selectable) ec.getEvaluatable();
                    rows = selectable.getRows(this.getParent());
                    ViewOutputHelper.getInstance().toCsv(out, selectable, rows );
                }
            } else {
                throw new RuntimeException("component is not of type EvaluatableComponent, is a: " + c.getClass());
            }
        } else {
            log.warn("Neither a rootSelect (view) not a selectablePath (query) are present");
        }
    }

    @Override
    public void replaceContent(InputStream in, Long length) throws NotAuthorizedException, BadRequestException {
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

    private Folder getSourceFolder() throws NotAuthorizedException, BadRequestException {
        if (sourceFolder == null) {
            return this.getParent();
        }
        Resource r = ExistingResourceFactory.findChild(this.getParentFolder(), sourceFolder);
        if (r == null) {
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

    public void setSourceFolderPath(Path sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    public Path getSourceFolderPath() {
        return sourceFolder;
    }
}
