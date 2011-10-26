
package com.ettrema.web.component;

import com.bradmcevoy.http.FileItem;
import com.ettrema.web.Component;
import com.ettrema.web.RenderContext;
import java.util.Map;
import org.jdom.Element;

public class WrappedComponent implements Component, WrappableComponent, ValueHolder {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WrappedComponent.class);
    private static final long serialVersionUID = 1L;
    
    private final Addressable container;
    private final WrappableComponent wrapped;

    public WrappedComponent(Addressable container, WrappableComponent wrapped) {
        this.container = container;
        this.wrapped = wrapped;
    }
        
    
    @Override
    public void init(Addressable container) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Addressable getContainer() {
        return container;
    }

    @Override
    public boolean validate(RenderContext rc) {
        return wrapped.validate(container,rc);
    }

    @Override
    public String render(RenderContext rc) {
        return wrapped.render(container,rc);
    }

    @Override
    public String renderEdit(RenderContext rc) {
        return wrapped.renderEdit(container,rc);
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return wrapped.onProcess(container, rc, parameters, files);
    }

    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        wrapped.onPreProcess(container, rc, parameters, files);
    }

    @Override
    public Element toXml(Addressable container, Element el) {
        return wrapped.toXml(container,el);
    }
    
    @Override
    public Object getValue() {
        return wrapped.getValue(container);
    }

    public String getFormattedValue() {
        return wrapped.getFormattedValue(container);
    }

    @Override
    public String toString() {
        return getFormattedValue();
    }
    
    

    
    
    @Override
    public void onPreProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        wrapped.onPreProcess(container, rc, parameters, files);
    }

    @Override
    public String onProcess(Addressable container, RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return wrapped.onProcess(container, rc, parameters, files);
    }

    @Override
    public String render(Addressable container, RenderContext rc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String renderEdit(Addressable container, RenderContext rc) {
        return wrapped.renderEdit(container,rc);
    }

    @Override
    public boolean validate(Addressable container, RenderContext rc) {
        return wrapped.validate(container, rc);
    }

    @Override
    public Object getValue(Addressable container) {
        return wrapped.getValue(container);
    }

    @Override
    public String getFormattedValue(Addressable container) {
        return wrapped.getFormattedValue(container);
    }

    public final String getValidationMessage() {
        return wrapped.getValidationMessage();
    }
}
