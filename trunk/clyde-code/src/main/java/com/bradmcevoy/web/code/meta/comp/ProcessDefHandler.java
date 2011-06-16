package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.process.ClydeState;
import com.bradmcevoy.process.ClydeTransition;
import com.bradmcevoy.process.ProcessDef;
import com.bradmcevoy.process.ProcessImpl;
import com.bradmcevoy.process.State;
import com.bradmcevoy.process.StateImpl;
import com.bradmcevoy.process.Transition;
import com.bradmcevoy.web.Template;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.InitUtils;
import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class ProcessDefHandler implements ComponentDefHandler {

    private final static String ALIAS = "process";
    private final SubPageHandler subPageHandler;

    public ProcessDefHandler(SubPageHandler subPageHandler) {
        this.subPageHandler = subPageHandler;
    }

    public String getAlias() {
        return ALIAS;
    }

    public ComponentDef fromXml(Template res, Element el) {
        String name = el.getAttributeValue("name");
        ProcessDef def = new ProcessDef(res, name);
        fromXml(el, def);
        return def;
    }

    public Element toXml(ComponentDef def, Template template) {
        ProcessDef html = (ProcessDef) def;
        Element el = new Element(ALIAS, CodeMeta.NS);
        populateXml(el, html);
        return el;
    }

    public void populateXml(Element el, ProcessDef pdef) {
        InitUtils.set(el, "name", pdef.getName());
        subPageHandler.populateXml(el, pdef);
        ProcessImpl process = pdef.getProcess();
        populateXml(el, process);
    }

    public Class getDefClass() {
        return ProcessDef.class;
    }

    public void fromXml(Element el, ProcessDef def) {
        subPageHandler.fromXml(def, el);
        ProcessImpl process = def.getProcess();
        process.loadXml(el);
    }

    private void populateXml(Element el, ProcessImpl process) {
        el.setAttribute("startState", process.getStartState().getName());
        for (State s : process.getStates()) {
            if (s instanceof ClydeState) {
                appendToElement((ClydeState) s, el);
            } else {
                throw new RuntimeException("Unhandled implementation: " + s.getClass());
            }
        }
    }

    public void loadXml(Element el, ProcessImpl process) {
        process.setStartState(null);
        Map<Element, State> statesAndElements = new HashMap<Element, State>();
        for (Object o : el.getChildren()) {
            Element elChild = (Element) o;
            State s = null;
            String n = elChild.getName();
            String stateName = elChild.getAttributeValue("name");
            if (n.equals("state")) {
                s = process.createState(stateName);
                String sInterval = elChild.getAttributeValue("interval");
                if( sInterval != null && sInterval.length() > 0 ) {
                    s.setInterval(State.TimeDependentInterval.valueOf(sInterval));
                }
            }
            if (s != null) {
                if (process.getStartState() == null) {
                    process.setStartState(s);
                }
                statesAndElements.put(elChild, s);
                process.add(s);
            }
        }
        for (Element elState : statesAndElements.keySet()) {
            State s = statesAndElements.get(elState);
            s.loadFromXml(elState);
        }
        String startStateName = el.getAttributeValue("startState");
        if (startStateName == null || startStateName.length() == 0) {
            throw new IllegalArgumentException("no start state name");
        }
        process.setStartState(process.getState(startStateName));
        if (process.getStartState() == null) {
            throw new IllegalArgumentException("No start state found: " + startStateName);
        }

        process.walkStates();
    }

    private void appendToElement(ClydeState s, Element el) {
        Element elThis = new Element("state", CodeMeta.NS);
        el.addContent(elThis);
        populateXml(s, elThis);
    }

    public void populateXml(ClydeState s, Element e2) {
        subPageHandler.populateXml(e2, s);

        //s.getState().populateXml(e2);
        StateImpl stateImpl = s.getState();
        e2.setAttribute("interval", toString(s.getInterval()));
        populateTransitionsXml(stateImpl, e2);
        stateImpl.appendHandlers(stateImpl.getOnEnterHandlers(), "onEnter", e2);
        stateImpl.appendHandlers(stateImpl.getOnExitHandlers(), "onExit", e2);
    }

    protected void populateTransitionsXml(StateImpl stateImpl, Element elThis) {
        for (Transition t : stateImpl.getTransitions()) {
            ClydeTransition ctrans = (ClydeTransition) t;
            Element elTrans = new Element("transition", CodeMeta.NS);
            elThis.addContent(elTrans);
            subPageHandler.populateXml(elTrans, ctrans);
            ctrans.getTransition().populateXml(elTrans);
        }
    }

    private String toString(State.TimeDependentInterval t) {
        if( t == null ) {
            return "";
        } else {
            return t.name();
        }
    }
}
