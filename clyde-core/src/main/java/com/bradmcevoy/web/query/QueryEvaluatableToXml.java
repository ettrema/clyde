package com.bradmcevoy.web.query;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.utils.JDomUtils;
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

    public Query fromXml(Element elEval, Namespace ns) {
        Query query = new Query();
        updateSelect(query, elEval, ns);
        updateGroupBy(query, elEval, ns);
        updateFrom(query, elEval, ns);
        updateWhere(query, elEval, ns);
        updateOrderBy(query, elEval, ns);
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

    private void updateSelect(Query query, Element elEval, Namespace ns) {
        List<Field> list = new ArrayList<Field>();
        query.setSelectFields(list);
        for( Element elField : JDomUtils.childrenOf(elEval, "select", ns)) {
            Field f = new Field();
            list.add(f);
            f.setName(elField.getAttributeValue("name"));
            f.setEvaluatable( EvalUtils.getEvalDirect(elField, ns) );
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

    private void updateGroupBy(Query query, Element elEval, Namespace ns) {
        Map<String,Field> map = new HashMap<String,Field>();
        query.setGroupFields(map);
        for( Element elField : JDomUtils.childrenOf(elEval, "groupby", ns)) {
            Field f = new Field();
            f.setName(elField.getAttributeValue("name"));
            f.setEvaluatable( EvalUtils.getEvalDirect(elField, ns) );
            map.put(f.getName(), f);
        }
    }

    private void populateFrom(Element elEval, Selectable from, Namespace ns) {
        Element elFrom = new Element("from", ns);
        elEval.addContent(elFrom);
        if( from instanceof PathSelectable) {
            PathSelectable ps = (PathSelectable) from;
            elFrom.setAttribute("path", ps.getPath().toString());
        } else if( from instanceof Query) {
            Query subQuery = (Query) from;
            populateXml(elFrom, subQuery, ns);
        }
    }

    private void updateFrom(Query query, Element elEval, Namespace ns) {
        Element elFrom = elEval.getChild("from", ns);
        Selectable selectable;
        if( elFrom == null ) {
            PathSelectable ps = new PathSelectable();
            selectable = ps;
        } else if( elFrom.getAttribute("path") != null ) {
            PathSelectable ps = new PathSelectable();
            ps.setPath(Path.path(elFrom.getAttributeValue("path")));
            selectable = ps;
        } else {
            Query subQuery = (Query) EvalUtils.getEvalDirect(elFrom, ns);
            selectable = subQuery;
        }
        query.setFrom(selectable);
    }


    private void populateWhere(Element elEval, Evaluatable where, Namespace ns) {
        EvalUtils.setEval(elEval, "where", where, ns);
    }

    private void updateWhere(Query query, Element elEval, Namespace ns) {
        Evaluatable where = EvalUtils.getEval(elEval,"where", ns);
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
            elField.setAttribute("direction", f.getDirection().toString() );
            elGroupBy.addContent(elField);
            EvalUtils.setEvalDirect(elField, f.getEvaluatable(), ns);
        }
    }

    private void updateOrderBy(Query query, Element elEval, Namespace ns) {
        System.out.println("updateOrderBy");
        List<OrderByField> list = new ArrayList<OrderByField>();
        query.setOrderByFields(list);
        for( Element elField : JDomUtils.childrenOf(elEval, "orderby", ns)) {
            OrderByField f = new OrderByField();
            list.add(f);
            String nm = elField.getAttributeValue("name");
            
            f.setName(nm);
            String sDir = elField.getAttributeValue("direction");
            Direction dir = Direction.ascending; 
            if( sDir != null ) {
                dir = Direction.valueOf(sDir);
            }
            f.setDirection(dir);
            f.setEvaluatable( EvalUtils.getEvalDirect(elField, ns, false) );
            System.out.println("updateOrderBy: " + f.getName());
        }
    }
}
