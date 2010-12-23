package com.bradmcevoy.web;

import java.util.List;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.utils.JDomUtils;
import com.bradmcevoy.web.component.Addressable;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.csv.CsvService;
import com.bradmcevoy.web.csv.FieldAndName;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

/**
 *
 * @author brad
 */
public class CsvSubPage extends SubPage implements Replaceable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CsvSubPage.class );
    private static final long serialVersionUID = 1L;
    public Path sourceFolder;
    /**
     * only include files which satisfy the is('') test for isType
     */
    private String isType;
    private List<FieldAndName> fields;

    public CsvSubPage( CommonTemplated parent, String name ) {
        super( parent, name );
    }

    public CsvSubPage( Addressable container, Element el ) {
        super( container, el );
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.setString( e2, "type", isType );
        InitUtils.set( e2, "sourceFolder", sourceFolder );
        populateFieldsInXml( e2 );

    }

    public void populateFieldsInXml( Element e2 ) {
        if( fields != null && fields.size() > 0 ) {
            Element eFields = new Element( "csvfields" );
            e2.addContent( eFields );
            for( FieldAndName f : fields ) {
                Element elField = new Element( "csvfield" );
                eFields.addContent( elField );
                elField.setAttribute( "name", f.getName() );
                elField.setText( f.getExpr() );
            }
        }
    }

    @Override
    public void loadFromXml( Element el ) {
        super.loadFromXml( el );
        isType = InitUtils.getValue( el, "type" );
        String s = InitUtils.getValue( el, "sourceFolder", "." );
        sourceFolder = Path.path( s );
        loadFieldsFromXml( el );
    }

    public void loadFieldsFromXml( Element el ) {
        List<Element> elFields = JDomUtils.childrenOf( el, "csvfields" );
        if( elFields.size() > 0 ) {
            this.fields = new ArrayList<FieldAndName>();
            for( Element elField : JDomUtils.childrenOf( el, "csvfields" ) ) {
                FieldAndName f = new FieldAndName( elField.getAttributeValue( "name" ), elField.getText() );
                this.fields.add( f );
            }
        } else {
            this.fields = null;
        }
    }

    public void replaceContent( InputStream in, Long length ) {
        log.trace( "replaceContent" );
        try {
            _( CsvService.class ).replaceContent( in, length, isType, sourceFolder, this.getParentFolder() );
            this.commit();
        } catch( Exception ex ) {
            this.rollback();
            throw new RuntimeException( ex );
        }
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        log.trace( "sendContent: direct" );
        _( CsvService.class ).generate( out, fields, isType, sourceFolder, this.getParentFolder() );
    }

    @Override
    public void sendContent( WrappedSubPage requestedPage, OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        log.trace( "sendContent: wrapped" );
        _( CsvService.class ).generate( out, fields, isType, sourceFolder, requestedPage.getParentFolder() );
    }

    public String getIsType() {
        return isType;
    }

    public void setIsType( String isType ) {
        this.isType = isType;
    }

    public void setSourceFolderPath( Path sourceFolder ) {
        this.sourceFolder = sourceFolder;
    }

    public Path getSourceFolderPath() {
        return sourceFolder;
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String getContentType( String accepts ) {
        return getContentType();
    }
}
