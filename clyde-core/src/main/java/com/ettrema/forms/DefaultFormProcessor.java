package com.ettrema.forms;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.ITemplate;
import com.ettrema.web.RenderContext;
import java.util.Map;

/**
 *
 * @author brad
 */
public class DefaultFormProcessor implements FormProcessor {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultFormProcessor.class);

    @Override
    public String processForm(CommonTemplated target, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
        String redirect = target.doOnPost(parameters, files);
        if (redirect != null) {
            LogUtils.info(log, "processForm", target.getName(), "redirecting to", redirect);
            return redirect;
        }
        ITemplate template = target.getTemplate();
        RenderContext rc = new RenderContext( template, target, null, true );
        target.preProcess(rc, parameters, files);
        String s = target.process(rc, parameters, files);
        LogUtils.info(log, "processForm", target.getName(), "process result", s);
        return s;
    }
}
