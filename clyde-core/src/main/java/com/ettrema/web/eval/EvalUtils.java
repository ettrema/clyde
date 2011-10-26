package com.bradmcevoy.web.eval;

import com.bradmcevoy.query.persistence.SqlExportEvaluatableToXml;
import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.query.QueryEvaluatableToXml;
import com.bradmcevoy.web.query.SqlSelectableToXml;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
		try {
			add(new ConstEvaluatableToXml());
			add(new ComponentReferenceToXml());
			add(new MvelEvaluatableToXml());
			add(new GroovyEvaluatableToXml());
			add(new VelocityEvaluatableToXml());

			add(new AndEvaluatableToXml());
			add(new OrEvaluatableToXml());
			add(new NotEvaluatableToXml());

			add(new QueryEvaluatableToXml());
			add(new SqlExportEvaluatableToXml());
			add(new SqlSelectableToXml());
		} catch(Throwable e) {
			e.printStackTrace();
			throw new RuntimeException("Exception initialising EvalUtils: " + e.getMessage(), e);
		}
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
     *
     * @param elParent
     * @param name
     * @param ns
     * @return
     */
    public static Evaluatable getEval(Element elParent, String name, Namespace ns, Addressable container) {
        Element el = InitUtils.getChildElement(elParent, name, ns);
        return getEvalDirect(el, ns, container);
    }

    /**
     * Looks for a single child, and parses it to return an Evaultate
     *
     * @param el
     * @param ns
     * @return
     */
    public static Evaluatable getEvalDirect(Element el, Namespace ns, Addressable container) {
        return getEvalDirect(el, ns, true, container);
    }
	/**
	 * 
	 * @param el
	 * @param ns
	 * @param defaultIfNull
	 * @param container
	 * @param elementName - optional
	 * @return 
	 */
    public static Evaluatable getEvalDirect(Element el, Namespace ns, boolean defaultIfNull, Addressable container) {
        EvaluatableToXml parser;
        if (el == null) {
            return null;
        } else {
            Element elChild = null;
            for (Object o : el.getChildren()) {
                if (o instanceof Element) {
                    elChild = (Element) o;
                    break;
                }
            }
            if (elChild == null) {
                if( defaultIfNull ) {
                    return new ConstEvaluatable();
                } else {
                    return null;
                }
            } else {
                String n = elChild.getName();
                parser = parsersByName.get(n);
                if (parser == null) {
                    throw new RuntimeException("Unsupported evaulatable type: " + n);
                } else {
                    Evaluatable eval = parser.fromXml(elChild, ns, container);
                    return eval;
                }
            }
        }
    }

    public static List<Evaluatable> getEvalDirectList(Element el, Namespace ns, Addressable container) {
        List<Evaluatable> list = new ArrayList<Evaluatable>();
        EvaluatableToXml parser;
        if (el != null) {
            Element elChild = null;
            for (Object o : el.getChildren()) {
                if (o instanceof Element) {
                    elChild = (Element) o;
                    String n = elChild.getName();
                    parser = parsersByName.get(n);
                    if (parser == null) {
                        throw new RuntimeException("Unsupported evaulatable type: " + n);
                    } else {
                        Evaluatable eval = parser.fromXml(elChild, ns, container);
                        list.add(eval);
                    }
                }
            }
        }
        return list;
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
            parser.populateXml(elChild, eval, ns);
        }
    }

    public static void setEvalDirect(Element elParent, Evaluatable eval, Namespace ns) {
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
            Element elChild = new Element(parser.getLocalName(), ns);
            elParent.addContent(elChild);
            parser.populateXml(elChild, eval, ns);
        }
    }

    public static void setEvalDirectList(Element elParent, List<Evaluatable> list, Namespace ns) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Evaluatable eval  : list) {
            EvaluatableToXml parser = parsersByClass.get(eval.getClass());
            if (parser == null) {
                log.warn("listing supported classes");
                for (Class c : parsersByClass.keySet()) {
                    log.warn(" supported class: " + c);
                }
                throw new RuntimeException("Unsupported evaulablte type: " + eval.getClass());
            } else {
                Element elChild = new Element(parser.getLocalName(), ns);
                elParent.addContent(elChild);
                parser.populateXml(elChild, eval, ns);
            }
        }
    }
}
