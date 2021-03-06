package com.ettrema.web.console2;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.RenderContext;
import com.ettrema.web.User;
import com.ettrema.web.component.Addressable;
import com.ettrema.web.component.CommonComponent;
import com.ettrema.console.Result;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

public class Eval extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Eval.class);
    User theUser;

    Eval(List<String> args, String host, String currentDir, User theUser, ResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
        this.theUser = theUser;
    }

    @Override
    public Result execute() {
        if (args.isEmpty()) {
            return result("missing expression");
        }
        String exp = "";
        for (String s : args) {
            exp += s + " ";
        }

        log.debug("eval: " + exp);

        Resource r;
        try {
            r = currentResource();
        } catch (NotAuthorizedException | BadRequestException ex) {
            return result("Not authorised, or bad request");
        }
        if (r instanceof CommonTemplated) {
            CommonTemplated t = (CommonTemplated) r;
            RenderContext rc = new RenderContext(t.getTemplate(), t, null, false);
            EvalComponent comp = new EvalComponent(exp);
            String s = comp.render(rc);
            this.commit();
            return result(s);
        } else {
            return result("cannot eval against this type: " + r.getClass());
        }
    }

    private class EvalComponent extends CommonComponent {

        private static final long serialVersionUID = 1L;
        final String template;

        public EvalComponent(String template) {
            this.template = template;
        }

        @Override
        public void init(Addressable container) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Addressable getContainer() {
            try {
                return (Addressable) Eval.this.currentResource();
            } catch (NotAuthorizedException | BadRequestException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean validate(RenderContext rc) {
            return true;
        }

        @Override
        public String render(RenderContext rc) {
            return _render(template, velocityContext(rc, "some_value"));
        }

        @Override
        public String renderEdit(RenderContext rc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getName() {
            return "eval";
        }

        @Override
        public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Element toXml(Addressable container, Element el) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
