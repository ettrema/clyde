package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.GenerateThumbsCommand;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class GenerateThumbsCommandHandler implements ComponentHandler {

    private final CommandHandler commandHandler;

    public GenerateThumbsCommandHandler( CommandHandler commandHandler ) {
        this.commandHandler = commandHandler;
    }

    public Class getComponentClass() {
        return GenerateThumbsCommand.class;
    }

    public String getAlias() {
        return "genthumbs";
    }

    public Element toXml( Component c ) {
        GenerateThumbsCommand g = (GenerateThumbsCommand) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, g );
        return e2;
    }

    private void populateXml( Element e2, GenerateThumbsCommand g ) {
        commandHandler.populateXml( e2, g );
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        GenerateThumbsCommand g = new GenerateThumbsCommand( res, name );
        return g;
    }

}
