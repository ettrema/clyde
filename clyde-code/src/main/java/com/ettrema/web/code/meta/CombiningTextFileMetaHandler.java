package com.ettrema.web.code.meta;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.ettrema.scheduled.ScheduleTask;
import com.ettrema.utils.JDomUtils;
import com.ettrema.web.CombiningTextFile;
import com.ettrema.web.Folder;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.MetaHandler;
import java.util.List;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author bradm
 */
public class CombiningTextFileMetaHandler implements MetaHandler<CombiningTextFile> {

    public static final String ALIAS = "combiner";
    private final BaseResourceMetaHandler baseResourceMetaHandler;

    public CombiningTextFileMetaHandler(BaseResourceMetaHandler baseResourceMetaHandler) {
        this.baseResourceMetaHandler = baseResourceMetaHandler;
    }

    @Override
    public Class getInstanceType() {
        return ScheduleTask.class;
    }

    @Override
    public boolean supports(Resource r) {
        return r instanceof CombiningTextFile;
    }

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public Element toXml(CombiningTextFile r) {
        Element elRoot = new Element(ALIAS, CodeMeta.NS);
        populateXml(elRoot, r);
        return elRoot;
    }

    @Override
    public CombiningTextFile createFromXml(CollectionResource parent, Element d, String name) {
        CombiningTextFile f = new CombiningTextFile((Folder) parent, name);
        updateFromXml(f, d);
        return f;
    }

    private void populateXml(Element el, CombiningTextFile page) {
        String sIncludes = null;
        List<Path> includes = page.getIncludes();
        if( includes != null ) {
            for( Path name : includes ) {
                if( sIncludes != null ) sIncludes += ",";
                else sIncludes = "";
                sIncludes += name;
            }
        }
        JDomUtils.setChildText(el, "includes", sIncludes, CodeMeta.NS);
        JDomUtils.setChildText(el, "includesExt", page.getIncludeExt(), CodeMeta.NS);
        baseResourceMetaHandler.populateXml(el, page);
    }

    @Override
    public void updateFromXml(CombiningTextFile r, Element el) {
        baseResourceMetaHandler.updateFromXml(r, el);
        String includes = JDomUtils.valueOf(el, "includes", CodeMeta.NS);
        r.setIncludes(includes);
        String includesExt = JDomUtils.valueOf(el, "includeExt", CodeMeta.NS);
        r.setIncludeExt(includesExt);
        r.save();
    }

    @Override
    public void applyOverrideFromXml(CombiningTextFile r, Element el) {
        baseResourceMetaHandler.updateFromXml(r, el);
        r.save();
    }
	
}
