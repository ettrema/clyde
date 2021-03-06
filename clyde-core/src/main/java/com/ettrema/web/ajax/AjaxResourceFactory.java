package com.ettrema.web.ajax;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.ITemplate;
import com.ettrema.web.NewPage;
import com.ettrema.web.RenderContext;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.ComponentUtils;
import com.ettrema.web.component.ComponentValue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.apache.commons.lang.StringUtils;

/**
 * Supports invoking components via ajax, with response as JSON data
 *
 * Eg post to /users/_autoname.new/.ajax to create a new user via an ajax call,
 * with any validation errors returned as ajax
 *
 * @author brad
 */
public class AjaxResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AjaxResourceFactory.class);
    private static final String NAME = ".ajax";
    private static final String ENDS_WITH = "/" + NAME;
    private final ResourceFactory wrapped;

    public AjaxResourceFactory(ResourceFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Resource getResource(String host, String path) throws NotAuthorizedException, BadRequestException {
        if (path.endsWith(ENDS_WITH)) {
            Path p = Path.path(path);
            if (p.getParent() == null) {
                log.trace("not ajax request, because path parent is null");
                return null;
            }
            Path pRes = p.getParent();
            log.trace("is ajax request. resource path: " + pRes);
            Resource res = wrapped.getResource(host, pRes.toString());
            if (res == null) {
                log.trace("no parent resource found");
                return null;
            } else if (res instanceof CommonTemplated) {
                log.trace("found a resource");
                CommonTemplated ct = (CommonTemplated) res;
                return new AjaxPostResource(new ExistingResourceAccessor(ct));
            } else if (res instanceof NewPage) {
                log.trace("found a new page resource");
                NewPage np = (NewPage) res;
                return new AjaxPostResource(new NewPageResourceAccessor(np));
            } else {
                log.warn("unsupported resource type: " + res.getClass());
                return null;
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("getResource: not an ajax request: " + path);
            }
            return null;
        }
    }

    private interface ResourceAccessor {

        CommonTemplated get(Map<String, String> parameters);

        Object authenticate(String user, String password);

        Object authenticate(DigestResponse digestRequest);

        boolean isDigestAllowed();

        boolean authorise(Request request, Method method, Auth auth);

        String getRealm();
    }

    private class ExistingResourceAccessor implements ResourceAccessor {

        private final CommonTemplated res;

        public ExistingResourceAccessor(CommonTemplated res) {
            this.res = res;
        }

        @Override
        public CommonTemplated get(Map<String, String> parameters) {
            return res;
        }

        @Override
        public Object authenticate(String user, String password) {
            return res.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return res.authorise(request, method, auth);
        }

        @Override
        public String getRealm() {
            return res.getRealm();
        }

        @Override
        public Object authenticate(DigestResponse digestRequest) {
            return res.authenticate(digestRequest);
        }

        @Override
        public boolean isDigestAllowed() {
            return res.isDigestAllowed();
        }
    }

    private class NewPageResourceAccessor implements ResourceAccessor {

        private final NewPage newPage;

        public NewPageResourceAccessor(NewPage newPage) {
            this.newPage = newPage;
        }

        @Override
        public CommonTemplated get(Map<String, String> parameters) {
            return newPage.getEditee(parameters);
        }

        @Override
        public Object authenticate(String user, String password) {
            return newPage.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return newPage.authorise(request, method, auth);
        }

        @Override
        public String getRealm() {
            return newPage.getRealm();
        }

        @Override
        public Object authenticate(DigestResponse digestRequest) {
            return newPage.authenticate(digestRequest);
        }

        @Override
        public boolean isDigestAllowed() {
            return newPage.isDigestAllowed();
        }
    }

    public class AjaxPostResource implements PostableResource, DigestResource {

        private final ResourceAccessor accessor;

        private AjaxPostResource(ResourceAccessor accessor) {
            this.accessor = accessor;
        }

        /**
         * Note that we can't just delegate to the underlying page because we
         * need access to the component values, so we can get their validation
         * messages
         *
         * @param parameters
         * @param files
         * @return
         * @throws BadRequestException
         * @throws NotAuthorizedException
         * @throws ConflictException
         */
        @Override
        public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
            log.trace("processForm");
            preProcess(null, parameters, files);
            String s = process(null, parameters, files);
            return s;
        }

        public void preProcess(RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files) {
            CommonTemplated res = accessor.get(parameters);
            if (res == null) {
                throw new RuntimeException("No resource was created. Was a template specified?");
            }
            ITemplate lTemplate = res.getTemplate();
            RenderContext rc = new RenderContext(lTemplate, res, rcChild, false);
            if (lTemplate != null) {
                lTemplate.preProcess(rc, parameters, files);
                for (ComponentDef def : lTemplate.getComponentDefs().values()) {
                    if (!res.getValues().containsKey(def.getName())) {
                        ComponentValue cv = def.createComponentValue(res);
                        res.getValues().add(cv);
                    }
                }
            }
            for (String paramName : parameters.keySet()) {
                Path path = Path.path(paramName);
                Component c = rc.findComponent(path);
                if (c != null) {
                    if (log.isTraceEnabled()) {
                        log.trace("bind parameter: " + paramName + " to component " + c.getClass());
                    }
                    c.onPreProcess(rc, parameters, files);
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("parameter did not bind: " + paramName);
                    }
                }

            }
        }

        public String process(RenderContext rcChild, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException {
            CommonTemplated res = accessor.get(parameters);
            LogUtils.info(log, "process. resource name=", res.getName(), "template=", res.getTemplateName(), "class=", res.getClass());
            ITemplate lTemplate = res.getTemplate();
            RenderContext rc = new RenderContext(lTemplate, res, rcChild, false);

            for (String paramName : parameters.keySet()) {
                Path path = Path.path(paramName);
                Component c = rc.findComponent(path);
                if (c instanceof ComponentValue) {
                    ComponentValue cv = (ComponentValue) c;
                    ComponentDef def = cv.getDef(rc);
                    if( def != null ) {
                        def.changedValue(cv);
                    }
                }
            }


            boolean componentFound = false;
            for (String paramName : parameters.keySet()) {
                log.info("found request param: " + paramName);
                Path path = Path.path(paramName);
                Component c = rc.findComponent(path);
                if (c != null) {
                    log.info("process component: " + c);
                    String redirect = c.onProcess(rc, parameters, files);
                    if (redirect != null) {
                        // ignore redirects, but redirects cause processing to stop
                        componentFound = true;
                        break;
                    }
                }
            }

            if (!componentFound) {
                log.warn("form post occurred, but did not process any components. Check that you have sent a component identified");
            }
            return null;
        }

        @Override
        public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
            Map<String, Object> errors = new HashMap<>();
            CommonTemplated res = accessor.get(params);

            for (Component c : ComponentUtils.getAllComponents(res)) {
                String v = c.getValidationMessage();
                if (!StringUtils.isEmpty(v)) {
                    errors.put(c.getName(), v);
                }
            }
            boolean allOk = errors.isEmpty();
            errors.put("result", allOk ? "ok" : "err");
            JsonConfig cfg = new JsonConfig();
            cfg.setIgnoreTransientFields(true);
            cfg.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);

            if (allOk) {
                Map<String, Object> values = new HashMap<>();
                for (ComponentValue cv : res.getValues().values()) {
                    Object o = cv.getValue();
                    if (o instanceof WorkflowFactor) {
                        WorkflowFactor factor = (WorkflowFactor) o;
                        values.put(cv.getName(), factor.getWorkflowAdvice());
                    }
                }
                values.put("url", res.getUrl());
                errors.put("data", values);
            }

            JSON json = JSONSerializer.toJSON(errors, cfg);
            Writer writer = new PrintWriter(out);
            json.write(writer);
            writer.flush();
        }

        @Override
        public Long getMaxAgeSeconds(Auth auth) {
            return null;
        }

        @Override
        public String getContentType(String accepts) {
            return "application/x-javascript; charset=utf-8";
        }

        @Override
        public Long getContentLength() {
            return null;
        }

        @Override
        public String getUniqueId() {
            return null;
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Object authenticate(String user, String password) {
            return accessor.authenticate(user, password);
        }

        @Override
        public boolean authorise(Request request, Method method, Auth auth) {
            return accessor.authorise(request, method, auth);
        }

        @Override
        public String getRealm() {
            return accessor.getRealm();
        }

        @Override
        public Date getModifiedDate() {
            return null;
        }

        @Override
        public String checkRedirect(Request request) {
            return null;
        }

        @Override
        public Object authenticate(DigestResponse digestRequest) {
            return accessor.authenticate(digestRequest);
        }

        @Override
        public boolean isDigestAllowed() {
            return accessor.isDigestAllowed();
        }
    }
}
