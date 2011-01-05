package com.bradmcevoy.web.code;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.code.content.BinaryContentTypeHandler;
import com.bradmcevoy.web.code.content.PageContentTypeHandler;
import com.bradmcevoy.web.code.content.TemplateContentTypeHandler;
import com.bradmcevoy.web.code.content.TextFileContentHandler;
import com.bradmcevoy.web.code.meta.BaseResourceMetaHandler;
import com.bradmcevoy.web.code.meta.BinaryFileMetaHandler;
import com.bradmcevoy.web.code.meta.CommonTemplatedMetaHandler;
import com.bradmcevoy.web.code.meta.CsvViewMetaHandler;
import com.bradmcevoy.web.code.meta.FolderMetaHandler;
import com.bradmcevoy.web.code.meta.GroupMetaHandler;
import com.bradmcevoy.web.code.meta.PageMetaHandler;
import com.bradmcevoy.web.code.meta.PdfMetaHandler;
import com.bradmcevoy.web.code.meta.TemplateMetaHandler;
import com.bradmcevoy.web.code.meta.TextFileMetaHandler;
import com.bradmcevoy.web.code.meta.UserMetaHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author brad
 */
public final class CodeResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( CodeResourceFactory.class );
    private final ResourceFactory wrapped;
    private final MetaParser metaParser;
    private List<ContentTypeHandler> contentTypeHandlers;
    private List<MetaHandler> metaHandlers;
    private String root = "_code";
    private String metaSuffix = ".meta.xml";

    public CodeResourceFactory( ResourceFactory wrapped ) {
        this.wrapped = wrapped;
        PageContentTypeHandler pageContentTypeHandler = new PageContentTypeHandler();
        TemplateContentTypeHandler templateContentTypeHandler = new TemplateContentTypeHandler( pageContentTypeHandler );
        TextFileContentHandler textFileContentHandler = new TextFileContentHandler();
        setContentTypeHandlers( Arrays.asList( templateContentTypeHandler, pageContentTypeHandler, textFileContentHandler, new BinaryContentTypeHandler() ) );
        initMetaHandlers();
        this.metaParser = new MetaParser( this );
    }

    public Resource getResource( String host, String path ) {
        Resource r = find( host, path );
        if( log.isTraceEnabled() ) {
            if( r == null ) {
                log.trace( "not found: " + path );
            } else {
                log.trace( "found: " + r.getClass() + " at: " + path );
            }
        }
        return r;
    }

    private Resource find( String host, String path ) {
        if( log.isTraceEnabled() ) {
            log.trace( "getResource: " + path );
        }
        Path p = Path.path( path );
        String first = p.getFirst();
        if( root.equals( first ) ) {
            p = p.getStripFirst();
            if( isMeta( p ) ) {
                p = getPagePath( p );
                Resource r = wrapped.getResource( host, p.toString() );
                if( r == null ) {
                    return null;
                } else {
                    Resource parent = wrapped.getResource( host, p.getParent().toString() );
                    if( parent == null ) {
                        throw new RuntimeException( "Found resource, but could not find its parent at: " + p.getParent() );
                    } else if( !( parent instanceof CollectionResource ) ) {
                        throw new RuntimeException( "Found resource, but its parent is not of type CollectionResource. Is a: " + parent.getClass() + " - at path: " + p.getParent() );
                    }
                    return wrapMeta( r, (CollectionResource) parent );
                }

            } else {
                Resource r = wrapped.getResource( host, p.toString() );
                if( r == null ) {
                    log.trace( "not found" );
                    return null;
                } else {
                    if( r instanceof CollectionResource ) {
                        return wrapCollection( (CollectionResource) r );
                    } else {
                        if( r instanceof GetableResource ) {
                            log.trace( "return content" );
                            return wrapContent( (GetableResource) r );
                        } else {
                            log.warn( "Unsupported type: " + r.getClass() );
                            return null;
                        }
                    }
                }
            }
        } else {
            log.trace( "not code path" );
            return null;
        }
    }

    public boolean isIgnoredResource( Resource r ) {
        if( r instanceof Folder ) {
            Folder f = (Folder) r;
            if( f.isSystemFolder() ) {
                return true;
            } else {
                return f.getName().equals( "Trash" );
            }
        } else {
            return false;
        }
    }

    public boolean isMeta( Path path ) {
        if( path == null ) {
            return false;
        }
        return isMeta( path.getName() );
    }

    public boolean isMeta( String name ) {
        if( name == null ) {
            return false;
        }
        return name.endsWith( metaSuffix );
    }

    public Path getPagePath( Path path ) {
        String nm = path.getName().replace( metaSuffix, "" );
        return path.getParent().child( nm );
    }

    public String getPageName( String name ) {
        String nm = name.replace( metaSuffix, "" );
        return nm;
    }

    Resource wrapMeta( Resource r, CollectionResource parent ) {
        if( isIgnoredResource( r ) ) {
            return null;
        }
        MetaHandler mh = getMetaHandler( r );
        if( mh == null ) {
            return null;
        }
        return new CodeMeta( this, mh, r.getName() + metaSuffix, r, parent );
    }

    Resource wrapContent( GetableResource r ) {
        return new CodeContentPage( this, r.getName(), r );
    }

    Resource wrapCollection( CollectionResource col ) {
        if( isIgnoredResource( col ) ) {
            return null;
        }
        return new CodeFolder( this, col.getName(), col );
    }

    public void setContentTypeHandlers( List<ContentTypeHandler> list ) {
        this.contentTypeHandlers = list;
    }

    public List<ContentTypeHandler> getContentTypeHandlers() {
        return contentTypeHandlers;
    }

    public void setMetaHandlers( List<MetaHandler> metaHandlers ) {
        this.metaHandlers = metaHandlers;
    }

    public List<MetaHandler> getMetaHandlers() {
        return metaHandlers;
    }

    public ContentTypeHandler getContentTypeHandler( Resource wrapped ) {
        for( ContentTypeHandler cth : contentTypeHandlers ) {
            if( cth.supports( wrapped ) ) {
                return cth;
            }
        }
        return null;
        //throw new RuntimeException( "Unsupported type: " + wrapped.getClass() );
    }

    public MetaHandler getMetaHandler( Resource r ) {
        for( MetaHandler h : metaHandlers ) {
            if( h.supports( r ) ) {
                return h;
            }
        }
        return null;
    }

    private void initMetaHandlers() {
        Map<Class, String> mapOfAliases = new HashMap<Class, String>();
        this.metaHandlers = new ArrayList<MetaHandler>();

        CommonTemplatedMetaHandler commonTemplatedMetaHandler = new CommonTemplatedMetaHandler();
        BaseResourceMetaHandler baseResourceMetaHandler = new BaseResourceMetaHandler( commonTemplatedMetaHandler );
        BinaryFileMetaHandler binaryFileMetaHandler = add( new BinaryFileMetaHandler( baseResourceMetaHandler ), mapOfAliases );
        FolderMetaHandler folderMetaHandler = add( new FolderMetaHandler( baseResourceMetaHandler ), mapOfAliases );
        PageMetaHandler pageMetaHandler = add( new PageMetaHandler( baseResourceMetaHandler ), mapOfAliases );
        add( new GroupMetaHandler( folderMetaHandler ), mapOfAliases );
        add( new UserMetaHandler( folderMetaHandler ), mapOfAliases );
        add( new CsvViewMetaHandler( baseResourceMetaHandler ), mapOfAliases );
        add( new TextFileMetaHandler( baseResourceMetaHandler ), mapOfAliases );
        add( new PdfMetaHandler( binaryFileMetaHandler ), mapOfAliases );

        // Must be constructed last because uses mapOfAlias, but must be ahead of page because of supports
        add( new TemplateMetaHandler( pageMetaHandler, mapOfAliases ), mapOfAliases );
    }

    private <T extends MetaHandler> T add( T h, Map<Class, String> mapOfAliases ) {
        mapOfAliases.put( h.getInstanceType(), h.getAlias() );
        this.metaHandlers.add( 0, h );
        return h;
    }

    public MetaParser getMetaParser() {
        return metaParser;
    }

    public MetaHandler getMetaHandler( Element elItem ) {
        String itemElementName = elItem.getName();
        for( MetaHandler h : metaHandlers ) {
            if( supports( h, itemElementName ) ) {
                return h;
            }
        }
        return null;
    }

    private boolean supports( MetaHandler<? extends Resource> h, String itemElementName ) {
        return h.getAlias().equals( itemElementName );
    }
}
