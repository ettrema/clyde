package com.ettrema.web.relations;

import com.bradmcevoy.common.Path;
import com.ettrema.logging.LogUtils;
import com.ettrema.vfs.Relationship;
import com.ettrema.web.*;
import com.ettrema.web.component.ComponentUtils;
import com.ettrema.web.component.ComponentValue;
import java.util.*;

/**
 *
 * @author brad
 */
public class RelationsHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RelationsHelper.class);

    public boolean updateRelations(ComponentValue componentValue, Templatable page, Folder selectFrom, String relationName) {
        List reqValues = null;
        if (componentValue.getValue() instanceof List) {
            reqValues = (List) componentValue.getValue();
        } else if (componentValue.getValue() instanceof String) {
            reqValues = parse((String) componentValue.getValue());
        } else {
            throw new RuntimeException("Not a suitable type: " + componentValue.getValue());
        }

        List<BaseResource> selected;
        try {
            selected = toResources(reqValues, page, selectFrom);
        } catch (Exception e) {
            throw new RuntimeException("Exception updating relations on page: " + page.getUrl() + " component: " + componentValue.getName(), e);
        }
        LogUtils.trace(log, "updateRelation", reqValues, reqValues.size(), "resources=", selected.size());

        BaseResource res = (BaseResource) page;
        if (log.isTraceEnabled()) {
            if (reqValues != null) {
                for (Object id : reqValues) {
                    log.trace("requested relation to: " + id);
                }
            }
        }


        List<Relationship> current = res.getNameNode().findFromRelations(relationName);
        boolean didChange = false;
        if (removeDeselected(selected, current, res, relationName)) {
            didChange = true;
        }
        if (createNewlySelected(selected, current, res, relationName)) {
            didChange = true;
        }
        return didChange;
    }

    public String checkBoxesHtml(Folder fSelectFrom, List<UUID> relIds, String selectTemplate, Path componentName) {
        StringBuilder sb = new StringBuilder();
        // just a dummy value to trigger processing when no checkboxes are ticked
        sb.append("<input type='hidden' name='").append(componentName).append("' value=''/>");
        checkBoxesHtml(fSelectFrom, relIds, sb, "", selectTemplate, componentName);
        return sb.toString();
    }

    private void checkBoxesHtml(Folder fSelectFrom, List<UUID> relIds, StringBuilder sb, String prefix, String selectTemplate, Path componentName) {
        for (Templatable ct : fSelectFrom.getChildren(selectTemplate)) {
            if (ct instanceof BaseResource) {
                BaseResource res = (BaseResource) ct;
                String sel = "";
                if (relIds != null && relIds.contains(res.getNameNodeId())) {
                    sel = " checked='true' ";
                }
                String name = res.getName();
                String id = res.getNameNodeId().toString();
                String htmlId = name + "-" + id;
                // put the name of this input in as a class to make styling specific instances easier
                sb.append("<input name='").append(componentName).append("' class='multiSelect ").append(name).append("' type='checkbox' id='").append(htmlId).append("' value='").append(id).append("'").append(sel).append("/>");
                String link = "<a href=\"" + res.getUrl() + "\">" + prefix + name + "</a>";
                sb.append("<label class='checkboxLabel' for='").append(htmlId).append("'>").append(link).append("</label>");
            }
        }
        for (Folder fChild : fSelectFrom.getSubFolders()) {
            String newPrefix = prefix + fChild.getName() + " - ";
            checkBoxesHtml(fChild, relIds, sb, newPrefix, selectTemplate, componentName);
        }

    }

    private boolean removeDeselected(List<BaseResource> requested, List<Relationship> current, BaseResource page, String relationName) {
        log.trace("removeDeselected");
        boolean didChange = false;
        Set<UUID> requestedIds = new HashSet<>();
        for (BaseResource r : requested) {
            System.out.println("requested:" + r.getNameNodeId());
            requestedIds.add(r.getNameNodeId());
        }
        for (Relationship rel : current) {
            UUID id = rel.to().getId();
            if (requestedIds.contains(id)) {
                log.trace("current id is requested, so leave: " + id);
            } else {
                log.trace("removeDeselected: found previously selected relationship to remove");
                rel.delete();
                didChange = true;
            }
        }
        LogUtils.trace(log, "removeDeselected:  didChange: ", didChange);
        return didChange;
    }

    private boolean createNewlySelected(List<BaseResource> requested, List<Relationship> current, BaseResource page, String relationName) {
        if (requested == null) {
            return false;
        }
        Set<UUID> currentIds = new HashSet<>();
        for (Relationship rel : current) {
            System.out.println("current: " + rel.to().getId());
            currentIds.add(rel.to().getId());
        }
        boolean didChange = false;
        for (BaseResource toRes : requested) {
            if (currentIds.contains(toRes.getNameNodeId())) {
                LogUtils.trace(log, "createNewlySelected: found existing relationship to leave intact", toRes.getUrl());
            } else {
                LogUtils.trace(log, "createNewlySelected: found newly requested relationship to create", toRes.getUrl(), toRes.getNameNodeId());
                page.createManyToManyRelationship(relationName, toRes); // since we're manually taking care of removing old relationships we can call this method.
                didChange = true;
            }
        }
        LogUtils.trace(log, "createNewlySelected: didChange=", didChange);
        return didChange;
    }

    /**
     * Parse the given string as a comma seperated list of values. Each value
     * can be either a UUID or a path.
     *
     * The returned list will contain UUID objects and/or Path's, or be empty
     *
     * @param paramVal - must be non-null. A comma-seperated list of values
     * which can be UUID's or paths. The paths can be absolute or relative. In
     * the simplest case a path is simply the name of a resource which will be
     * in the same folder as the component
     * @return
     */
    public List parse(String paramVal) {
        if (paramVal == null || paramVal.trim().length() == 0) {
            return Collections.EMPTY_LIST;
        }
        List list = new ArrayList();
        String[] arr = paramVal.split(",");
        for (String s : arr) {
            System.out.println("parse: " + s);
            if (s.trim().length() > 0) {
                try {
                    UUID id = UUID.fromString(s);
                    list.add(id);
                } catch (IllegalArgumentException e) {
                    // Not a UUID, so look for name/path
                    Path path = Path.path(s);
                    System.out.println("not a UUID, converted to path. s=" + s + "  path=" + path);
                    list.add(path);
                }
            }
        }
        LogUtils.trace(log, "parse", paramVal, "list size", list.size());
        return list;
    }

    public String validate(List list, Templatable page, Folder selectFrom) {
        for (Object o : list) {
            String s = validateItem(o, page, selectFrom);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    private String validateItem(Object o, Templatable page, Folder selectFrom) {
        if (o instanceof UUID) {
            UUID id = (UUID) o;
            BaseResource res = ExistingResourceFactory.get(id);
            if (res == null) {
                return "Invalid value: " + id;
            }
        } else if (o instanceof Path) {
            Path p = (Path) o;
            Templatable found = ComponentUtils.find(selectFrom, p);
            if (found == null) {
                return "Invalid path: " + p;
            }
        } else {
            String s = o.toString();
            try {
                UUID id = UUID.fromString(s);
                return validateItem(id, page, selectFrom);
            } catch (IllegalArgumentException e) {
                Path path = Path.path(s);
                return validateItem(path, page, selectFrom);
            }
        }
        return null;
    }

    /**
     *
     * @param fSelectFrom - the folder to choose from, or null
     * @param selected - selected relationships, or null
     * @param selectTemplate - the template which constrains choices, or null
     * @param path - the path to the component
     * @param page - the page containing the relation select
     * @return
     */
    public String selectHtml(Folder fSelectFrom, List selected, String selectTemplate, Path path, Templatable page) {
        StringBuilder sb = new StringBuilder();
        // the selected item is just the first in the list, if the list is not-empty
        UUID relId = selected == null || selected.isEmpty() ? null : toResourceId(selected.get(0), page, fSelectFrom);
        sb.append("<select id='").append(path).append("' name='").append(path).append("'>");
        log.debug("selectFrom: " + fSelectFrom.getHref() + " - " + selectTemplate);
        String sel = relId == null ? " selected " : "";
        sb.append("<option value=''").append(sel).append(">").append("[None]").append("</option>");
        buildOptions(fSelectFrom, relId, sb, "", selectTemplate);
        sb.append("</select>");
        return sb.toString();
    }

    private void buildOptions(Folder fSelectFrom, UUID relId, StringBuilder sb, String prefix, String selectTemplate) {
        for (Templatable ct : fSelectFrom.getChildren(selectTemplate)) {
            if (ct instanceof BaseResource) {
                BaseResource res = (BaseResource) ct;
                String sel = "";
                if (relId != null && relId.equals(res.getNameNodeId())) {
                    sel = " selected ";
                }
                sb.append("<option value='").append(res.getNameNodeId().toString()).append("'").append(sel).append(">").append(prefix).append(res.getTitle()).append("</option>");
            }
        }
        for (Folder fChild : fSelectFrom.getSubFolders()) {
            buildOptions(fChild, relId, sb, prefix + " - " + fSelectFrom.getName(), selectTemplate);
        }
    }

    public List<BaseResource> toResources(List list, Templatable page, Folder selectFrom) {
        List<BaseResource> resources = new ArrayList<>();
        for (Object o : list) {
            BaseResource r = toResource(o, page, selectFrom);
            if (r != null) {
                resources.add(r);
            } else {
                throw new RuntimeException("Could not locate resource: " + o);
            }
        }
        return resources;
    }

    private BaseResource toResource(Object o, Templatable page, Folder selectFrom) throws IllegalArgumentException {
        if (o == null) {
            return null;
        } else if (o instanceof UUID) {
            UUID id = (UUID) o;
            return ExistingResourceFactory.get(id);
        } else if (o instanceof Path) {
            Path p = (Path) o;
            if (selectFrom == null) {
                if (p.isRelative()) {
                    throw new IllegalArgumentException("selectFromFolder is null and the path given is relative. Please enter a selctFromFolder value or a UUID or an absolute path: " + p);
                } else {
                    selectFrom = page.getHost();
                }
            }
            Templatable found = ComponentUtils.find(selectFrom, p);
            if (found == null) {
                throw new RuntimeException("Couldnt find path: " + p + " from folder: " + selectFrom.getUrl());
            } else if (found instanceof BaseResource) {
                return (BaseResource) found;
            } else {
                throw new RuntimeException("Found a resource which is not an appropriate type: " + found.getClass() + " at " + found.getUrl());
            }
        } else {
            String s = o.toString();
            try {
                UUID id = UUID.fromString(s);
                return toResource(id, page, selectFrom);
            } catch (IllegalArgumentException e) {
                Path path = Path.path(s);
                return toResource(path, page, selectFrom);
            }
        }
    }

    private UUID toResourceId(Object o, Templatable page, Folder selectFrom) throws IllegalArgumentException {
        if (o == null) {
            return null;
        } else if (o instanceof UUID) {
            UUID id = (UUID) o;
            return id;
        } else if (o instanceof Path) {
            Path p = (Path) o;
            if (selectFrom == null) {
                if (p.isRelative()) {
                    throw new IllegalArgumentException("selectFromFolder is null and the path given is relative. Please enter a selctFromFolder value");
                } else {
                    selectFrom = page.getHost();
                }
            }
            Templatable found = ComponentUtils.find(selectFrom, p);
            if (found == null) {
                return null;
            }
        } else {
            String s = o.toString();
            try {
                UUID id = UUID.fromString(s);
                return toResourceId(id, page, selectFrom);
            } catch (IllegalArgumentException e) {
                Path path = Path.path(s);
                return toResourceId(path, page, selectFrom);
            }
        }
        return null;
    }
}
