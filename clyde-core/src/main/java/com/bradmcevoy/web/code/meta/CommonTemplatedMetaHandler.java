package com.bradmcevoy.web.code.meta;

import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.code.meta.comp.ComponentHandler;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Component;
import com.bradmcevoy.web.code.CodeMeta;
import com.bradmcevoy.web.code.meta.comp.AbstractInputHandler;
import com.bradmcevoy.web.code.meta.comp.CommandHandler;
import com.bradmcevoy.web.code.meta.comp.DateValueHandler;
import com.bradmcevoy.web.code.meta.comp.DefaultValueHandler;
import com.bradmcevoy.web.code.meta.comp.GroupEmailCommandHandler;
import com.bradmcevoy.web.code.meta.comp.SubPageHandler;
import com.bradmcevoy.web.code.meta.comp.TemplateInputHandler;
import com.bradmcevoy.web.code.meta.comp.TextHandler;
import com.bradmcevoy.web.code.meta.comp.ValueHandler;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.component.NameInput;
import com.bradmcevoy.web.component.TemplateSelect;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CommonTemplatedMetaHandler {

    private Map<Class, ComponentHandler> mapOfComponentHandlersByClass;
    private Map<String, ComponentHandler> mapOfComponentHandlersByAlias;
    private Map<Class, ValueHandler> mapOfValueHandlers;
    private Map<String, ValueHandler> mapOfValueAliases;

    public CommonTemplatedMetaHandler() {
        mapOfComponentHandlersByClass = new LinkedHashMap<Class, ComponentHandler>();
        mapOfComponentHandlersByAlias = new LinkedHashMap<String, ComponentHandler>();
        AbstractInputHandler abstractInputHandler = new AbstractInputHandler();
        CommandHandler commandHandler = new CommandHandler();
        GroupEmailCommandHandler groupEmailCommandHandler = new GroupEmailCommandHandler( commandHandler );
        TextHandler textHandler = new TextHandler( abstractInputHandler );
        TemplateInputHandler templateInputHandler = new TemplateInputHandler( textHandler );
        add( templateInputHandler );
        add( groupEmailCommandHandler );
        add( textHandler );
        add( new SubPageHandler( this ) );

        mapOfValueHandlers = new LinkedHashMap<Class, ValueHandler>();
        mapOfValueAliases = new LinkedHashMap<String, ValueHandler>();
        DefaultValueHandler defaultValueHandler = new DefaultValueHandler();
        add( new DateValueHandler( defaultValueHandler ) );
        add( defaultValueHandler );
    }

    private void add( ComponentHandler h ) {
        mapOfComponentHandlersByClass.put( h.getComponentClass(), h );
        mapOfComponentHandlersByAlias.put(h.getAlias(), h);
        
    }

    private void add( ValueHandler h ) {
        mapOfValueHandlers.put( h.getComponentValueClass(), h );
        mapOfValueAliases.put(h.getAlias(), h);
    }

    public void populateXml( Element el, CommonTemplated res, boolean includeContentVals ) {
        populateValues( res, el, includeContentVals );
        populateComponents( res, el );
        InitUtils.setString( el, "template", res.getTemplateName() );

        InitUtils.setString( el, "contentType", res.getContentType() );
    }

    private void populateComponents( CommonTemplated res, Element el ) {
        Element e2 = null;

        for( Component c : res.getComponents().values() ) {
            if( isIgnoredComponent( c ) ) {
                // ignore
            } else {
                if( e2 == null ) {
                    e2 = new Element( "components", CodeMeta.NS );
                }
                ComponentHandler ch = mapOfComponentHandlersByClass.get( c.getClass() );
                if( ch == null ) {
                    throw new RuntimeException( "No component handler for: " + c.getClass() );
                }
                Element eComp = ch.toXml( c );
                e2.addContent( eComp );
            }
        }
        if( e2 != null ) {
            el.addContent( e2 );
        }
    }

    private boolean isIgnoredComponent( Component c ) {
        return ( c instanceof TemplateSelect )
            || ( c instanceof NameInput )
            || c.getName().equals( "class" );
    }

    private boolean isIgnoredComponent( String name ) {
        return name.equals( "template" )
            || name.equals( "name" )
            || name.equals( "class" );
    }


    private void populateValues( CommonTemplated res, Element el, boolean includeContentVals ) {
        Element e2 = null;
        for( ComponentValue cv : res.getValues().values() ) {
            if( isIgnoredVal( cv, includeContentVals ) ) {
                // ignore
            } else {
                if( e2 == null ) {
                    e2 = new Element( "attributes", CodeMeta.NS );
                }
                ValueHandler h = mapOfValueHandlers.get( cv.getClass() );
                if( h == null ) {
                    throw new RuntimeException( "No handler for: " + cv.getClass() );
                }
                Element elVal = h.toXml( cv, res );
                e2.addContent( elVal );
            }
        }
        if( e2 != null ) {
            el.addContent( e2 );
        }
    }

    private boolean isIgnoredVal( ComponentValue val, boolean includeContentVals ) {
        return isIgnoredVal( val.getName(), includeContentVals );
    }

    private boolean isIgnoredVal( String name, boolean includeContentVals ) {
        if( includeContentVals ) {
            return false;
        } else {
            // these fields are output in the content for html pages
            return name.equals( "body" ) || name.equals( "title" );
        }
    }

    public void updateFromXml( CommonTemplated res, Element el ) {
        updateFromXml( res, el, false);
    }

    public void updateFromXml( CommonTemplated res, Element el, boolean includeContentVals ) {
        updateValues( res, el, includeContentVals );
        updateComponents( res, el );
        // TODO: handle values for non body+title

        res.setTemplateName( InitUtils.getValue( el, "template" ) );
        res.setContentType( InitUtils.getValue( el, "contentType" ) );

    }

    private void updateValues( CommonTemplated res, Element el, boolean includeContentVals ) {
        // Remove all cv's except title and body
        Iterator<Entry<String, ComponentValue>> it = res.getValues().entrySet().iterator();
        while( it.hasNext() ) {
            Entry<String, ComponentValue> entry = it.next();
            if( !isIgnoredVal( entry.getKey(), includeContentVals ) ) {
                it.remove();
            }
        }
        
        for( Element eAtt : JDomUtils.childrenOf( el, "attributes" )) {
            ValueHandler h = mapOfValueAliases.get( eAtt.getName() );
            ComponentValue cv = h.fromXml(res, eAtt);
            res.getValues().add( cv );
        }

    }

    private void updateComponents( CommonTemplated res, Element el ) {
        Iterator<Entry<String, Component>> it = res.getComponents().entrySet().iterator();
        while( it.hasNext() ) {
            Entry<String, Component> entry = it.next();
            if( !isIgnoredComponent( entry.getKey() ) ) {
                it.remove();
            }
        }

        for( Element eAtt : JDomUtils.childrenOf( el, "components" )) {
            ComponentHandler h = mapOfComponentHandlersByAlias.get( eAtt.getName());
            if( h == null) {
                throw new RuntimeException( "Couldnt find component handler for element of type: " + eAtt.getName());
            }
            Component c = h.fromXml(res, eAtt);
            res.getComponents().add( c );
        }
    }

}
