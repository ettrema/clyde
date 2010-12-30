package com.bradmcevoy.web.component;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.process.ActionHandler;
import com.bradmcevoy.process.ProcessContext;
import com.bradmcevoy.vfs.VfsCommon;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.RequestParams;
import java.util.Map;
import org.jdom.Element;

public abstract class Command extends VfsCommon implements Component, Addressable, ActionHandler {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Command.class);
    private static final long serialVersionUID = 1L;
    
    String name;
    
    protected Addressable container;

    /**
     * Gets called from process management. You should implement onProcess to
     * call this method to do its stuff
     *
     * @param rc
     * @param parameters
     * @param files
     * @return
     */
    protected abstract String doProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) throws NotAuthorizedException;
    
    public Command(Addressable container, String name) {
        this.container = container;
        this.name = name;
    }
    
    public Command(Addressable container, Element el) {
        this.container = container;
        fromXml(el);
    }
    
    /**
     * 
     * @return - a number which indicates the relative order to place commands
     */
    public int getSignificance() {
        return 5;
    }
    
    @Override
    public void init(Addressable container) {
        this.container = container;
    }

    @Override
    public Path getPath() {
        return container.getPath().child(name);
    }
    
    
    @Override
    public Addressable getContainer() {
        return container;
    }


    public void fromXml(Element el) {
        name = el.getAttributeValue("name");        
    }
    
    @Override
    public Element toXml(Addressable container,Element el) {
        Element e2 = new Element("component");
        el.addContent(e2);
        populateXml(e2);
        return e2;
    }



    @Override
    public String render(RenderContext rc) {
        Path path = CommonComponent.getPath(this, rc);
        return "<button type='submit' id='" + path + "' name='" + path + "' value='" + name + "' >" + name + "</button>";
    }

    @Override
    public String renderEdit(RenderContext rc) {
        return "";
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public void onPreProcess(RenderContext rc, Map<String, String> parameters, Map<String, FileItem> files) {
    }

    /**
     * Fired by process management
     *
     * @param context
     */
    @Override
    public void process(ProcessContext context) {
        try {
            BaseResource res = (BaseResource) context.getAttribute("res");
            RenderContext rc = new RenderContext( null, res, null, false );
            rc.addAttribute( "processContext", context );
            for( Map.Entry entry : context.getAttributes().entrySet() ) {
                if( entry != null && entry.getKey() != null ) {
                    rc.addAttribute( entry.getKey().toString(), entry.getValue() );
                }
            }
//        Map<String,String> map = new HashMap<String, String>();
            if( !validate( rc ) ) {
                log.debug( "validation failed" );
                return;
            }
            log.debug( "doing command ..." );
            String newHref = doProcess( rc, null, null );
        } catch( NotAuthorizedException ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public void populateXml(Element e2) {
        e2.setAttribute("class",getClass().getName());
        e2.setAttribute("name",name);
    }
    
    public final void setValidationMessage( String s ) {
        RequestParams params = RequestParams.current();
        params.attributes.put( this.getName() + "_validation", s );
    }

    public final String getValidationMessage() {
        RequestParams params = RequestParams.current();
        return (String) params.attributes.get( this.getName() + "_validation" );
    }
}
