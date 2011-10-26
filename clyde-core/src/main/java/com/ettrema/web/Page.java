package com.ettrema.web;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.property.BeanPropertyResource;
import com.ettrema.web.component.ComponentValue;
import com.ettrema.web.component.HtmlInput;
import com.ettrema.web.component.Text;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@BeanPropertyResource( "clyde" )
public class Page extends File implements Replaceable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Page.class );
    private static final long serialVersionUID = 1L;

    public Page( Folder parentFolder, String name ) {
        super( "text/html", parentFolder, name );

    }

    @Override
    public String getDefaultContentType() {
        return "text/html";
    }

   
    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new Page( parent, newName );
    }

    @Override
    protected void initComponents() {
        super.initComponents();
    }

    @Override
    void setContent( InputStream in ) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            StreamUtils.readTo( in, bout );
        } catch( ReadingException ex ) {
            throw new RuntimeException( ex );
        } catch( WritingException ex ) {
            throw new RuntimeException( ex );
        }
        HtmlInput root = new HtmlInput( this, "root" );
        root.setValue( bout.toString() );
        componentMap.add( root );
        save();
    }

    @Override
    public boolean is( String type ) {
        if( type == null ) {
            return false;
        }
        if( super.is( type ) ) {
            return true;
        } else {
            return ( type.equals( "html" ) || type.equals( "page" ) );
        }
    }

    @Override
    public void replaceContent( InputStream in, Long arg1 ) {
        log.debug( "replaceContent" );
        ReplaceableHtmlParser parser = new ReplaceableHtmlParserImpl();
        ITemplate template = this.getTemplate();
        ComponentDefMap defs;
        Map<String, String> mapOfVals;

        ByteArrayOutputStream out;
        try {
            out = new ByteArrayOutputStream();
            StreamUtils.readTo( in, out );
        } catch( ReadingException ex ) {
            log.warn( "reading exception" );
            return;
        } catch( WritingException ex ) {
            throw new RuntimeException( ex );
        }

        if( template == null || template.getName().equals( "root" ) ) {

            Component cRoot = getComponent( "root" );
            if( cRoot == null ) {
                cRoot = new HtmlInput( this, "root" );
                this.getComponents().add( cRoot );
            }
            if( cRoot instanceof Text ) {
                Text tRoot = (Text) cRoot;
                tRoot.setValue( out.toString() );
                this.save();
                this.commit();

            } else {
                log.warn( "i dont know how to set component type: " + cRoot.getClass().getCanonicalName() );
            }
        } else {
            defs = template.getComponentDefs();
            if( defs != null ) {
                if( defs.size() > 0 ) {
                    Set<String> names = defs.keySet();
                    mapOfVals = parser.parse( out.toString(), names );
                    log.debug( "found values: " + mapOfVals.size() );
                    for( Map.Entry<String, String> entry : mapOfVals.entrySet() ) {
                        ComponentValue cv = this.getValues().get( entry.getKey() );
                        if( cv != null ) {
                            cv.value = entry.getValue();
                        } else {
                            log.warn( "no value: " + entry.getKey() );
                        }
                    }
                    log.debug( "saving" );
                    this.save();
                    this.commit();
                } else {
                    log.warn( "empty component defs, cant replace. template: " + template.getHref() );
                }
            } else {
                log.warn( "no component defs map, can't replace content" );
            }
        }
    }

    @Override
    public boolean isIndexable() {
        return true;
    }
}
