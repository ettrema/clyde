package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.HttpManager;
import com.ettrema.web.*;
import com.ettrema.web.relations.RelationsHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;
import com.ettrema.logging.LogUtils;

/**
 * Accepts an input value which is either a name of a resource in the selectFrom
 * folder or a UUID of a resource
 *
 * But will always set a value of the UUID
 *
 * @author brad
 */
public class RelationSelectDef extends CommonComponent implements ComponentDef, Addressable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RelationSelectDef.class);
    private static final long serialVersionUID = 1L;
    protected Addressable container;
    protected String name;
    protected String validationMessage;
    private boolean required;
    private String description;
    private String relationName;
    protected String selectFromFolder;
    protected String selectTemplate;
    protected boolean multiSelect;

    public RelationSelectDef(Addressable container, String name) {
        this.container = container;
        this.name = name;
    }

    public RelationSelectDef(Addressable container, Element el) {
        this.container = container;
        this.name = el.getAttributeValue("name");
        required = InitUtils.getBoolean(el, "required");
        description = InitUtils.getValue(el, "description");
        relationName = InitUtils.getValue(el, "relationName");
        selectFromFolder = InitUtils.getValue(el, "selectFromFolder");
        selectTemplate = InitUtils.getValue(el, "selectTemplate");
        multiSelect = InitUtils.getBoolean(el, "multiSelect", false);
    }

    @Override
    public void init(Addressable container) {
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return this.container;
    }

    @Override
    public boolean validate(ComponentValue c, RenderContext rc) {
        Object val = c.getValue();
        log.trace("validate: " + val);
        if (required) {
            if (ComponentUtils.isEmpty(val)) {
                log.trace("required, and no value given");
                c.setValidationMessage("Please enter a value");
                return false;
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("required and value supplied, so ok: " + val);
                }
            }
        } else {
            if (ComponentUtils.isEmpty(val)) {
                log.trace("not required and empty so apply no validation");
                return true;
            }
        }
        if (!ComponentUtils.isEmpty(val)) {
            LogUtils.trace(log, "validate: check value of type", val.getClass());
            List<BaseResource> list = findResources(val, rc.getTargetPage());
            if (list == null || list.isEmpty()) {
                c.setValidationMessage("Invalid value");
                log.trace("not valid because no resource was found");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validate(RenderContext rc) {
        if (name == null || name.trim().length() == 0) {
            validationMessage = "Please enter a name";
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element e2 = new Element("componentDef");
        el.addContent(e2);
        e2.setAttribute("class", getClass().getName());
        e2.setAttribute("name", getName());
        InitUtils.setBoolean(e2, "required", required);
        InitUtils.setString(e2, "description", description);
        InitUtils.setString(e2, "relationName", relationName);
        InitUtils.setString(e2, "selectFromFolder", selectFromFolder);
        InitUtils.setString(e2, "selectTemplate", selectTemplate);
        InitUtils.setBoolean(e2, "multiSelect", multiSelect);
        return e2;
    }

    @Override
    public Path getPath() {
        return container.getPath().child(name);
    }

    /**
     *
     * @param newPage
     * @return - create an empty intance of a value containing object suitable
     * for this def
     */
    @Override
    public ComponentValue createComponentValue(Templatable newPage) {
        ComponentValue cv = new ComponentValue(name, newPage);
        cv.init(newPage);
        cv.setValue("");
        return cv;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String render(RenderContext rc) {
        return renderEdit(rc);
    }

    @Override
    public String renderEdit(RenderContext rc) {
        return "";
    }

    @Override
    public String render(ComponentValue c, RenderContext rc) {
        BaseResource res = (BaseResource) c.getContainer();
        BaseResource related = res.getRelation(relationName);
        if (related != null) {
            return related.getLink();
        } else {
            return "";
        }
    }

    @Override
    public String renderEdit(ComponentValue c, RenderContext rc) {
        log.debug("renderEdit");
        Templatable selectFrom = rc.getTarget().find(selectFromFolder);
        if (selectFrom == null) {
            return "Error: couldnt find folder: " + selectFromFolder;
        } else if (selectFrom instanceof Folder) {
            Map<String, String> params = null;
            if (HttpManager.request() != null) {
                params = HttpManager.request().getParams();
            }
            List<UUID> relIds = getRelationIdsFromRequest(rc, params);
            if (relIds == null || relIds.isEmpty()) {
                // find previously selected
                BaseResource page = (BaseResource) c.getContainer();
                BaseResourceList prevSelected = page.getRelations(relationName);
                if (prevSelected != null && !prevSelected.isEmpty()) {
                    relIds = new ArrayList<>();
                    for (Templatable r : prevSelected) {
                        if (r instanceof BaseResource) {
                            relIds.add(((BaseResource) r).getNameNodeId());
                        }
                    }
                }

            }
            StringBuilder sb = new StringBuilder();
            Folder fSelectFrom = (Folder) selectFrom;
            if (multiSelect) {
                _(RelationsHelper.class).buildChecks(fSelectFrom, relIds, sb, "", selectTemplate);
            } else {
                UUID relId = relIds == null || relIds.isEmpty() ? null : relIds.get(0);
                sb.append("<select id='").append(name).append("' name='").append(name).append("'>");
                log.debug("selectFrom: " + fSelectFrom.getHref() + " - " + selectTemplate);
                String sel = relId == null ? " selected " : "";
                sb.append("<option value=''").append(sel).append(">").append("[None]").append("</option>");
                _(RelationsHelper.class).buildOptions(fSelectFrom, relId, sb, "", selectTemplate);
                sb.append("</select>");
            }

            return sb.toString();
        } else {
            return "Error: not a folder: " + selectFromFolder;
        }
    }


    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
    }

    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return null;
    }

    @Override
    public Object parseValue(ComponentValue cv, Templatable ct, String s) {
        return parseValue(ct, s);
    }


    @Override
    public Object parseValue(ComponentValue cv, Templatable ct, Element elValue) {
        String sVal = InitUtils.getValue(elValue);
        return sVal;
    }

    @Override
    public Class getValueClass() {
        return UUID.class;
    }

    @Override
    public String formatValue(Object v) {
        if (v == null) {
            return "";
        }
        return v.toString();
    }

    /**
     * Do pre-processing for child component. This means that it will parse the
     * request parameter and set the value on the child
     */
    @Override
    public void onPreProcess(ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        log.trace("onPreProcess");
        Path compPath = getPath(rc);
        String key = compPath.toString();
        if (!parameters.containsKey(key)) {
            return;
        }
        String paramVal = parameters.get(key);
        updateRelation(componentValue, rc.page, paramVal);
    }



    @Override
    public void changedValue(ComponentValue cv) {
        // big whoop
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getSelectFromFolder() {
        return selectFromFolder;
    }

    public void setSelectFromFolder(String selectFromFolder) {
        this.selectFromFolder = selectFromFolder;
    }

    public String getSelectTemplate() {
        return selectTemplate;
    }

    public void setSelectTemplate(String selectTemplate) {
        this.selectTemplate = selectTemplate;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    private List<BaseResource> findResources(Object val, Templatable ct) {
        Templatable page = (Templatable) getContainer();
        Folder selectFrom = (Folder) ComponentUtils.find(page, Path.path(selectFromFolder));
        if (selectFrom == null) {
            log.warn("Could not find select from path: " + selectFromFolder + " for relation def: " + this.getPath());
            return null;
        }

        return _(RelationsHelper.class).findResources(val, page, selectFrom);
    }

    private List<UUID> getRelationIdsFromRequest(RenderContext rc, Map<String, String> params) {
        Templatable page = (Templatable) getContainer();
        Folder selectFrom = (Folder) ComponentUtils.find(page, Path.path(selectFromFolder));
        if (selectFrom == null) {
            log.warn("Could not find select from path: " + selectFromFolder + " for relation def: " + this.getPath());
            return null;
        }
        
        Path compPath = getPath(rc);
        return _(RelationsHelper.class).getRelationIdsFromRequest(rc, params, compPath, selectFrom);
    }
    
    public Object parseValue(Templatable ct, String s) {
        Templatable page = (Templatable) getContainer();
        Folder selectFrom = (Folder) ComponentUtils.find(page, Path.path(selectFromFolder));
        if (selectFrom == null) {
            log.warn("Could not find select from path: " + selectFromFolder + " for relation def: " + this.getPath());
            return null;
        }

        return _(RelationsHelper.class).parseValue(ct, s, selectFrom);
    }

    public boolean updateRelation(ComponentValue componentValue, Templatable page, String s) {
        Folder selectFrom = (Folder) ComponentUtils.find(page, Path.path(selectFromFolder));
        if (selectFrom == null) {
            log.warn("Could not find select from path: " + selectFromFolder + " for relation def: " + this.getPath());
            return false;
        }

        return _(RelationsHelper.class).updateRelation(componentValue, page, s, selectFrom, relationName);        
    }
}
