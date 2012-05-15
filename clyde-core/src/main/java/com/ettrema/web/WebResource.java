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
            String adjustedValue = adjustRelativePath(entry.getKey(), entry.getValue());
            sb.append(entry.getKey()).append("=\"").append(adjustedValue).append("\" ");
        }
        if( body != null && body.length()>0 ) {
            sb.append(">").append(body).append("</").append(tag).append(">");
        } else {
            sb.append("/>");
        }
        return sb.toString();
    }

    /**
     * If the attribute name is src or href, checks the value to see if
     * its relative, and if so return an absolute path, assuming webresource
     * root is /templates
     * 
     * @param value
     * @return 
     */
    private String adjustRelativePath(String name, String value) {
        if( name.equals("href") || name.equals("src")) {
            if( value != null && value.length() > 0 ) {
                if( !value.startsWith("/") && !value.startsWith("http")) {
                    return "/templates/" + value;
                }
            }
        }
        return value;
    }
}
