package com.bradmcevoy.web.csv;

import java.io.Serializable;

public class Field implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;

    public Field(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
