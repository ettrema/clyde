package com.ettrema.web.code.meta;

import com.ettrema.utils.JDomUtils;
import com.ettrema.web.BaseResource;
import com.ettrema.web.code.meta.comp.ComponentHandler;
import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Component;
import com.ettrema.web.code.CodeMeta;
import com.ettrema.web.code.meta.comp.*;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.InitUtils;
import com.ettrema.web.component.NameInput;
import com.ettrema.web.component.TemplateSelect;
import com.ettrema.web.component.ThumbsComponent;
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
    private UnknownComponentHandler unknownComponentHandler = new UnknownComponentHandler();

    public CommonTemplatedMetaHandler() {
        mapOfComponentHandlersByClass = new LinkedHashMap<>();
        mapOfComponentHandlersByAlias = new LinkedHashMap<>();
        AbstractInputHandler abstractInputHandler = new AbstractInputHandler();
        CommandHandler commandHandler = new CommandHandler();
        GroupEmailCommandHandler groupEmailCommandHandler = new GroupEmailCommandHandler(commandHandler);
        TextHandler textHandler = new TextHandler(abstractInputHandler);
        TemplateInputHandler templateInputHandler = new TemplateInputHandler(textHandler);
        VelocityTemplateTextComponentHandler velocityTemplateTextComponentHandler = new VelocityTemplateTextComponentHandler(textHandler);
        HtmlInputHandler htmlInputHandler = new HtmlInputHandler(textHandler);
        NumberInputHandler numberInputHandler = new NumberInputHandler(abstractInputHandler);
        GroupSelectHandler groupSelectHandler = new GroupSelectHandler();
        MultiGroupSelectHandler multiGroupSelectHandler = new MultiGroupSelectHandler();
        ForgottenPasswordComponentHandler forgottenPasswordComponentHandler = new ForgottenPasswordComponentHandler();
        EmailInputHandler emailInputHandler = new EmailInputHandler(textHandler);
        EmailCommandHandler emailCommandHandler = new EmailCommandHandler(commandHandler);
        CreateCommandHandler createCommandHandler = new CreateCommandHandler(commandHandler);
        ExpressionComponentHandler expressionComponentHandler = new ExpressionComponentHandler();
        SumComponentHandler sumComponentHandler = new SumComponentHandler(expressionComponentHandler);
        GenerateThumbsCommandHandler generateThumbsCommandHandler = new GenerateThumbsCommandHandler(commandHandler);
        subPageHandler = new SubPageHandler(this);
        CsvSubPageHandler csvSubPageHandler = new CsvSubPageHandler(subPageHandler);
        ViewSubPageHandler viewSubPageHandler = new ViewSubPageHandler(subPageHandler);
        ReCaptchaComponentHandler reCaptchaComponentHandler = new ReCaptchaComponentHandler();
        PayPalIpnComponentHandler payPalIpnComponentHandler = new PayPalIpnComponentHandler();
        GroovyCommandHandler groovyCommandHandler = new GroovyCommandHandler();
        EvaluatableComponentHandler evaluatableComponentHandler = new EvaluatableComponentHandler();

        add(evaluatableComponentHandler);
        add(expressionComponentHandler);
        add(sumComponentHandler);
        add(generateThumbsCommandHandler);
        add(velocityTemplateTextComponentHandler);
        add(groovyCommandHandler);
        add(payPalIpnComponentHandler);
        add(reCaptchaComponentHandler);
        add(emailCommandHandler);
        add(createCommandHandler);
        add(emailInputHandler);
        add(forgottenPasswordComponentHandler);
        add(groupSelectHandler);
        add(multiGroupSelectHandler);
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

        JDomUtils.setChildText(el, "onPostScript", res.getOnPostScript(), CodeMeta.NS);

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
                    ch = unknownComponentHandler;
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

    void applyOverrideFromXml(CommonTemplated res, Element el) {
        updateValues(res, el, false, true);
        updateComponents(res, el, true);
    }

    public void updateFromXml(CommonTemplated res, Element el, boolean includeContentVals) {
        log.trace("updateFromXml2");
        String templateName = InitUtils.getValue(el, "template");
        log.trace("templateName: " + templateName);
        res.setTemplateName(templateName);
        res.setContentType(InitUtils.getValue(el, "contentType"));
        res.setMaxAgeSecsThis(InitUtils.getInteger(el, "maxAge"));

        updateValues(res, el, includeContentVals, false);
        updateComponents(res, el, false);
        // TODO: handle values for non body+title

        String onPostScript = JDomUtils.valueOf(el, "onPostScript", CodeMeta.NS);
        res.setOnPostScript(onPostScript);


    }

    private void updateValues(CommonTemplated res, Element el, boolean includeContentVals, boolean override) {
        // Remove all cv's except title and body
        log.trace("updateValues: current values: " + res.getValues().size());
        if (!override) {
            Iterator<Entry<String, ComponentValue>> it = res.getValues().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, ComponentValue> entry = it.next();
                if (!isIgnoredVal(entry.getKey(), includeContentVals)) {
                    log.trace("remove: " + entry.getKey());
                    it.remove();
                }
            }
        }

        for (Element eAtt : JDomUtils.childrenOf(el, "attributes", CodeMeta.NS)) {
            String cvName = eAtt.getName();
            ValueHandler h = mapOfValueAliases.get(cvName);
            if (h == null) {
                throw new IllegalArgumentException("No handler for: " + eAtt.getName());
            }
            ComponentValue cv = h.fromXml(res, eAtt);
            res.getValues().add(cv);
        }

    }

    private void updateComponents(CommonTemplated res, Element el, boolean override) {
        if (!override) {
            Iterator<Entry<String, Component>> it = res.getComponents().entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Component> entry = it.next();
                if (!isIgnoredComponent(entry.getKey())) {
                    it.remove();
                }
            }
        }

        for (Element eAtt : JDomUtils.childrenOf(el, "components", CodeMeta.NS)) {
            String cName = eAtt.getName();
            ComponentHandler h = mapOfComponentHandlersByAlias.get(cName);
            if (h == null) {
                h = unknownComponentHandler;
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
