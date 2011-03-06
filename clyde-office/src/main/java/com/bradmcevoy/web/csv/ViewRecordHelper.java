package com.bradmcevoy.web.csv;

import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Templatable;

/**
 *
 * @author brad
 */
public class ViewRecordHelper {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ViewRecordHelper.class );

    private static final ViewRecordHelper theInstance = new ViewRecordHelper();

    public static ViewRecordHelper getInstance() {
        return theInstance;
    }



    public ViewRecord toRecords(Select rootSelect, Folder source) {
        log.trace("toRecords");
        ViewRecord rootRecord = new ViewRecord(null, null, null);
        populate(rootSelect, rootRecord, source);
        return rootRecord;
    }

    private void populate(Select select, ViewRecord parent, BaseResource source) {
        log.trace("populate");
        if (source instanceof Folder) {
            Folder folder = (Folder) source;
            for (Templatable res : folder.getChildren(select.getType())) {
                if (res instanceof BaseResource) {
                    BaseResource bres = (BaseResource) res;
                    ViewRecord rec = new ViewRecord(parent, bres, select);
                    parent.add(rec);
                    if (select.getSubSelect() != null) {
                        populate(select.getSubSelect(), rec, bres);
                    }
                }
            }
        }
    }
}
