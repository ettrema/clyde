package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.process.ClydeTransition;
import com.bradmcevoy.process.ProcessContext;
import com.bradmcevoy.process.TokenValue;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.WrappedSubPage;
import java.util.Map;
import org.jdom.Element;

/**
 *
 */
public class ScanCommand extends Command {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScanCommand.class);
    private static final long serialVersionUID = 1L;

    public ScanCommand(Addressable container, String name) {
        super(container, name);
    }

    public ScanCommand(Addressable container, Element el) {
        super(container, el);
    }

    public boolean validate(RenderContext rc) {
        return ComponentUtils.validateComponents(this, rc);
    }

    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        String s = parameters.get(this.getName());
        if (s == null) {
            return null; // not this command
        }
        Addressable cn = this.getContainer();
        WrappedSubPage thisPage = (WrappedSubPage) cn;
        ClydeTransition tr = (ClydeTransition) thisPage.unwrap();
        com.bradmcevoy.process.State fromState = tr.getFromState();
        com.bradmcevoy.process.Process process = fromState.getProcess();
        Templatable ct = rc.getTargetPage();
        log.debug("target page: " + ct.getHref());
        TokenValue token = getTokenValueFromTransition(ct);
        ProcessContext pc = new ProcessContext(token, process);
        BaseResource pageToSave = (BaseResource) token.getContainer();
        if (pc.scan()) {
            log.debug("transitioned to: " + token.getStateName());
            pageToSave.save();
            pageToSave.commit();
            return token.getHref();
        } else {
            log.debug("did not transition");
            return null;
        }
    }

    public TokenValue getTokenValueFromTransition(Templatable ctWrappedTransition) {
        log.debug("getTokenValueFromTransition: " + ctWrappedTransition.getHref());
        WrappedSubPage wrappedState = parentFromWrapped(ctWrappedTransition);
        log.debug("wrapped state: " + wrappedState.getHref());
        return (TokenValue) wrappedState.getParent();
//        TokenValue tv = (TokenValue) parentFromWrapped(wrappedState).unwrap();
//        return tv;
    }

    public WrappedSubPage parentFromWrapped(Templatable ct) {
        if (ct instanceof WrappedSubPage) {
            WrappedSubPage wsp = (WrappedSubPage) ct;
            if( wsp.getParent() instanceof WrappedSubPage ) {
                return (WrappedSubPage) wsp.getParent();
            } else {
                throw new RuntimeException("Parent is not a WrappedSubPage. Is a: " + wsp.getParent().getClass().getCanonicalName() + " - " + wsp.getParent().getHref());
            }
        } else {
            throw new RuntimeException("Expected the page to be a WrappedSubPage, not a: " + ct.getClass().getName());
        }
    }

    @Override
    protected String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
