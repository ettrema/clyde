package com.ettrema.web.query;

import com.ettrema.web.component.Addressable;
import com.ettrema.web.eval.EvalUtils;
import com.ettrema.web.eval.Evaluatable;
import java.io.Serializable;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * A field is a name and optionally an expression of some kind.
 *
 * If evaluatable name must identify a field in the source
 *
 * @author brad
 */
public class Field implements Serializable {

    private static final long serialVersionUID = 1L;

    public static Field fromXml(Element elField, Addressable container, Namespace ns) {
        Field f = new Field();
        f.setName(elField.getAttributeValue("name"));
        f.setEvaluatable(EvalUtils.getEvalDirect(elField, ns, false, container));
        return f;

    }
    private String name;
    private Evaluatable evaluatable;

    public Field(String name, Evaluatable evaluatable) {
        this.name = name;
        this.evaluatable = evaluatable;
    }

    public Field(String name) {
        this.name = name;
    }

    public Field() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Evaluatable getEvaluatable() {
        return evaluatable;
    }

    public void setEvaluatable(Evaluatable evaluatable) {
        this.evaluatable = evaluatable;
    }
}
