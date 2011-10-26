package com.bradmcevoy.process;

import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.*;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.SubPage;
import com.ettrema.context.RequestContext;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdom.Document;
import org.jdom.Element;


import static com.ettrema.context.RequestContext._;

/**
 * Contains one process
 * 
 * @author brad
 */
public class ProcessDef extends SubPage implements ComponentDef, com.bradmcevoy.process.Process {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProcessDef.class);
    private static final long serialVersionUID = 1L;
    public static final String VAR_RES_ID = "resId";

    /**
     * Scan each Token defined in the given page
     * 
     * Returns true if a change was made
     * 
     * @param res
     */
    public static boolean scan(BaseResource res) {
        return scan(res, 0);
    }

    
    private  static boolean scan(BaseResource res, int count) {
        log.debug( "scan: " + res.getPath() + " count: " + count);
        if(count > 10) {
            throw new RuntimeException( "Excessive rescannign detected, aborting");
        }
        boolean didChange = false;
        List<ProcessContext> contexts = new ArrayList<ProcessContext>();
        for (ComponentValue cv : res.getValues().values()) {
            ProcessContext context = createContext(cv, res);
            if( context != null ) {
                contexts.add( context );
            }
        }

        for (ProcessContext context : contexts) {
            Boolean b = context.scan();
            log.debug( "state: " + context.token.getStateName() );
            didChange = didChange || b;
        }
        if( didChange) {
            // since one process changing state can affect another, we
            // should rescan
            scan(res, count+1);
            log.debug( "did transition so saving resource: " + res.getPath());
            res.save();
        }
        return didChange;
    }

    public static boolean scan(ComponentValue cv, BaseResource res) {
        ProcessContext context = createContext(cv, res);
        if( context == null ) return false;

        if( context.token == null ) {
            log.warn( "context.token is null, can't scan " + cv.getName());
            return false;
        } else {
            log.debug( "scanning context: current state: " + context.token.getStateName());
        }
        boolean b = context.scan();
        log.debug( "state: " + context.token.getStateName() );
        return b;
    }

    public static ProcessContext createContext(ComponentValue cv, BaseResource res) {
        ComponentDef def = cv.getDef(res);        
        if( !(def instanceof ProcessDef) ) return null;
        if( cv.getValue() == null ) {
            throw new RuntimeException( "CV does not contain a value: " + cv.getName());
        }
        
        ProcessDef pdef = (ProcessDef) def;
        Object o = cv.getValue();
        Token t = (Token) o;
        Process process = pdef.process;
        TimerService timerService = _(TimerService.class);
        ProcessContext context = new ProcessContext(t, process, timerService);
        context.addAttribute("res", res);
        context.addAttribute("cv", cv);
        context.addAttribute("def", pdef);
        return context;
    }

    public static BaseResource getTokensResource(ProcessContext processContext) {
        UUID id = (UUID) processContext.token.getVariables().get(ProcessDef.VAR_RES_ID);
        if (id == null) {
            throw new NullPointerException("no resource id was supplied");
        }
        RequestContext req = RequestContext.getCurrent();
        if (processContext == null) {
            throw new NullPointerException("No RequestContext in scope. Have you done RootContext.execute?");
        }
        VfsSession sess = req.get(VfsSession.class);
        if (sess == null) {
            throw new NullPointerException("No VfsSession in scope. Have you configured it in catalog.xml?");
        }
        NameNode n = sess.get(id);
        if (n == null) {
            throw new NullPointerException("Could not find namenode: " + id);
        }
        BaseResource res = (BaseResource) n.getData();
        if (res == null) {
            throw new NullPointerException("No data node for name node: " + id);
        }
        return res;
    }

    
    private ProcessImpl process;

    public ProcessDef(CommonTemplated parent, String name) {
        super(parent, name);
        process = new ClydeProcess();
    }

    public ProcessDef(Addressable container, Element el) {
        super(container, el);
        log.debug("creating process from xml");
        process = new ClydeProcess(el);
        loadXml(el);
    }

    @Override
    public void loadXml(Element el) {
        process.loadXml(el);
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        Element elThis = new Element("componentDef");
        el.addContent(elThis);
        elThis.setAttribute("class", getClass().getName());
        elThis.setAttribute("name", getName());

        process.populateXml(elThis);
        return elThis;
    }

    @Override
    public boolean validate(ComponentValue c, RenderContext rc) {
        return true;
    }

    @Override
    public String render(ComponentValue c, RenderContext rc) {
        log.debug("render");
        return null;
    }

    @Override
    public String renderEdit(ComponentValue c, RenderContext rc) {
        log.debug("renderEdit");
        return null;
    }

    public void onPreProcess(ComponentValue componentValue, RenderContext rc, Map<String, String> parameters, Map<String, com.bradmcevoy.http.FileItem> files) {
        
    }

    @Override
    public ComponentValue createComponentValue(Templatable tr) {
        BaseResource newRes = (BaseResource) tr;
        TokenValue t = startProcess(newRes);
        t.getVariables().put(VAR_RES_ID, newRes.getId());
        ComponentValue cv = new ComponentValue(getName(), tr);
        cv.setValue( t );
        t.setComponentValue(cv);
        return cv;
    }

    @Override
    public Object parseValue(ComponentValue cv, Templatable ct, String s) {
        log.debug( "parseValue: " + this.getName());
        Token t = TokenUtils.parse(s);
        TokenValue tv  = new TokenValue((BaseResource) ct, this.getName(), t);
        tv.setComponentValue( cv);
        return tv;
    }

    public Object parseValue(ComponentValue cv, Templatable ct, Element elValue) {
        String sVal = InitUtils.getValue( elValue );
        // TODO: should parse xml directly
        return parseValue(cv, ct, sVal);
    }


    public Class getValueClass() {
        return TokenValue.class;
    }



    @Override
    public String formatValue(Object v) {
        if( v instanceof Token) {
            Token t = (Token) v;
            return t.toString();
        } else {
            return v.toString();
        }
    }

    @Override
    public State createState(String stateName) {
        return process.createState(stateName);
    }

    @Override
    public Transition createTransition(State fromState, Element el) {
        return process.createTransition(fromState, el);
    }

    @Override
    public Transition createTransition(String transitionName, State fromState, State toState, Rule rule) {
        return process.createTransition(transitionName, fromState, toState, rule);
    }

    @Override
    public State getState(String stateName) {
        return process.getState(stateName);
    }

    @Override
    public void populateXml(Element el) {
        super.populateXml(el);
        process.populateXml(el);
    }

    @Override
    public void setStartState(State start) {
        process.setStartState(start);
    }

    @Override
    public Token startProcess() {
        return process.startProcess();
    }

    public TokenValue startProcess(BaseResource newRes) {
        Token t = startProcess();
        return new TokenValue(newRes, this.getName(), t);
    }

    @Override
    public Element toXml(Element el) {
        Element elThis = new Element("process");
        el.addContent(elThis);
        populateXml(elThis);
        return elThis;
    }

    @Override
    public Document toXmlDoc() {
        return process.toXmlDoc();
    }

    @Override
    public Resource getChildResource(String name) {
        State s = this.getState(name);
        return (Resource) s;
    }

    @Override
    public void walkStates() {
        this.process.walkStates();
    }

    public void changedValue(ComponentValue cv) {
        
    }

    public ProcessImpl getProcess() {
        return process;
    }

    public void setProcess(ProcessImpl process) {
        this.process = process;
    }



    public class ClydeProcess extends ProcessImpl {

        private static final long serialVersionUID = 1L;

        ClydeProcess(Element el) {
            super(el);
        }

        public ClydeProcess() {
            super(ProcessDef.this.getName());
        }

        @Override
        public String getName() {
            return ProcessDef.this.getName();
        }

        @Override
        public State createState(String stateName) {
            return new ClydeState(ProcessDef.this, stateName);
        }

        @Override
        public Transition createTransition(State fromState, Element el) {
            String name = el.getAttributeValue("name");
            log.debug("creating clydetransition: " + name);
            ClydeState lFromState = (ClydeState) process.getState(fromState.getName());
            if (lFromState == null) {
                throw new RuntimeException("Couldnt find state: " + fromState.getName());
            }
            Transition t = new ClydeTransition(lFromState, name);
            t.loadFromXml(el);
            return t;
        }

        @Override
        public Transition createTransition(String transitionName, State fromState, State toState, Rule rule) {
            ClydeState lFromState = (ClydeState) process.getState(fromState.getName());
            if (lFromState == null) {
                throw new RuntimeException("Couldnt find state: " + fromState.getName());
            }
            Transition t = new ClydeTransition(lFromState, transitionName, toState, rule);
            log.debug("creating clydetransition2: " + t.getName());
            return t;
        }

        public ProcessDef getProcessDef() {
            return ProcessDef.this;
        }
    }
}
