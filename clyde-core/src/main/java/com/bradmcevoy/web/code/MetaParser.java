package com.bradmcevoy.web.code;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;

/**
 *
 * @author brad
 */
public class MetaParser {

    private static List<String> knownSystemIds = Arrays.asList( "core.dtd","xhtml-lat1.ent","xhtml-special.ent","xhtml-symbol.ent","xhtml1-strict.dtd");

    private final CodeResourceFactory rf;
    private final EntityResolver entityResolver;

    public MetaParser( CodeResourceFactory rf ) {
        this.rf = rf;
        entityResolver = new KnownEntityResolver( knownSystemIds );
    }

    public Resource createNew( CollectionResource parent, String newName, InputStream inputStream ) {
        try {
            Document doc = parse( inputStream );
            Element elItem = getItemElement( doc );
            String itemAlias = elItem.getName();
            MetaHandler handler = null;
            for( MetaHandler<?> h : rf.getMetaHandlers() ) {
                for( String alias : h.getAliases() ) {
                    if( alias.equals( itemAlias ) ) {
                        handler = h;
                        break;
                    }
                }
            }
            if( handler == null ) {
                throw new RuntimeException( "No suitable meta handler: " + itemAlias );
            }

            return handler.createFromXml( parent, elItem, newName );
        } catch( JDOMException ex ) {
            throw new RuntimeException( "Exception parsing xml", ex );
        }
    }

    public Element getItemElement( Document doc ) {
        Element elRoot = doc.getRootElement();
        List childElements = elRoot.getChildren();
        if( CollectionUtils.isEmpty( childElements ) ) {
            throw new RuntimeException( "No meta item" );
        }
        Element elItem = (Element) childElements.get( 0 );
        return elItem;
    }

    public Document parse( InputStream in ) throws JDOMException {
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setExpandEntities( false );
            builder.setEntityResolver( entityResolver );
            //builder.setFeature(  "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            return builder.build( in );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }
}
