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
import com.bradmcevoy.web.code.meta.BaseResourceMetaHandler;
import com.bradmcevoy.web.code.meta.BinaryFileMetaHandler;
import com.bradmcevoy.web.code.meta.CommonTemplatedMetaHandler;
import com.bradmcevoy.web.code.meta.CsvViewMetaHandler;
import com.bradmcevoy.web.code.meta.FolderMetaHandler;
import com.bradmcevoy.web.code.meta.GroupMetaHandler;
import com.bradmcevoy.web.code.meta.PageMetaHandler;
import com.bradmcevoy.web.code.meta.TemplateMetaHandler;
import com.bradmcevoy.web.code.meta.UserMetaHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        setContentTypeHandlers( Arrays.asList( templateContentTypeHandler, pageContentTypeHandler, new BinaryContentTypeHandler() ) );
        initMetaHandlers();
        this.metaParser = new MetaParser( this );
    }

    public Resource getResource( String host, String path ) {
        Resource r = find( host, path );
        if( r == null ) {
            log.trace( "not found: " + path );
        } else {
            log.trace( "found: " + r.getClass() + " at: " + path );
        }
        return r;
    }

    private Resource find( String host, String path ) {
        log.warn( "getResource: " + path );
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
                    return wrapMeta( r );
                }

            } else {
                Resource r = wrapped.getResource( host, p.toString() );
                if( r == null ) {
                    log.warn( "not found: " + p );
                    return null;
                } else {
                    if( r instanceof CollectionResource ) {
                        return wrapCollection( (CollectionResource) r );
                    } else {
                        if( r instanceof GetableResource ) {
                            log.trace( "return content" );
                            return wrapContent( (GetableResource) r );
                        } else {
                            log.warn( "Unsupported typoe: " + r.getClass() );
                            return null;
                        }
                    }
                }
            }
        } else {
            log.warn( "not code path" );
            return null;
        }
    }

    public boolean isIgnoredResource( Resource r ) {
        if( r instanceof Folder ) {
            Folder f = (Folder) r;
            if( f.isSystemFolder() ) {
                return true;
            } else {
                return f.getName().equals( "Trash");
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

    Resource wrapMeta( Resource r ) {
        if(isIgnoredResource( r )) {
            return null;
        }
        MetaHandler mh = getMetaHandler( r );
        if( mh == null ) {
            return null;
        }
        return new CodeMeta( this, mh, r.getName() + metaSuffix, r );
    }

    Resource wrapContent( GetableResource r ) {
        return new CodeContentPage( this, r.getName(), r );
    }

    Resource wrapCollection( CollectionResource col ) {
        if( isIgnoredResource( col )) {
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
        CommonTemplatedMetaHandler commonTemplatedMetaHandler = new CommonTemplatedMetaHandler();
        BaseResourceMetaHandler baseResourceMetaHandler = new BaseResourceMetaHandler( commonTemplatedMetaHandler );
        BinaryFileMetaHandler binaryFileMetaHandler = new BinaryFileMetaHandler( baseResourceMetaHandler );
        FolderMetaHandler folderMetaHandler = new FolderMetaHandler( baseResourceMetaHandler );
        GroupMetaHandler groupMetaHandler = new GroupMetaHandler( folderMetaHandler );
        PageMetaHandler pageMetaHandler = new PageMetaHandler( baseResourceMetaHandler );
        UserMetaHandler userMetaHandler = new UserMetaHandler( folderMetaHandler );
        CsvViewMetaHandler csvViewMetaHandler = new CsvViewMetaHandler( baseResourceMetaHandler );

        Map<Class,String> mapOfAliases = new HashMap<Class, String>();
        for(MetaHandler<?> h : Arrays.asList( userMetaHandler, groupMetaHandler, folderMetaHandler, pageMetaHandler, binaryFileMetaHandler, csvViewMetaHandler)) {
            for(String alias : h.getAliases()) {
                mapOfAliases.put( h.getInstanceType(), alias);
            }
        }

        TemplateMetaHandler templateMetaHandler = new TemplateMetaHandler( pageMetaHandler, mapOfAliases);

        this.metaHandlers = new ArrayList<MetaHandler>();
        Collections.addAll( metaHandlers, templateMetaHandler, userMetaHandler, groupMetaHandler, folderMetaHandler, pageMetaHandler, binaryFileMetaHandler, csvViewMetaHandler );
    }

    public MetaParser getMetaParser() {
        return metaParser;
    }
}
