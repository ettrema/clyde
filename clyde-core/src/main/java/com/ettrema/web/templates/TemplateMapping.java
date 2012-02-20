package com.ettrema.web.templates;

import com.ettrema.web.CommonTemplated;
import com.ettrema.web.Folder;
import com.ettrema.web.Host;
import java.io.Serializable;
import java.util.List;

/**
 * Provides a mapping between a MIME content type and a template name
 * 
 * These are defined on a folder and propogate to subfolders
 *
 * @author brad
 */
public class TemplateMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    public static String findTemplateName(String contentType, CommonTemplated res) {
        if( res instanceof Folder ) {
            if( contentType == null ) {
                contentType = "folder"; // if the originall requested resource is a Folder, and the given content type is null, then it should be "folder"
            }
            return findTemplateName(contentType, (Folder)res);
        } else {
            Folder parent = res.getParentFolder();
            return findTemplateName(contentType, parent);
        }
    }
    
    private static String findTemplateName(String contentType, Folder folder) {
        String s = findTemplateName(contentType, folder.getTemplateMappings());
        if( s != null ) {
            return s;
        }
        if( folder instanceof Host ) {
            return null; // go no further
        } else {
            Folder parent = folder.getParentFolder(); // find from parent
            if( parent != null ) {
                return findTemplateName(contentType, parent);
            } else {
                return null;
            }
        }
    }
    
    private static String findTemplateName(String contentType, List<TemplateMapping> templateMappings) {
        if( templateMappings == null || templateMappings.isEmpty()) {
            return null;
        }
        for( TemplateMapping tm : templateMappings ) {
            if( contentType.contains(tm.getMimeType())) {
                return tm.getTemplateName();
            }
        }
        return null;
    }
    private final String mimeType;
    private final String templateName;

    public TemplateMapping(String mimeType, String templateName) {
        this.mimeType = mimeType;
        this.templateName = templateName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getTemplateName() {
        return templateName;
    }       
}
