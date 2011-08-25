package com.bradmcevoy.query.persistence;

import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Page;

/**
 *
 * @author bradm
 */
public class ScheduleTask extends Page {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScheduleTask.class);
    private static final long serialVersionUID = 1L;


    public ScheduleTask(Folder parentFolder, String name) {
        super(parentFolder, name);
    }

    
}
