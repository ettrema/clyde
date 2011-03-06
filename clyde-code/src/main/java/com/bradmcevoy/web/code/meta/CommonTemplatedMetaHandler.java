package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.code.meta.comp.ComponentHandler;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.meta.comp.AbstractInputHandler;
import com.bradmcevoy.web.code.meta.comp.CommandHandler;
import com.bradmcevoy.web.code.meta.comp.CsvSubPageHandler;
import com.bradmcevoy.web.code.meta.comp.DateValueHandler;
import com.bradmcevoy.web.code.meta.comp.DefaultValueHandler;
import com.bradmcevoy.web.code.meta.comp.EmailCommandHandler;
import com.bradmcevoy.web.code.meta.comp.EmailInputHandler;
import com.bradmcevoy.web.code.meta.comp.EmailValHandler;
import com.bradmcevoy.web.code.meta.comp.EvaluatableComponentHandler;
import com.bradmcevoy.web.code.meta.comp.ForgottenPasswordComponentHandler;
import com.bradmcevoy.web.code.meta.comp.GroovyCommandHandler;
import com.bradmcevoy.web.code.meta.comp.GroupEmailCommandHandler;
import com.bradmcevoy.web.code.meta.comp.GroupSelectHandler;
import com.bradmcevoy.web.code.meta.comp.HtmlInputHandler;
import com.bradmcevoy.web.code.meta.comp.NumberInputHandler;
import com.bradmcevoy.web.code.meta.comp.PayPalIpnComponentHandler;
import com.bradmcevoy.web.code.meta.comp.ReCaptchaComponentHandler;
import com.bradmcevoy.web.code.meta.comp.SubPageHandler;
import com.bradmcevoy.web.code.meta.comp.TemplateInputHandler;
import com.bradmcevoy.web.code.meta.comp.TextHandler;
import com.bradmcevoy.web.code.meta.comp.ValueHandler;
import com.bradmcevoy.web.code.meta.comp.ViewSubPageHandler;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.NameInput;
import com.bradmcevoy.web.component.TemplateSelect;
import com.bradmcevoy.web.component.ThumbsComponent;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CommonTemplatedMetaHandler {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommonTemplatedMetaHandler.class);
    private Map<Class, ComponentHandler> mapOfComponentHandlersByClass;
    private Map<String, ComponentHandler> mapOfComponentHandlersByAlias;
    private Map<Class, ValueHandler> mapOfValueHandlers;
    private Map<String, ValueHandler> mapOfValueAliases;
    private SubPageHandler subPageHandler;

    public CommonTemplatedMetaHandler() {
        mapOfComponentHandlersByClass = new LinkedHashMap<Class, ComponentHandler>();
        mapOfComponentHandlersByAlias = new LinkedHashMap<String, ComponentHandler>();
        AbstractInputHandler abstractInputHandler = new AbstractInputHandler();
        CommandHandler commandHandler = new CommandHandler();
        GroupEmailCommandHandler groupEmailCommandHandler = new GroupEmailCommandHandler(commandHandler);
        TextHandler textHandler = new TextHandler(abstractInputHandler);
        TemplateInputHandler templateInputHandler = new TemplateInputHandler(textHandler);
        HtmlInputHandler htmlInputHandler = new HtmlInputHandler(textHandler);
        NumberInputHandler numberInputHandler = new NumberInputHandler(abstractInputHandler);
        GroupSelectHandler groupSelectHandler = new GroupSelectHandler();
        ForgottenPasswordComponentHandler forgottenPasswordComponentHandler = new ForgottenPasswordComponentHandler();
        EmailInputHandler emailInputHandler = new EmailInputHandler(textHandler);
        EmailCommandHandler emailCommandHandler = new EmailCommandHandler(commandHandler);
        subPageHandler = new SubPageHandler(this);
        CsvSubPageHandler csvSubPageHandler = new CsvSubPageHandler(subPageHandler);
        ViewSubPageHandler viewSubPageHandler = new ViewSubPageHandler(subPageHandler);
        ReCaptchaComponentHandler reCaptchaComponentHandler = new ReCaptchaComponentHandler();
        PayPalIpnComponentHandler payPalIpnComponentHandler = new PayPalIpnComponentHandler();
        GroovyCommandHandler groovyCommandHandler = new GroovyCommandHandler();
        EvaluatableComponentHandler evaluatableComponentHandler = new EvaluatableComponentHandler();

        add(evaluatableComponentHandler);
        add(groovyCommandHandler);
        add(payPalIpnComponentHandler);
        add(reCaptchaComponentHandler);
        add(emailCommandHandler);
        add(emailInputHandler);
        add(forgottenPasswordComponentHandler);
        add(groupSelectHandler);
        add(numberInputHandler);
        add(templateInputHandler);
        add(htmlInputHandler);
        add(groupEmailCommandHandler);
        add(textHandler);
        add(viewSubPageHandler);
        add(csvSubPageHandler);
        add(subPageHandler);

        mapOfValueHandlers = new LinkedHashMap<Class, ValueHandler>();
        mapOfValueAliases = new LinkedHashMap<String, ValueHandler>();
        DefaultValueHandler defaultValueHandler = new DefaultValueHandler();
        add(new DateValueHandler(defaultValueHandler));
        add(new EmailValHandler(defaultValueHandler));
        add(defaultValueHandler);
    }

    public SubPageHandler getSubPageHandler() {
        return subPageHandler;
    }

    private void add(ComponentHandler h) {
        mapOfComponentHandlersByClass.put(h.getComponentClass(), h);
        mapOfComponentHandlersByAlias.put(h.getAlias(), h);

    }

    private void add(ValueHandler h) {
        mapOfValueHandlers.put(h.getComponentValueClass(), h);
        mapOfValueAliases.put(h.getAlias(), h);
    }

    public void populateXml(Element el, CommonTemplated res, boolean includeContentVals) {
        populateValues(res, el, includeContentVals);
        populateComponents(res, el);
        InitUtils.set(el, "template", res.getTemplateName());
        InitUtils.set(el, "contentType", res.getContentType());
        InitUtils.set(el, "maxAge", res.getMaxAgeSecsThis());

    }

    private void populateComponents(CommonTemplated res, Element el) {
        log.trace("populateComponents");
        Element e2 = null;

        for (Component c : res.getComponents().values()) {
            if (isIgnoredComponent(c)) {
                log.trace("ignore component: " + c.getName());
            } else {
                if (e2 == null) {
                    e2 = new Element("components", CodeMeta.NS);
                }
                ComponentHandler ch = mapOfComponentHandlersByClass.get(c.getClass());
                if (ch == null) {
                    throw new RuntimeException("No component handler for: " + c.getClass());
                }
                log.trace("add component: " + c.getName());
                Element eComp = ch.toXml(c);
                e2.addContent(eComp);
            }
        }
        if (e2 != null) {
            el.addContent(e2);
        }
    }

    private boolean isIgnoredComponent(Component c) {
        return (c instanceof TemplateSelect)
                || (c instanceof NameInput)
                || (c instanceof ThumbsComponent)
                || c.getName().equals("maxAge")
                || c.getName().equals("class");
    }

    private boolean isIgnoredComponent(String name) {
        return name.equals("template")
                || name.equals("name")
                || name.equals("maxAge")
                || name.equals("thumbSpecs")
                || name.equals("class");
    }

    private void populateValues(CommonTemplated res, Element el, boolean includeContentVals) {
        Element e2 = null;
        for (ComponentValue cv : res.getValues().values()) {
            if (isIgnoredVal(cv, includeContentVals)) {
                // ignore
            } else {
                if (e2 == null) {
                    e2 = new Element("attributes", CodeMeta.NS);
                }
                ValueHandler h = mapOfValueHandlers.get(cv.getClass());
                if (h == null) {
                    throw new RuntimeException("No handler for: " + cv.getClass());
                }
                Element elVal = h.toXml(cv, res);
                e2.addContent(elVal);
            }
        }
        if (e2 != null) {
            el.addContent(e2);
        }
    }

    private boolean isIgnoredVal(ComponentValue val, boolean includeContentVals) {
        return isIgnoredVal(val.getName(), includeContentVals);
    }

    private boolean isIgnoredVal(String name, boolean includeContentVals) {
        if (includeContentVals) {
            return false;
        } else {
            // these fields are output in the content for html pages
            if (name == null) {
                return false;
            }
            return name.equals("body") || name.equals("title");
        }
    }

    public void updateFromXml(CommonTemplated res, Element el) {
        updateFromXml(res, el, false);
    }

    public void updateFromXml(CommonTemplated res, Element el, boolean includeContentVals) {
        log.trace("updateFromXml2");
        String templateName = InitUtils.getValue(el, "template");
        log.trace("templateName: " + templateName);
        res.setTemplateName(templateName);
        res.setContentType(InitUtils.getValue(el, "contentType"));
        res.setMaxAgeSecsThis(InitUtils.getInteger(el, "maxAge"));

        updateValues(res, el, includeContentVals);
        updateComponents(res, el);
        // TODO: handle values for non body+title
    }

    private void updateValues(CommonTemplated res, Element el, boolean includeContentVals) {
        // Remove all cv's except title and body
        log.trace("updateValues: current values: " + res.getValues().size());
        Iterator<Entry<String, ComponentValue>> it = res.getValues().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, ComponentValue> entry = it.next();
            if (!isIgnoredVal(entry.getKey(), includeContentVals)) {
                log.trace("remove: " + entry.getKey());
                it.remove();
            }
        }

        for (Element eAtt : JDomUtils.childrenOf(el, "attributes", CodeMeta.NS)) {
            ValueHandler h = mapOfValueAliases.get(eAtt.getName());
            if (h == null) {
                throw new IllegalArgumentException("No handler for: " + eAtt.getName());
            }
            ComponentValue cv = h.fromXml(res, eAtt);
            res.getValues().add(cv);
        }

    }

    private void updateComponents(CommonTemplated res, Element el) {
        Iterator<Entry<String, Component>> it = res.getComponents().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Component> entry = it.next();
            if (!isIgnoredComponent(entry.getKey())) {
                it.remove();
            }
        }

        for (Element eAtt : JDomUtils.childrenOf(el, "components", CodeMeta.NS)) {
            ComponentHandler h = mapOfComponentHandlersByAlias.get(eAtt.getName());
            if (h == null) {
                throw new RuntimeException("Couldnt find component handler for element of type: " + eAtt.getName());
            }
            Component c = h.fromXml(res, eAtt);
            if (c != null) {
                log.trace("add component: " + c.getName());
                res.getComponents().add(c);
            } else {
                log.warn("got null component from: " + h.getClass());
            }
        }
    }
}
