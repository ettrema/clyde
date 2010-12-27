package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.web.component.InitUtils;
import com.bradmcevoy.web.csv.CsvService;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.jdom.Element;

import static com.ettrema.context.RequestContext._;

public class CsvView extends com.bradmcevoy.web.File implements Replaceable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CsvView.class );
    private static final long serialVersionUID = 1L;
    public Path sourceFolder;
    /**
     * only include files which satisfy the is('') test for isType
     */
    private String isType;

    public CsvView( String contentType, Folder parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    public CsvView( Folder parentFolder, String newName ) {
        this( "text/csv", parentFolder, newName );
    }

    @Override
    public String getDefaultContentType() {
        return "text/csv";
    }



    @Override
    protected String getHelpDescription() {
        return "Dynamically generates a CSV representaton of resources of a given type in a specified folder";
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        InitUtils.setString( e2, "type", isType );
        InitUtils.set( e2, "sourceFolder", sourceFolder );
    }

    @Override
    public void loadFromXml( Element el ) {
        super.loadFromXml( el );
        isType = InitUtils.getValue( el, "type" );
        String s = InitUtils.getValue( el, "sourceFolder", "." );
        sourceFolder = Path.path( s );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new CsvView( parent, newName );
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        _( CsvService.class ).generate( out, isType, sourceFolder, this.getParent() );
    }

    public void replaceContent( InputStream in, Long length ) {
        try {
            _( CsvService.class ).replaceContent( in, length, isType, sourceFolder, this.getParent() );
            this.commit();
        } catch( Exception ex ) {
            this.rollback();
            throw new RuntimeException( ex );
        }
    }

    @Override
    public boolean isIndexable() {
        return false;
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
}
