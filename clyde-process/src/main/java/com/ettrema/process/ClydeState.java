package com.ettrema.process;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.process.ActionHandler;
import com.bradmcevoy.process.State;
import com.bradmcevoy.process.Process;
import com.bradmcevoy.process.State.TimeDependentInterval;
import com.bradmcevoy.process.StateImpl;
import com.bradmcevoy.process.Transitions;
import com.ettrema.web.Component;
import com.ettrema.web.SubPage;
import com.ettrema.web.WrappedSubPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

public class ClydeState extends SubPage implements State {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeState.class);
    private static final long serialVersionUID = 1L;
    private StateImpl state;

    public ClydeState(ProcessDef def, String name) {
        super(def, name);
        this.state = new MyStateImpl(def, name);
    }

    public StateImpl getState() {
        return state;
    }
    

    public TimeDependentInterval getInterval() {
        return state.getInterval();
    }

    @Override
    public List<ActionHandler> getOnEnterHandlers() {
        List<ActionHandler> handlers = new ArrayList<ActionHandler>();
        handlers.addAll(state.getOnEnterHandlers());
        for( Component c : this.allComponents() ) {
            if( c instanceof ActionHandler ) {
                if( c.getName().contains("onEnter") ) handlers.add((ActionHandler) c);
            }
        }
        return handlers;
    }

    @Override
    public List<ActionHandler> getOnExitHandlers() {
        List<ActionHandler> handlers = new ArrayList<ActionHandler>();
        handlers.addAll(state.getOnExitHandlers());
        for( Component c : this.allComponents() ) {
            if( c instanceof ActionHandler ) {
                if( c.getName().contains("onExit") ) handlers.add((ActionHandler) c);
            }
        }
        return handlers;
    }

    @Override
    public Process getProcess() {
        return state.getProcess();
    }

    @Override
    public Map<String, Process> getSubProcesses() {
        return state.getSubProcesses();
    }

    @Override
    public Transitions getTransitions() {
        return state.getTransitions();
    }

    @Override
    public Element toXml(Element el) {
        Element elThis = new Element("state");
        el.addContent(elThis);
        populateXml(elThis);
        return elThis;
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        state.populateXml(e2);
    }

    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        state.loadFromXml(el);
    }

    @Override
    public Resource getChildResource(String childName) {
        log.debug( "getChildResource: " + childName);
        ClydeTransition t = (ClydeTransition) this.getTransitions().get(childName);
        if( t != null ) {
            log.debug( "..found transition");
            Resource r = new WrappedSubPage(t, this);
            return r;
        } else {
            return super.getChildResource(childName);
        }
    }

    public void setInterval(TimeDependentInterval interval) {
        state.setInterval(interval);
    }

    public class MyStateImpl extends StateImpl {

        private static final long serialVersionUID = 1L;

        public MyStateImpl(Process process, String name) {
            super(process, name);
        }

        @Override
        public Transitions getTransitions() {
            return super.getTransitions();
        }

        @Override
        public void loadFromXml(Element el) {
            this.getTransitions().clear();            
            super.loadFromXml(el);
        }


    }
}
