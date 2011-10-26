package com.bradmcevoy.web.code.meta.comp;

import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.component.GroupEmailCommand2;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class GroupEmailCommandHandler implements ComponentHandler {

    private final CommandHandler commandHandler;

    public GroupEmailCommandHandler( CommandHandler commandHandler ) {
        this.commandHandler = commandHandler;
    }

    public Class getComponentClass() {
        return GroupEmailCommand2.class;
    }

    public String getAlias() {
        return "groupEmail";
    }

    public Element toXml( Component c ) {
        GroupEmailCommand2 g = (GroupEmailCommand2) c;
        Element e2 = new Element( getAlias(), CodeMeta.NS );
        populateXml( e2, g );
        return e2;
    }

    private void populateXml( Element e2, GroupEmailCommand2 g ) {
        commandHandler.populateXml( e2, g );
        g.populateLocalXml(e2);
    }

    public Component fromXml( CommonTemplated res, Element el ) {
        String name = el.getAttributeValue( "name" );
        GroupEmailCommand2 g = new GroupEmailCommand2( res, name );
        //String text = InitUtils.getValueOf( el, "c:text" );
        g.parseXml(el);
        return g;
    }

}
