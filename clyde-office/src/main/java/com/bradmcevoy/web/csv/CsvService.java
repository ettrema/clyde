package com.bradmcevoy.web.csv;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.Formatter;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.Templatable;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.mvel.MVEL;

/**
 *
 * @author brad
 */
public class CsvService {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CsvService.class );
    private final Formatter formatter = new Formatter();

    public void generate( OutputStream out, String isType, Path sourceFolder, Folder folder ) throws IOException {
        generate( out, null, isType, sourceFolder, folder );
    }

    public void generate( OutputStream out, List<FieldAndName> fields, String isType, Path sourceFolder, Folder folder ) throws IOException {
        PrintWriter pw = new PrintWriter( out );
        CSVWriter writer = new CSVWriter( pw );
        Folder source = getSourceFolder( sourceFolder, folder );
        for( Resource res : source.getChildren() ) {
            if( res instanceof CommonTemplated && ( res != this ) ) {
                CommonTemplated tres = (CommonTemplated) res;
                if( isType == null || isType.length() == 0 || tres.is( isType ) ) {
                    output( tres, writer, fields );
                }
            }
        }
        pw.flush();
        pw.close();
    }

    public Folder getSourceFolder( Path sourceFolder, Folder folder ) {
        if( sourceFolder == null ) {
            return folder;
        }
        return (Folder) folder.find( sourceFolder );
    }

    private void output( CommonTemplated tres, CSVWriter writer, List<FieldAndName> fields ) throws IOException {
        List<String> vals = new ArrayList<String>();
        ITemplate template = tres.getTemplate();
        if( template == null ) {
            log.warn( "Couldnt find template for page: " + tres.getPath() );
            return;
        }

        if( fields == null ) {
            vals.add( tres.getName() );
            vals.add( template.getName() );
            for( ComponentDef def : template.getComponentDefs().values() ) {
                String s = getTextualValue( def, tres );
                if( s == null ) {
                    vals.add( "" );
                } else {
                    vals.add( s );
                }
            }
        } else {
            for( FieldAndName f : fields ) {
                String s = getTextualValue( f.getExpr(), tres );
                vals.add( s );
            }
        }
        String[] arr = new String[vals.size()];
        vals.toArray( arr );
        writer.writeNext( arr );
    }

    private String getTextualValue( ComponentDef def, CommonTemplated tres ) {
        ComponentValue val = tres.getValues().get( def.getName() );
        if( val == null ) return null;
        Object o = val.getValue();
        if( o == null ) return null;
        String s = def.formatValue( o );
        if( s == null ) return null;
        return s;
    }

    private String getTextualValue( String expr, CommonTemplated tres ) {
        try {
            Object o = MVEL.eval( expr, tres );
            if( o instanceof ComponentValue ) {
                ComponentValue cv = (ComponentValue) o;
                return cv.getFormattedValue( tres );
            } else {
                return formatter.format( o );
            }
        } catch( Exception e ) {
            log.error("Expression: " + expr + " resource: " + tres.getHref(), e);
            return "ERR";
        }
    }

    public void replaceContent( InputStream in, Long length, String isType, Path sourceFolder, Folder folder ) throws IOException {
        InputStreamReader r = new InputStreamReader( in );
        CSVReader reader = new CSVReader( r );
        List<BaseResource> processed = new ArrayList<BaseResource>();
        Folder source = getSourceFolder( sourceFolder, folder );
        String[] lineParts;
        while( ( lineParts = reader.readNext() ) != null ) {
            if( lineParts.length > 0 ) {
                List<String> lineList = new ArrayList<String>();
                lineList.addAll( Arrays.asList( lineParts ) );
                BaseResource res = doProcess( source, lineList );
                processed.add( res );
            }
        }
        List<? extends Resource> existing;
        if( isType != null && isType.length() > 0 ) {
            existing = folder.getChildren( isType );
        } else {
            existing = folder.getChildren();
        }
        for( BaseResource res : processed ) {
            existing.remove( res );
        }

        List<? extends Resource> toDelete = existing;
        for( Resource resToDelete : toDelete ) {
            log.debug( "..will delete: " + resToDelete.getName() );
            log.warn( "deleting disabled" );
        }

    }

    /**
     * Use the given tokenised line of values to locate, create and/or update
     * a resource
     *
     * @param line
     */
    private BaseResource doProcess( Folder source, List<String> line ) {
        String name = line.get( 0 );
        String templateName;
        if( line.size() > 1 ) {
            templateName = line.get( 1 );
            line.remove( 0 );
        } else {
            templateName = "";
        }
        line.remove( 0 );
        return doProcess( source, name, templateName, line );
    }

    private BaseResource doProcess( Folder source, String name, String templateName, List<String> line ) {
        BaseResource tres = (BaseResource) source.child( name );
        // todo: implement changing template
        if( tres == null ) {
//            log.debug("..creating a: " + templateName + " called " + name);
            ITemplate newTemplate = source.getWeb().getTemplate( templateName );
            if( newTemplate == null ) {
                throw new RuntimeException( "No template called: " + templateName + " could be found" );
            } else {
                tres = newTemplate.createPageFromTemplate( source, name );
            }
            tres.save();
        }
        doUpdate( tres, line );
        return tres;
    }

    /**
     * For each component definition, grab a value from the line and update it
     *
     * @param tres
     * @param line
     */
    private Templatable doUpdate( Templatable tres, List<String> line ) {
//        log.debug("..doUpdate: " + tres.getName());
        int pos = 0;
        ITemplate template = tres.getTemplate();
        boolean isChanged = false;
        for( ComponentDef def : template.getComponentDefs().values() ) {
            ComponentValue val = tres.getValues().get( def.getName() );
            if( val == null ) {
                val = def.createComponentValue( tres );
                tres.getValues().add( val );
            }
            Object oldVal = val.getValue();
            if( pos < line.size() ) {
                String sNewVal = line.get( pos++ );
                Object newVal = def.parseValue( val, tres, sNewVal );
                if( !equal( oldVal, newVal ) ) {
//                    log.debug( "setting val: " + def.getName() + " to " + newVal);
                    val.setValue( newVal );
                    def.changedValue( val );
                    isChanged = true;
                } else {
//                    log.debug("..NOT setting val: " + def.getName() + " to " + newVal);
                }
            } else {
                log.debug( "no value for: " + def.getName() );
            }
        }
        if( isChanged ) {
            log.debug( "..saving: " + tres.getName() );
            if( tres instanceof BaseResource ) {
                ( (BaseResource) tres ).save();
            }
        }
        return tres;
    }

    private boolean equal( Object oldVal, Object newVal ) {
        if( oldVal == null ) {
            return ( newVal == null );
        } else {
            if( newVal == null ) {
                return false;
            } else {
                return oldVal.equals( newVal );
            }
        }
    }
}
