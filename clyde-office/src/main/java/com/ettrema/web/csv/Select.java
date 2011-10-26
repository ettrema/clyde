package com.bradmcevoy.web.csv;

import com.bradmcevoy.web.query.Field;
import java.io.Serializable;
import java.util.List;

public class Select implements Serializable {

    private static final long serialVersionUID = 1L;
    private String type;
    private Select subSelect;
    private List<Field> fields;

    public Select(String type, Select subSelect, List<Field> fields) {
        this.type = type;
        this.subSelect = subSelect;
        this.fields = fields;
    }

    public List<Field> getFields() {
        return fields;
    }

    public Select getSubSelect() {
        return subSelect;
    }

    public String getType() {
        return type;
    }
}
