package com.ettrema.web.component;

import com.bradmcevoy.common.Path;
import com.ettrema.web.RenderContext;
import com.ettrema.web.Templatable;
import org.jdom.Element;

public abstract class AbstractDef<T> implements ComponentDef, Addressable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( AbstractDef.class );
    private static final long serialVersionUID = 1L;
    protected Addressable container;
    protected final Text name = new Text( this, "name" );
    protected String validationMessage;

    @Override
    public abstract String formatValue( Object v );

    protected abstract String editChildTemplate();

    public AbstractDef( Addressable container, String name ) {
        this.container = container;
        this.name.setValue( name );
    }

    public AbstractDef( Addressable container, Element el ) {
        this.container = container;
        this.name.setValue( el.getAttributeValue( "name" ) );
    }

    @Override
    public void init( Addressable container ) {
        this.container = container;
    }

    @Override
    public Addressable getContainer() {
        return this.container;
    }

    @Override
    public boolean validate( ComponentValue c, RenderContext rc ) {
        return true;
    }

    @Override
    public boolean validate( RenderContext rc ) {
        if( name.getValue() == null || name.getValue().trim().length() == 0 ) {
            validationMessage = "Please enter a name";
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Element toXml( Addressable container, Element el ) {
        Element e2 = new Element( "componentDef" );
        el.addContent( e2 );
        e2.setAttribute( "class", getClass().getName() );
        e2.setAttribute( "name", getName() );
        return e2;
    }

    @Override
    public Path getPath() {
        return container.getPath().child( name.getValue() );
    }

    @Override
    public ComponentValue createComponentValue( Templatable newPage ) {
        ComponentValue cv = new ComponentValue( name.getValue(), newPage );
        cv.init( newPage );
        cv.setValue( "" );
        return cv;
    }

    @Override
    public String getName() {
        return name.getValue();
    }

    public Path getPath( RenderContext rc ) {
        Path p = rc.page.getPath();
        p = p.child( name.getValue() );
        return p;
    }
}
