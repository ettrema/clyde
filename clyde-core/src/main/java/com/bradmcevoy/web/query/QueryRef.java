package com.bradmcevoy.web.query;

import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.EvaluatableComponent;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author brad
 */
public class QueryRef implements Selectable, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(QueryRef.class);
    private static final long serialVersionUID = 1L;
    private String componentName;
    private Templatable container;

    public QueryRef(String componentName, Templatable container) {
        this.componentName = componentName;
        this.container = container;
    }

    public List<FieldSource> getRows(Folder from) {
        log.trace("getRows");
        Component c = container.getComponents().get(componentName);
        if (c == null) {
            throw new RuntimeException("Cant find component: " + componentName + " for queryRef");
        } else if (c instanceof EvaluatableComponent) {
            EvaluatableComponent ec = (EvaluatableComponent) c;
            if (ec.getEvaluatable() == null) {
                throw new RuntimeException("QueryReference is to a component which does not have a Evaluatable set");
            } else if (ec.getEvaluatable() instanceof Query) {
                Query q = (Query) ec.getEvaluatable();
                return q.getRows(from);
            } else {
                throw new RuntimeException("QueryReference is to a component whose evaluatable is not of type Query. Is a: " + ec.getEvaluatable().getClass());
            }
        } else {
            throw new RuntimeException("QueryRef resolved to a component which is not a Query");
        }
    }

    public List<String> getFieldNames() {
        log.trace("getRows");
        Component c = container.getComponents().get(componentName);
        if (c == null) {
            throw new RuntimeException("Cant find component: " + componentName + " for queryRef");
        } else if (c instanceof EvaluatableComponent) {
            EvaluatableComponent ec = (EvaluatableComponent) c;
            if (ec.getEvaluatable() == null) {
                throw new RuntimeException("QueryReference is to a component which does not have a Evaluatable set");
            } else if (ec.getEvaluatable() instanceof Query) {
                Query q = (Query) ec.getEvaluatable();
                return q.getFieldNames();
            } else {
                throw new RuntimeException("QueryReference is to a component whose evaluatable is not of type Query. Is a: " + ec.getEvaluatable().getClass());
            }
        } else {
            throw new RuntimeException("QueryRef resolved to a component which is not a Query");
        }
    }
}
