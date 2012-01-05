package com.ettrema.web.code;

import com.bradmcevoy.common.Path;
import java.io.IOException;
import java.io.InputStream;
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
            String res =  "/" + p.getName();            
            //System.out.println("KnownEntityResolver: found systemId: " + systemId + " - resource: " + res);
            InputStream resourceStream = this.getClass().getResourceAsStream(res);
            return new InputSource( resourceStream );
        } else {
            return null;
        }
    }
}
