package com.ettrema.web.code;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
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
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( MetaParser.class );
    private static List<String> knownSystemIds = Arrays.asList( "core.dtd", "xhtml-lat1.ent", "xhtml-special.ent", "xhtml-symbol.ent", "xhtml1-strict.dtd" );
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
            MetaHandler<? extends Resource> handler = null;
            for( MetaHandler<? extends Resource> h : rf.getMetaHandlers() ) {
                if( h.getAlias().equals( itemAlias ) ) {
                    handler = h;
                    break;
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
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            IOUtils.copy(in, bout);
        } catch (IOException ex) {
            throw new JDOMException("IOException reading data", ex);
        } finally {
            IOUtils.closeQuietly(bout);            
        }
                
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        try {
            SAXBuilder builder = new SAXBuilder();
            builder.setExpandEntities( false );
            builder.setEntityResolver( entityResolver );
            //builder.setFeature(  "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            return builder.build( bin );
        } catch( Exception ex ) {
            log.error("Exception processing:" + bout.toString());
            throw new RuntimeException( ex );
        }
    }
}
