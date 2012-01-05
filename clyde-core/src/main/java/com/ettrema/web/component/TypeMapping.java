package com.ettrema.web.component;

import java.io.Serializable;

public class TypeMapping implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public TypeMapping(String templateName, String contentType) {
        super();
        this.templateName = templateName;
        this.contentType = contentType;
    }
    public final String templateName;
    public final String contentType;
}
