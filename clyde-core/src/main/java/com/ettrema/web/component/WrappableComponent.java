package com.bradmcevoy.web.component;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.RenderContext;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public interface WrappableComponent {

    public String getName();

    public void onPreProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files);

    public String onProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files);

    public String render(Addressable container, RenderContext rc);

    public String renderEdit(Addressable container, RenderContext rc);

    public Element toXml(Addressable container, Element el);

    public boolean validate(Addressable container, RenderContext rc);
    
    public Object getValue(Addressable container);
    
    public String getFormattedValue(Addressable container);

    public String getValidationMessage();

}
