package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.jdom.Element;

public abstract class AbstractInput<T> extends CommonComponent implements Component, Addressable, ValidatingComponent {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractInput.class);
    
    private static final long serialVersionUID = 1L;
    
    protected Addressable container;
    
    protected String name;
    private T value;
    protected String validationMessage;
    protected boolean requestScope;
    protected boolean required;
    protected String type = "text";  // text, hidden, etc
    
    protected abstract String editTemplate();
    
    protected abstract T parse(String s);
    
    public AbstractInput(Addressable container, String name) {
        this(container, name, false);
    }
    
    public AbstractInput(Addressable container, String name, boolean requestScope) {
        this.container = container;
        this.name = name;
        this.requestScope = requestScope;
    }

    public AbstractInput(Addressable container, Element el) {
        this.container = container;
        fromXml(el);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }    
    
    @Override
    public void init(Addressable container) {
        if( container == null ) throw new IllegalArgumentException("container is null");
        this.container = container;
    }

    public String getHtmlId() {
        String s = getPath().toString();
        if( s.startsWith("/")) s = s.substring(1);
        s = s.replaceAll("/", "-");
        s = s.replace('.', '_');
        return s;
    }
    
    @Override
    public boolean validate(RenderContext rc) {
        validationMessage = null;
        if( required ) {
            if( getValue() == null ) {
                validationMessage = "Required field";
                log.debug(" validation failed on required field: " + this.getName());
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    
    @Override
    public Addressable getContainer() {
        return container;
    }
    
    public void fromXml(Element el) {
        name = el.getAttributeValue("name");
        requestScope = InitUtils.getBoolean(el, "requestScope");
        required = InitUtils.getBoolean(el, "required");
        String s = InitUtils.getValue(el);
        value = parse(s);
    }
    
    @Override
    public Element toXml(Addressable container,Element el) {
        Element e2 = new Element("component");
        el.addContent(e2);
        e2.setAttribute("name",name);                
        e2.setAttribute("class",this.getClass().getName());
        InitUtils.setBoolean(e2, "requestScope", requestScope);
        InitUtils.setBoolean(e2, "required", required);
        String s = getFormattedValue();
        if( s != null ) {
            if( s.contains( "<![CDATA")) {
                e2.addContent( s );
            } else {
//                CDATA data = new CDATA(s);
//                e2.addContent(data);
                e2.setText( s);
            }
        }
        return e2;
    }

    public String getFormattedValue() {
        T v = getValue();
        if( v == null ) return "";
        return v.toString();
    }
    
    @Override
    public String render(RenderContext rc) {
        String s = getFormattedValue();
        if( s == null ) return "";
        return s;
    }

    @Override
    public String renderEdit(RenderContext rc) {
        String template = editTemplate();
        if( validationMessage != null ) {
            template = template + "<span class='validationMessage'>${input.validationMessage}</span>";
        }        
        VelocityContext vc = velocityContext(rc, value);
        vc.put("formattedValue", this.getFormattedValue());
        return _render(template, vc);
    }

            
    
    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        String paramName = getPath(rc).toString();
        if( !parameters.containsKey(paramName) ) return ;
        String s = parameters.get(paramName);
        T t = parse(s);
        if( requestScope ) {
            RequestParams.current().attributes.put(this.getName(),t);
        } else {
            if( s != null ) setValue(t);
        }
    }

    public void setValue(T t) {
        this.value = t;
    }
    
    @Override
    public String onProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
        return null;
    } 

    @Override
    public String getName() {
        return name;
    }
        
    @Override
    public Path getPath() {                
        return RenderContext.findPath((Component)this);
    }
    
    
    public T getValue() {
        if( requestScope ) {
            RequestParams rq = RequestParams.current();
            if( rq == null ) {
                return null;
            } else {
                return (T) rq.attributes.get(this.getName());
            }
        } else {
            return value;
        }
    }
    


    @Override
    public String toString() {
        T o = getValue();
        if( o == null ) return "";
        return o.toString();
    }      


    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    
    public void setRequestScope(boolean requestScope) {
        this.requestScope = requestScope;
    }

    public boolean isRequestScope() {
        return requestScope;
    }

    public boolean isRequired() {
        return required;
    }

    
}
