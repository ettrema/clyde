
package com.ettrema.web.component;

import com.bradmcevoy.http.FileItem;
import com.ettrema.web.RenderContext;
import java.util.Map;
import org.jdom.Element;


public class FileInput extends AbstractInput<FileItem> {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileInput.class);
    private static final long serialVersionUID = 1L;
    
    private transient FileItem fileItem;
    
    public FileInput(Addressable container, String name) {
        super(container, name);
    }

    public FileInput(Addressable container, Element el) {
        super(container, el);
    }    
    @Override
    protected String editTemplate() {
        return "<input type='file' name='${path}' />";
    }

    @Override
    protected FileItem parse(String s) {
        return fileItem;
    }

    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        String paramName = getPath(rc).toString();
        if( !files.containsKey(paramName) ) return ;
        fileItem = files.get(paramName);
        log.debug("fileitem: " + fileItem.getSize());
    }

    @Override
    public String toString() {
        if( fileItem == null ) {
            return "no file";
        } else {
            return fileItem.getName();
        }
    }

    
}
