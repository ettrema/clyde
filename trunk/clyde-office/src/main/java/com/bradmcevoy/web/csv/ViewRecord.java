package com.bradmcevoy.web.csv;

import com.bradmcevoy.web.BaseResource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a node in a view tree
 */
public class ViewRecord {

    private final ViewRecord parent;
    private final BaseResource res;
    private final Select select;
    private List<ViewRecord> children = new ArrayList<ViewRecord>();
    private Map<String, ViewRecord> mapOfChildren = new HashMap<String, ViewRecord>();
    /**
     * Records if the record was found in an update request.
     */
    private boolean updated;

    public ViewRecord(ViewRecord parent, BaseResource res, Select select) {
        this.parent = parent;
        this.res = res;
        this.select = select;
    }

    public void add(ViewRecord child) {
        children.add(child);
        mapOfChildren.put(child.getRes().getName(), child);
    }

    public Collection<ViewRecord> getChildren() {
        return children;
    }

    public BaseResource getRes() {
        return res;
    }

    public Select getSelect() {
        return select;
    }

    public ViewRecord getParent() {
        return parent;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public ViewRecord child(String name) {
        return mapOfChildren.get(name);
    }
}
