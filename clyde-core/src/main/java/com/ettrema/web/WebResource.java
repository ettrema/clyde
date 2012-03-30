package com.ettrema.web;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a linked resource from a web page or template, such as a 
 * CSS or javascript file
 *
 * @author brad
 */
public class WebResource implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<String,String> atts = new HashMap<>();
    
    private String tag;
    
    private String body;

    /**
     * Eg <script>, <link>, <meta>
     * 
     * @return 
     */
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    
   
    /**
     * Eg src, property, content, type
     * 
     * @return 
     */
    public Map<String, String> getAtts() {
        return atts;
    }

    public void setAtts(Map<String, String> atts) {
        this.atts = atts;
    }

    /**
     * The body of the tag, such as an inline script
     * 
     * @return 
     */
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }           
    
    public String getHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(tag).append(" ");
        for( Entry<String, String> entry : atts.entrySet()) {
            sb.append(entry.getKey()).append("=\"").append(entry.getValue()).append("\" ");
        }
        if( body != null && body.length()>0 ) {
            sb.append(">").append(body).append("</").append(tag).append(">");
        } else {
            sb.append("/>");
        }
        return sb.toString();
    }
}
