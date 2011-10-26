package com.ettrema.web.csv;

import com.ettrema.web.Folder;
import java.util.List;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.ExistingResourceFactory;
import com.ettrema.web.Replaceable;
import com.ettrema.web.SubPage;
import com.ettrema.web.WrappedSubPage;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.query.Field;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import org.jdom.Element;

/**
 *          Example config
 *
          <c:view name="list.csv" path="/pharmacies">
               <select type="region">
                    <viewfields>
                      <viewfield name="name"/>
                    </viewfields>
                    <select alias="ph" type="pharmacy">
                        <viewfield name="title"/>
                       <viewfield name="address"/>
                    </select>
                 </select>
            </c:view>
 *
 *
 * @author brad
 */
public class ViewSubPage extends SubPage implements Replaceable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ViewSubPage.class);
    private static final long serialVersionUID = 1L;
    public Path sourceFolder;
    private Select rootSelect;

    public ViewSubPage(CommonTemplated parent, String name) {
        super(parent, name);
    }

    public ViewSubPage(Addressable container, Element el) {
        super(container, el);
    }

    @Override
    public void populateXml(Element el) {
        super.populateXml(el);
        InitUtils.set(el, "sourceFolder", sourceFolder);
        populateFieldsInXml(el);

    }

    public void populateFieldsInXml(Element el) {
        populateSelect(el, rootSelect);
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

    @Override
    public void replaceContent(WrappedSubPage requestedPage, InputStream in, Long length) throws BadRequestException {
        log.trace("replaceContent");
        Folder folder = getSourceFolder(requestedPage.getParentFolder());
        try {
            ViewUpdateHelper.getInstance().fromCsv(in, folder, rootSelect);
            commit();
        } catch (IOException ex) {
            rollback();
            log.error("exception updating", ex);
        }
    }

    public void replaceContent(InputStream in, Long length) {
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

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        log.trace("sendContent: direct");
        ViewRecord rootRec = ViewRecordHelper.getInstance().toRecords(rootSelect, getSourceFolder());
        ViewOutputHelper.getInstance().toCsv(out, rootRec.getChildren());
    }

    @Override
    public void sendContent(WrappedSubPage requestedPage, OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        log.trace("sendContent: wrapped");
        ViewRecord rootRec = ViewRecordHelper.getInstance().toRecords(rootSelect, getSourceFolder(requestedPage.getParentFolder()));
        ViewOutputHelper.getInstance().toCsv(out, rootRec.getChildren());
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String getContentType(String accepts) {
        return getContentType();
    }

    public Path getSourceFolderPath() {
        return sourceFolder;
    }

    public void setSourceFolderPath(Path sourceFolder) {
        this.sourceFolder = sourceFolder;
    }





    private Folder getSourceFolder() {
        return getSourceFolder(this.getParentFolder());
    }

    private Folder getSourceFolder(Resource from) {
        Resource r = ExistingResourceFactory.findChild(from, sourceFolder);
        if( r == null ) {
            throw new RuntimeException("From resource not found: path: " + sourceFolder + " from " + from.getName());
        } else if (r instanceof Folder) {
            return (Folder) r;
        } else {
            throw new RuntimeException("Not a folder: " + r.getClass());
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
}
