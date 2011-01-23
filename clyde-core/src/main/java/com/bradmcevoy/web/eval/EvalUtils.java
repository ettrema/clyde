package com.bradmcevoy.web.eval;

import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.InitUtils;
import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *
 * @author brad
 */
public class EvalUtils {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EvalUtils.class);
    private static final Map<String, EvaluatableToXml> parsersByName = new HashMap<String, EvaluatableToXml>();
    private static final Map<Class, EvaluatableToXml> parsersByClass = new HashMap<Class, EvaluatableToXml>();

    static {
        add(new ConstEvaluatableToXml());
        add(new ComponentReferenceToXml());
        add(new MvelEvaluatableToXml());
        add(new VelocityEvaluatableToXml());
    }

    private static void add(EvaluatableToXml parser) {
        parsersByName.put(parser.getLocalName(), parser);
        parsersByClass.put(parser.getEvalClass(), parser);
    }

    /**
     * null safe
     *
     * @param eval
     * @param rc
     * @param container
     * @return
     */
    public static Object eval(Evaluatable eval, RenderContext rc, Addressable container) {
        if (eval == null) {
            return null;
        } else {
            return eval.evaluate(rc, container);
        }
    }

    /**
     * Null safe. Uses formatter for non-string results
     *
     * @param el
     * @param rc
     * @param container
     * @return
     */
    public static String evalToString(Evaluatable el, RenderContext rc, Addressable container) {
        Object o = eval(el, rc, container);
        if (o == null) {
            return null;
        } else {
            return Formatter.getInstance().format(o);
        }
    }

    /**
     * Will never return null
     *
     * @param elParent
     * @param name
     * @param ns
     * @return
     */
    public static Evaluatable getEval(Element elParent, String name, Namespace ns) {
        Element el = InitUtils.getChildElement(elParent, name, ns);
        EvaluatableToXml parser;
        if (el == null) {
            return new ConstEvaluatable();
        } else {
            Element elChild = null;
            for (Object o : el.getChildren()) {
                if (o instanceof Element) {
                    elChild = (Element) o;
                    break;
                }
            }
            if (elChild == null) {
                return new ConstEvaluatable();
            } else {
                String n = elChild.getName();
                parser = parsersByName.get(n);
                if (parser == null) {
                    throw new RuntimeException("Unsupported evaulatable type: " + n);
                } else {
                    Evaluatable eval = parser.fromXml(elChild);
                    return eval;
                }
            }
        }
    }

    public static void setEval(Element elParent, String name, Evaluatable eval, Namespace ns) {
        if (eval == null) {
            return;
        }
        EvaluatableToXml parser = parsersByClass.get(eval.getClass());
        if (parser == null) {
            log.warn("listing supported classes");
            for (Class c : parsersByClass.keySet()) {
                log.warn(" supported class: " + c);
            }
            throw new RuntimeException("Unsupported evaulablte type: " + eval.getClass());
        } else {
            Element el = new Element(name, ns);
            elParent.addContent(el);
            Element elChild = new Element(parser.getLocalName(), ns);
            el.addContent(elChild);
            parser.populateXml(elChild, eval);
        }
    }

    private static interface EvaluatableToXml<T extends Evaluatable> {

        String getLocalName();

        void populateXml(Element elEval, T target);

        T fromXml(Element elEval);

        Class<T> getEvalClass();
    }

    private static class ConstEvaluatableToXml implements EvaluatableToXml<ConstEvaluatable> {

        public String getLocalName() {
            return "const";
        }

        public void populateXml(Element elEval, ConstEvaluatable target) {
            String s = null;
            if (target.getValue() != null) {
                s = target.getValue().toString();
            }
            elEval.setText(s);
        }

        public ConstEvaluatable fromXml(Element elEval) {
            String s = elEval.getText();
            return new ConstEvaluatable(s);
        }

        public Class getEvalClass() {
            return ConstEvaluatable.class;
        }
    }

    private static class ComponentReferenceToXml implements EvaluatableToXml<ComponentReference> {

        public String getLocalName() {
            return "ref";
        }

        public void populateXml(Element elEval, ComponentReference target) {
            if (target.getPath() != null) {
                elEval.setText(target.getPath().toString());
            }
        }

        public ComponentReference fromXml(Element elEval) {
            String s = elEval.getText();
            return new ComponentReference(s);
        }

        public Class getEvalClass() {
            return ComponentReference.class;
        }
    }

    private static class MvelEvaluatableToXml implements EvaluatableToXml<MvelEvaluatable> {

        public String getLocalName() {
            return "mvel";
        }

        public void populateXml(Element elEval, MvelEvaluatable target) {
            String s = null;
            if (target.getExpr() != null) {
                s = target.getExpr();
            }
            elEval.setText(s);
        }

        public MvelEvaluatable fromXml(Element elEval) {
            String s = elEval.getText();
            return new MvelEvaluatable(s);
        }

        public Class getEvalClass() {
            return MvelEvaluatable.class;
        }
    }

    private static class VelocityEvaluatableToXml implements EvaluatableToXml<VelocityEvaluatable> {

        public String getLocalName() {
            return "velocity";
        }

        public void populateXml(Element elEval, VelocityEvaluatable target) {
            String s = null;
            if (target.getTemplate() != null) {
                s = target.getTemplate();
            }
            JDomUtils.setInnerXml(elEval, s);
        }

        public VelocityEvaluatable fromXml(Element elEval) {
            String s = JDomUtils.getInnerXml(elEval);
            return new VelocityEvaluatable(s);
        }

        public Class getEvalClass() {
            return VelocityEvaluatable.class;
        }
    }
}
