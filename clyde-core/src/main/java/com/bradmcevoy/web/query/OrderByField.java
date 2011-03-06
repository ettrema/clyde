package com.bradmcevoy.web.query;

/**
 *
 * @author brad
 */
public class OrderByField extends Field {
    private static final long serialVersionUID = 1L;

    public enum Direction {
        ascending,
        descending
    }

    private Direction ascending;

    public Direction getDirection() {
        return ascending;
    }

    public void setDirection(Direction ascending) {
        this.ascending = ascending;
    }




}
