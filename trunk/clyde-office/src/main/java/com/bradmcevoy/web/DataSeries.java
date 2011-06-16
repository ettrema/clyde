package com.bradmcevoy.web;

/**
 * 
 * how about this
 * 
 * instead of dateseries, use a Query
 * 
 * Query
 *  - FieldOperator
 *    - SelectOperator (includes aggregation)
 *    - UpdateOperator
 *    - InsertOperator
 *  - RecordSelector
 *    - has a from
 *    - has a where
 * 
 *
 * @author brad
 */
public interface DataSeries {
    public String getName();
    public TupleList getSeries(Object from, Object to);
}
