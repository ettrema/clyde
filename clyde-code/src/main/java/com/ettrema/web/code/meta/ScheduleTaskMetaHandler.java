package com.ettrema.web.code.meta;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.scheduled.ScheduleTask;
import com.ettrema.web.Folder;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import org.jdom.Element;

/**
 *
 * @author bradm
 */
public class ScheduleTaskMetaHandler implements MetaHandler<ScheduleTask> {

    public static final String ALIAS = "scheduledtask";
    private final PageMetaHandler pageMetaHandler;

    public ScheduleTaskMetaHandler(PageMetaHandler pageMetaHandler) {
        this.pageMetaHandler = pageMetaHandler;
    }

    public Class getInstanceType() {
        return ScheduleTask.class;
    }

    public boolean supports(Resource r) {
        return r instanceof ScheduleTask;
    }

    public String getAlias() {
        return ALIAS;
    }

    public Element toXml(ScheduleTask r) {
        Element elRoot = new Element(ALIAS, CodeMeta.NS);
        populateXml(elRoot, r);
        return elRoot;
    }

    public ScheduleTask createFromXml(CollectionResource parent, Element d, String name) {
        ScheduleTask f = new ScheduleTask((Folder) parent, name);
        updateFromXml(f, d);
        return f;
    }

    private void populateXml(Element el, ScheduleTask page) {
        page.populateFieldsInXml(el);
        pageMetaHandler.populateXml(el, page);
    }

    public void updateFromXml(ScheduleTask r, Element d) {
        pageMetaHandler.updateFromXml(r, d);
        r.loadFieldsFromXml(d);
        r.save();
    }
	
}
