package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.CreateCommand;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CreateCommandHandler implements ComponentHandler {

    private final CommandHandler commandHandler;

    public CreateCommandHandler( CommandHandler commandHandler ) {
        this.commandHandler = commandHandler;
    }

    public Class getComponentClass() {
        return CreateCommand.class;
    }

    public String getAlias() {
        return "create";
    }

    public Element toXml( Component c ) {
        CreateCommand g = (CreateCommand) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, g );
        return e2;
    }

    private void populateXml( Element e2, CreateCommand g ) {
        commandHandler.populateXml( e2, g );
        g.populateLocalXml(e2);
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        CreateCommand g = new CreateCommand( res, name );
        g.fromLocalXml(el);
        return g;
    }

}
