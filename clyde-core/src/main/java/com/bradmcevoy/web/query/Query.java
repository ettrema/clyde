package com.bradmcevoy.web.query;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.eval.Evaluatable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A query has a list of fields to be evaluated for the rows in a source, where
 * the source might be another query or a folder path.
 *
 * There is an optional where object which will be evaluated for each row, and
 * only rows with a true value are output.
 *
 *
 * @author brad
 */
public class Query implements Selectable, Evaluatable, Serializable {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Query.class );
    private static final long serialVersionUID = 1L;

    private List<Field> selectFields;
    private Map<String,Field> groupFields;
    private Selectable from;
    private Evaluatable where;

    public List<Field> getSelectFields() {
        return selectFields;
    }

    public void setSelectFields(List<Field> selectFields) {
        this.selectFields = selectFields;
    }



    public Map<String, Field> getGroupFields() {
        return groupFields;
    }

    public void setGroupFields(Map<String, Field> groupFields) {
        this.groupFields = groupFields;
    }



    public Selectable getFrom() {
        return from;
    }

    public void setFrom(Selectable from) {
        this.from = from;
    }



    public Evaluatable getWhere() {
        return where;
    }

    public void setWhere(Evaluatable where) {
        this.where = where;
    }

    

    

    public Object evaluate(RenderContext rc, Addressable from) {
        CommonTemplated relativeTo = (CommonTemplated) from;
        if( !(relativeTo instanceof Folder) ) {
            relativeTo = relativeTo.getParentFolder();

        }
        return getRows((Folder) relativeTo);
    }

    public Object evaluate(Object from) {
        Folder relativeTo = (Folder) from;
        return getRows(relativeTo);
    }


    public List<FieldSource> getRows(Folder relativeTo) {
        log.trace("getRows: " + relativeTo.getHref());
        if (groupFields == null || groupFields.isEmpty()) {
            log.trace("non aggregating query");
            List<FieldSource> output = new ArrayList<FieldSource>();
            for (FieldSource fs : from.getRows(relativeTo)) {
                if (isWhereTrue(fs)) {
                    Row row = buildRow(fs, selectFields);
                    output.add(row);
                }
            }
            return output;
        } else {
            log.trace("aggregating query");
            Map<Row, Row> output = new HashMap<Row, Row>();
            for (FieldSource fs : from.getRows(relativeTo)) {
                if (isWhereTrue(fs)) {
//                    log.trace("true result");
                    addAggregatedResult(fs, output);
                } else {
  //                  log.trace("false result");
                }
            }
            calcAggegatedFields(output);
            List<FieldSource> list = new ArrayList<FieldSource>();
            list.addAll(output.values());
            log.trace("results size: " + list.size());
            return list;
        }
    }

    private boolean isWhereTrue(FieldSource fs) {
        if (where == null) {
            return true;
        } else {
            Object oVal = where.evaluate(fs.getData());
            return Formatter.getInstance().toBool(oVal);
        }
    }

    /**
     * For groupby queries, we accumulate sub-rows within the group by rows,
     * then we'll apply field expressions at the end
     *
     * @param fs
     * @param output
     */
    private void addAggregatedResult(FieldSource fs, Map<Row, Row> output) {
        Row groupByKey = buildRow(fs, groupFields.values());
        Row outputRow = output.get(groupByKey);
        if (outputRow == null) {
            outputRow = new Row();
            output.put(groupByKey, outputRow);
        }
        // Note that fields are calculated for each row in a second phase, after rows are accumulated
        outputRow.getSubRows().add(fs);
    }

    private Row buildRow(FieldSource fs, Collection<Field> fields) {
        Row row = new Row();
        populateRow(row, fs, fields);
        return row;
    }

    private Object evaluate(Field f, FieldSource fs) {
        if (f.getEvaluatable() == null) {
            return fs.get(f.getName());
        } else {
            return f.getEvaluatable().evaluate(fs.getData());
        }
    }

    private void calcAggegatedFields(Map<Row, Row> output) {
        log.trace("calcAggegatedFields: " + output.size());
        for (Row keyRow : output.keySet() ) {
            Row valueRow = output.get(keyRow);
            for (Field f : selectFields) {
                if (groupFields.containsKey(f.getName())) {
                    Object groupByValue = keyRow.get(f.getName());
                    valueRow.getMap().put(f.getName(), groupByValue);
                } else {
                    Object o = evaluate(f, valueRow );
                    valueRow.getMap().put(f.getName(), o);
                }
            }
        }
    }

    private void populateRow(Row row, FieldSource fs, Collection<Field> fields) {
        for (Field f : fields) {
            Object o = evaluate(f, fs);
            row.getMap().put(f.getName(), o);
        }
    }


    public void pleaseImplementSerializable() {

    }
}
