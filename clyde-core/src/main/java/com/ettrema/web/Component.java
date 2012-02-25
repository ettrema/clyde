package com.ettrema.web;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.web.component.Addressable;
import java.io.Serializable;
import java.util.Map;
import org.jdom.Element;

@SuppressWarnings( "serial" )
public interface Component extends Serializable {

    public void init(Addressable container);

    /**
     * 
     * @return - the container this component was initialized on
     */
    public Addressable getContainer();
    
    /**
     *  Calling this method should have the side effect that any error message
     *  information is populated
     * 
     * @return - true if this component is in a valid state
     */
    public boolean validate(RenderContext rc);

    /**
     * Retrieve the validation message set from the call to validate in this
     * request.
     *
     * Note that this generally must not internally use a member variable, but
     * should store the value in a request attribute
     *
     * @return
     */
    public String getValidationMessage();
    
    /** Generate HTML for this component. That is this component, not any
     *  particular instance of it
     */
    String render(RenderContext rc);
    
    /** Generate HTML to edit any configuration values stored in this component
     */
    String renderEdit(RenderContext rc);

    String getName();
       

    /** Called after OnPreProcess for all components
     *
     *  Commands should check to see if they have been invoked and if so execute
     */
    String onProcess(RenderContext rc,Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException;

    /** Called on all components before any have process called
     *
     *  Inputs should load their value from request params
     */
    void onPreProcess(RenderContext rc,Map<String, String> parameters, Map<String, FileItem> files);

    Element toXml(Addressable container, Element el);

}
