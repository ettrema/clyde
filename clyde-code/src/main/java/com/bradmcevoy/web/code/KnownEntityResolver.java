package com.bradmcevoy.web.code;

import com.bradmcevoy.common.Path;
import java.io.IOException;
import java.util.List;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author brad
 */
public class KnownEntityResolver implements EntityResolver {

    private final List<String> knownSystemIds;

    public KnownEntityResolver( List<String> knownSystemIds ) {
        this.knownSystemIds = knownSystemIds;
    }
        
    @Override
    public InputSource resolveEntity( String publicId, String systemId ) throws SAXException, IOException {
        Path p = Path.path( systemId );
        if( knownSystemIds.contains( p.getName() ) ) {
            return new InputSource( this.getClass().getResourceAsStream( "/" + p.getName() ) );
        } else {
            return null;
        }
    }
}
