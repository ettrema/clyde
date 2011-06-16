package com.bradmcevoy.web.query;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.eval.EvalUtils;
import com.bradmcevoy.web.eval.Evaluatable;
import com.bradmcevoy.web.eval.EvaluatableToXml;
import com.bradmcevoy.web.query.OrderByField.Direction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 *           <query>
 *              <select>
 *                  <field name="bannerGroup"/>
 *                  <field name="pharmacy"/>
 *                  <field name="numMembers">
 *                      <mvel>count</mvel>
 *                  </field>
 *              </select>
 *               <groupby>
 *                      <field name="bannerGroup">
 *                          <mvel>parent.bannerGroup</mvel>
 *                      </field>
 *                      <field name="pharmacy">
 *                          <mvel>parent.title</mvel>
 *                      </field>
 *               </groupby>
 *               <from path="/pharmacies/**" />
 *               <where>
 *                      <or>
 *                          <mvel>templateName=='pa'</mvel>
 *                          <mvel>templateName=='pharmacist'</mvel>
 *                      </or>
 *               </where>
 *              <orderby>
 *                  <orderfield name="numMembers" order="ascending">
 *                  <orderfield name="bannerGroup"/>
 *                  <orderfield name="pharmacy"/>
 *              </orderby>
 *           </query>
 *
 * @author brad
 */
public class QueryEvaluatableToXml implements EvaluatableToXml<Query> {

    public String getLocalName() {
        return "query";
    }

    public void populateXml(Element elEval, Query target, Namespace ns) {
        populateSelect(elEval, target.getSelectFields(), ns);
        populateGroupBy(elEval, target.getGroupFields(), ns);
        populateFrom(elEval, target.getFrom(), ns);
        populateWhere(elEval, target.getWhere(), ns);
        populateOrderBy(elEval, target.getOrderByFields(), ns);
    }

    public Query fromXml(Element elEval, Namespace ns, Addressable container) {
        Query query = new Query();
        updateSelect(query, elEval, ns, container);
        updateGroupBy(query, elEval, ns, container);
        if (container == null) {
            throw new RuntimeException("Container is null");
        } else if (container instanceof Templatable) {
            updateFrom(query, elEval, ns, (Templatable) container);
        } else {
            throw new RuntimeException("Container: " + container.getName() + " is not a Templatable. Is a: " + container.getClass());
        }
        updateWhere(query, elEval, ns, container);
        updateOrderBy(query, elEval, ns, container);
        return query;
    }

    public Class<Query> getEvalClass() {
        return Query.class;
    }

    private void populateSelect(Element elEval, List<Field> selectFields, Namespace ns) {
        Element elSelect = new Element("select", ns);
        elEval.addContent(elSelect);
        for (Field f : selectFields) {
            Element elField = new Element("field", ns);
            elField.setAttribute("name", f.getName());
            elSelect.addContent(elField);
            EvalUtils.setEvalDirect(elField, f.getEvaluatable(), ns);
        }
    }

    private void updateSelect(Query query, Element elEval, Namespace ns, Addressable container) {
        List<Field> list = new ArrayList<Field>();
        query.setSelectFields(list);
        for (Element elField : JDomUtils.childrenOf(elEval, "select", ns)) {
            Field f = Field.fromXml(elField,container, ns);
            list.add(f);
        }
    }

    private void populateGroupBy(Element elEval, Map<String, Field> groupFields, Namespace ns) {
        if (groupFields == null || groupFields.isEmpty()) {
            return;
        }
        Element elGroupBy = new Element("groupby", ns);
        elEval.addContent(elGroupBy);
        for (Field f : groupFields.values()) {
            Element elField = new Element("field", ns);
            elField.setAttribute("name", f.getName());
            elGroupBy.addContent(elField);
            EvalUtils.setEvalDirect(elField, f.getEvaluatable(), ns);
        }
    }

    private void updateGroupBy(Query query, Element elEval, Namespace ns, Addressable container) {
        Map<String, Field> map = new HashMap<String, Field>();
        query.setGroupFields(map);
        for (Element elField : JDomUtils.childrenOf(elEval, "groupby", ns)) {
            Field f = new Field();
            f.setName(elField.getAttributeValue("name"));
            f.setEvaluatable(EvalUtils.getEvalDirect(elField, ns, container));
            map.put(f.getName(), f);
        }
    }

    private void populateFrom(Element elEval, Selectable from, Namespace ns) {
        Element elFrom = new Element("from", ns);
        elEval.addContent(elFrom);
        if (from instanceof PathSelectable) {
            PathSelectable ps = (PathSelectable) from;
            String sPath = "";
            if( ps.getPath() != null ) {
                sPath = ps.getPath().toString();
            }
            elFrom.setAttribute("path", sPath);
        } else if (from instanceof Query) {
            Query subQuery = (Query) from;
            populateXml(elFrom, subQuery, ns);
        }
    }

    private void updateFrom(Query query, Element elEval, Namespace ns, Templatable container) {
        Element elFrom = elEval.getChild("from", ns);
        Selectable selectable;
        if (elFrom == null) {
            PathSelectable ps = new PathSelectable();
            selectable = ps;
        } else if (elFrom.getAttribute("ref") != null) {
            String componentName = elFrom.getAttributeValue("ref");
            selectable = new QueryRef(componentName, container);
        } else if (elFrom.getAttribute("path") != null) {
            PathSelectable ps = new PathSelectable();
            ps.setPath(Path.path(elFrom.getAttributeValue("path")));
            selectable = ps;
        } else {
            Query subQuery = (Query) EvalUtils.getEvalDirect(elFrom, ns, false, container);
            if (subQuery == null) {
                throw new RuntimeException("No valid from clause for query: " + query.toString());
            }
            selectable = subQuery;
        }
        query.setFrom(selectable);
    }

    private void populateWhere(Element elEval, Evaluatable where, Namespace ns) {
        EvalUtils.setEval(elEval, "where", where, ns);
    }

    private void updateWhere(Query query, Element elEval, Namespace ns, Addressable container) {
        Evaluatable where = EvalUtils.getEval(elEval, "where", ns, container);
        query.setWhere(where);
    }

    private void populateOrderBy(Element elEval, List<OrderByField> orderByFields, Namespace ns) {
        if (orderByFields == null || orderByFields.isEmpty()) {
            return;
        }
        Element elGroupBy = new Element("orderby", ns);
        elEval.addContent(elGroupBy);
        for (OrderByField f : orderByFields) {
            Element elField = new Element("orderfield", ns);
            elField.setAttribute("name", f.getName());
            elField.setAttribute("direction", f.getDirection().toString());
            elGroupBy.addContent(elField);
            EvalUtils.setEvalDirect(elField, f.getEvaluatable(), ns);
        }
    }

    private void updateOrderBy(Query query, Element elEval, Namespace ns, Addressable container) {
        List<OrderByField> list = new ArrayList<OrderByField>();
        query.setOrderByFields(list);
        for (Element elField : JDomUtils.childrenOf(elEval, "orderby", ns)) {
            OrderByField f = new OrderByField();
            list.add(f);
            String nm = elField.getAttributeValue("name");

            f.setName(nm);
            String sDir = elField.getAttributeValue("direction");
            Direction dir = Direction.ascending;
            if (sDir != null) {
                dir = Direction.valueOf(sDir);
            }
            f.setDirection(dir);
            f.setEvaluatable(EvalUtils.getEvalDirect(elField, ns, false, container));
        }
    }
}
