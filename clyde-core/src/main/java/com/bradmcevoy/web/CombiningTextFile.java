package com.bradmcevoy.web;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.web.component.InitUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public class CombiningTextFile extends File {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CombiningTextFile.class );
    private static final long serialVersionUID = 1L;
    private List<Path> includes;

    public CombiningTextFile( String contentType, Folder parentFolder, String newName ) {
        super( contentType, parentFolder, newName );
    }

    public CombiningTextFile( Folder parentFolder, String newName ) {
        super( "text", parentFolder, newName );
    }

    @Override
    public String getDefaultContentType() {
        // since binary files can represent many different content types
        // we try to infer from the file name
        return ContentTypeUtil.getContentTypeString( getName() );
    }

    @Override
    protected BaseResource newInstance( Folder parent, String newName ) {
        return new CombiningTextFile( this.getContentType( null ), parent, newName );
    }

    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        if( includes != null ) {
            //ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            OutputStream tempOut = out;
            for( Path includeName : includes ) {
                Resource child = this.getParent().find( includeName );
                if( child == null ) {
                    log.warn("Couldnt find resource to imclude: " + includeName + " in folder: " + this.getParent().getHref());
                } else if( child instanceof GetableResource ) {
                    GetableResource gr = (GetableResource) child;
                    gr.sendContent( tempOut, range, params, contentType );
//                    tempOut.write( "\n".getBytes() ); // write CR
                } else {
                    log.warn("is not getable! " + child.getClass());
                }
            }
            //byte[] arr = tempOut.toByteArray();
            //log.debug( "wrote: " + arr.length);
            //out.write( arr );// temp hack
        } else {
            log.warn( "no includes defined for combiningtextfile: " + this.getName() );
        }
    }

    @Override
    public Date getModifiedDate() {
        Date mostRecent = super.getModifiedDate();
        if( includes != null ) {
            for( Path includeName : includes ) {
                Resource child = this.getParent().find( includeName );
                if( child instanceof GetableResource ) {
                    GetableResource gr = (GetableResource) child;
                    Date mo = gr.getModifiedDate();
                    if( mo != null ) {
                        if( mostRecent == null ) {
                            mostRecent = mo;
                        } else if( mo.after( mostRecent ) ) {
                            mostRecent = mo;
                        }
                    }
                }
            }
        }
        return mostRecent;
    }

    @Override
    public Long getContentLength() {
        return null;
//        log.trace("getContentLength");
//        if( includes != null ) {
//            long length = 0;
//            for( Path includeName : includes ) {
//                Resource child = this.getParent().find( includeName );
//                if( child instanceof GetableResource ) {
//                    GetableResource gr = (GetableResource) child;
//                    Long l = gr.getContentLength();
//                    if( l == null ) {
//                        return null;
//                    } else {
//                        log.trace(" add: " + gr.getName() + " - " + l);
//                        length += l;
//                    }
//                } else {
//                    return null;
//                }
//            }
//            log.trace("getContentLength: " + length);
//            return length;
//        } else {
//            return null;
//        }

    }




    public List<Path> getIncludes() {
        return includes;
    }

    public void setIncludes( List<Path> includes ) {
        this.includes = includes;
    }

    @Override
    public void populateXml( Element e2 ) {
        super.populateXml( e2 );
        String s = null;
        if( includes != null ) {
            for( Path name : includes ) {
                if( s != null ) s += ",";
                else s = "";
                s += name;
            }
        }
        InitUtils.setString( e2, "includes", s );
    }

    @Override
    public void loadFromXml( Element el ) {
        super.loadFromXml( el );
        String s = el.getAttributeValue( "includes" );
        includes = new ArrayList<Path>();
        if( s != null && s.trim().length() > 0 ) {
            String[] arr = s.split( "," );
            for( String name : arr ) {
                includes.add( Path.path( name ) );
            }
        }
    }


    @Override
    public boolean isIndexable() {
        return false;
    }


}
