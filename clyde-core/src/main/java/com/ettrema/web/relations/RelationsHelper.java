package com.ettrema.web.relations;

import com.bradmcevoy.common.Path;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.*;
import com.ettrema.web.component.ComponentUtils;
import com.ettrema.web.component.ComponentValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author brad
 */
public class RelationsHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RelationsHelper.class);
    
    /**
     * Build a list of selected resources based on the value given. This value
     * can be of several types, usually a list of UUIDs
     * 
     * @param val
     * @param page
     * @param selectFrom
     * @return 
     */
    public List<BaseResource> findResources(Object val, Templatable page, Folder selectFrom) {
        UUID id;
        if (val == null) {
            return null;
        }
        List<BaseResource> list = new ArrayList<>();
        addResources(val.toString(), page, list, selectFrom);
        return list;
    }

    private void addResources(Object val, Templatable page, List<BaseResource> list, Folder selectFrom) {
        UUID id;
        if (val == null) {
            return;
        } else if (val instanceof Path) {
            Path path = (Path) val;
            Templatable found = ComponentUtils.find(selectFrom, path);
            if (found == null) {
                LogUtils.trace(log, "findResource: Could not find path", path);
            } else if (found instanceof BaseResource) {
                BaseResource res = (BaseResource) found;
                list.add(res);
            } else {
                LogUtils.info(log, "addResources: found a resource which is not a baseresource (ie its a subpage or component) so will be excluded", found.getHref());
                return;
            }

        } else if (val instanceof String) {
            for (String sVal : val.toString().split(",")) {
                try {
                    id = UUID.fromString(sVal);
                    addResources(id, page, list, selectFrom);
                } catch (IllegalArgumentException e) {
                    // Not a UUID, so look for name/path
                    Path path = Path.path(sVal);
                    addResources(path, page, list, selectFrom);
                }
            }
        } else if (val instanceof UUID) {
            id = (UUID) val;
            BaseResource res = ExistingResourceFactory.get(id);
            if (res == null) {
                log.warn("no resource found with id: " + id);
                return;
            } else {
                list.add(res);
            }
        } else if (val instanceof List) {
            for (Object listVal : (List) val) {
                addResources(listVal, page, list, selectFrom);
            }
        } else {
            log.warn("unknown value type: " + val.getClass());
        }
    }    
    
    public List<UUID> getRelationIdsFromRequest(RenderContext rc, Map<String, String> parameters, Path compPath, Folder selectFrom) {
        if (parameters == null) {
            return null;
        }
        
        String key = compPath.toString();
        if (!parameters.containsKey(key)) {
            return null;
        }
        String s = parameters.get(key);
        Object value = parseValue(rc.page, s, selectFrom);
        if (value != null && !(value instanceof List)) {
            return null;
        }
        List<UUID> ids = (List<UUID>) value;
        return ids;
    }
    
    public Object parseValue(Templatable ct, String s, Folder selectFrom) {
        if (StringUtils.isNotBlank(s)) {
            List<BaseResource> list = findResources(s, ct, selectFrom);
            if (list != null && !list.isEmpty()) {
                List<UUID> ids = new ArrayList<>();
                for (BaseResource res : list) {
                    ids.add(res.getNameNodeId());
                }
                return ids;
            } else {
                log.warn("not found: " + s);
                // return the invalid value so it can be used in validation. Not that
                // efficient, should set some temporary value
                return s;
            }
        } else {
            return null;
        }
    }    
    
    public boolean updateRelation(ComponentValue componentValue, Templatable page, String s, Folder selectFrom, String relationName) {
        Object value = parseValue(page, s, selectFrom);
        if (value != null && !(value instanceof UUID)) {
            log.trace("not a valid uuid, so dont do anything");
            componentValue.setValue(value);
            return true;
        }
        UUID id = (UUID) value;
        BaseResource res = (BaseResource) page;
        boolean found = false;
        BaseResource existingBaseRes = res.getRelation(relationName);
        if (existingBaseRes != null) {
            if (!existingBaseRes.getNameNodeId().equals(id)) {
                // same relationship to somewhere else, so remove it
                if (log.isDebugEnabled()) {
                    log.debug("remove relationship: " + relationName + " from: " + existingBaseRes.getHref());
                }
                res.removeRelationship(relationName);
            } else {
                // already exists, do nothing
                log.trace("relationship exists, so do nothing");
                found = true;
            }
        } else {
            if (id == null) {
                log.trace("selected value is null, and no current value. So do nothing");
                found = true;
            } else {
                log.trace("current relationship doesnt exist, but value is selected. Create.");
            }
        }
        if (!found) {
            if (id != null) {
                BaseResource dest = res.findByNameNodeId(id);
                if (log.isDebugEnabled()) {
                    log.debug("create relationship: " + relationName + " to: " + dest.getHref());
                }
                res.createRelationship(relationName, dest);
                componentValue.setValue(id);
            }
        }
        return false;
    }    
    
    public void buildChecks(Folder fSelectFrom, List<UUID> relIds, StringBuilder sb, String prefix, String selectTemplate) {
        for (Templatable ct : fSelectFrom.getChildren(selectTemplate)) {
            if (ct instanceof BaseResource) {
                BaseResource res = (BaseResource) ct;
                String sel = "";
                if (relIds != null && relIds.contains(res.getNameNodeId())) {
                    sel = " checked='true' ";
                }
                sb.append("<input type='checkbox' value='").append(res.getNameNodeId().toString()).append("'").append(sel).append("/>").append("<label>").append(prefix).append(res.getTitle()).append("</label>");
            }
        }
        for (Folder fChild : fSelectFrom.getSubFolders()) {
            String newPrefix = prefix + fSelectFrom.getName() + " - ";
            buildChecks(fChild, relIds, sb, newPrefix + fSelectFrom.getName(), selectTemplate);
        }

    }

    public void buildOptions(Folder fSelectFrom, UUID relId, StringBuilder sb, String prefix, String selectTemplate) {
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
    
}
