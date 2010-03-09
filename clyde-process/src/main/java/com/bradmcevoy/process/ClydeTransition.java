
package com.bradmcevoy.process;

import com.bradmcevoy.web.SubPage;
import org.jdom.Element;

public class ClydeTransition extends SubPage implements Transition {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeTransition.class);
    private static final long serialVersionUID = 1L; 
    
    private Transition transition;        

    public ClydeTransition(ClydeState fromState, String name) {
        super(fromState,name); 
        this.transition = new TransitionImpl(fromState.getProcess(), name);
        this.setFromState(fromState);
    }
    
    public ClydeTransition(ClydeState fromState, String name, State toState, Rule rule) {
        super(fromState,name); 
        this.transition = new TransitionImpl(fromState.getProcess(), name, fromState, toState, rule);
    }

    
    @Override
    public State getFromState() {
        return transition.getFromState();
    }

    @Override
    public Rule getRule() {
        return transition.getRule();
    }

    @Override
    public State getToState() {
        return transition.getToState();
    }

    @Override
    public void setRule(Rule rule) {
        transition.setRule(rule);
    }

    @Override
    public Element toXml(Element el) {
        Element elThis = new Element("transition");
        el.addContent(elThis);
        populateXml(elThis);
        return elThis;        
    }

    @Override
    public void populateXml(Element e2) {
        super.populateXml(e2);
        transition.populateXml(e2);
    }    
    
    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
        transition.loadFromXml(el);
    }

    @Override
    public void setFromState(State fromState) {
        transition.setFromState(fromState);
    }

}
