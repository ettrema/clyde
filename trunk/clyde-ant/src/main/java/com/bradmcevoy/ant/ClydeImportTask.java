package com.bradmcevoy.ant;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.RootFolder;
import com.ettrema.context.Context;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.jdom.Element;

public class ClydeImportTask extends AbstractClydeTask {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( ClydeImportTask.class );
    private File folder;
    private boolean recurse = true;

    public ClydeImportTask() {
    }

    void execute( Context context ) throws BuildException {
        try {
            if( folder == null ) throw new NullPointerException( "folder" );
            log.debug( "import execute: path=" + path + " folder:" + folder.getAbsolutePath() );
            final Path p = Path.path( path );
            log.debug( "Path name=" + p.getName() );
            log.debug( "Path path=" + p.toString() );
            VfsSession sess = context.get( VfsSession.class );
            DataNode dn;
            if( p.isRoot() ) {
                log.debug( "using root as destination" );
                Folder root = new RootFolder( sess.root() );
                root.save();
                dn = root;
            } else {
                log.debug( "looking for import destination: " + p );
                NameNode n = sess.find( p );
                if( n == null ) {
                    log.warn( "node not found: " + p );
                    return;
                }
                log.debug( "importing to folder: " + folder.getAbsolutePath() + " from node: " + n.getId() );
                dn = n.getData();
            }
            if( dn instanceof Folder ) {
                Folder folderDataNode = (Folder) dn;
                importFolder( folderDataNode, folder );
            } else {
                throw new RuntimeException( "not a folder: " + p );
            }
            sess.commit();
        } catch( Throwable ex ) {
            ex.printStackTrace();
            log.error( "exception doing import task", ex );
            throw new RuntimeException( ex );
        }
    }

    public boolean isRecurse() {
        return recurse;
    }

    public void setRecurse( boolean recurse ) {
        this.recurse = recurse;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder( File folder ) {
        this.folder = folder;
    }

    private void importFolder( Folder folderDataNode, File folder ) {
        if( !folder.isDirectory() )
            throw new IllegalArgumentException( "not a folder: " + folder.getAbsolutePath() );
        log.debug( "importFolder: " + folderDataNode.getHref() + " from " + folder.getAbsolutePath() );
        File[] arr = folder.listFiles();
        if( arr == null ) return;
        // process templates folder first since other stuff depends on it
        for( File f : arr ) {
            if( f.getName().equals( "templates.meta.xml" ) ) {
                doImport( folderDataNode, f );
                break;
            }
        }
        for( File f : arr ) {
            if( f.getName().endsWith( ".meta.xml" ) && !f.getName().equals( "templates.meta.xml" ) ) {
                doImport( folderDataNode, f );
            }
        }
    }

    private void doImport( Folder folder, File f ) {
        log.debug( "processing meta file: " + f.getAbsolutePath() );
        Importer importer = new Importer( folder, f );
        BaseResource res = importer.go();
        if( res instanceof Folder && recurse ) {
            Folder newFolder = (Folder) res;
            File nextDir = new File( f.getParentFile(), res.getName() );
            if( !nextDir.exists() || !nextDir.isDirectory() ) return;
            importFolder( newFolder, nextDir );
        }
    }

    class Importer {

        final Folder parentFolder;
        final File metaFile;

        Importer( Folder parentFolder, File metaFile ) {
            this.parentFolder = parentFolder;
            this.metaFile = metaFile;
        }

        BaseResource go() {
            throw new RuntimeException( "not implemented");
//            Document doc;
//            try {
//                XmlUtils2 utilXml = new XmlUtils2();
//                doc = utilXml.getJDomDocument( metaFile );
//            } catch( JDOMException ex ) {
//                throw new RuntimeException( metaFile.getAbsolutePath(), ex );
//            } catch( FileNotFoundException ex ) {
//                throw new RuntimeException( metaFile.getAbsolutePath(), ex );
//            }
//            Element el = doc.getRootElement();
//            el = el.getChild( "res" );
//            String filename = metaFile.getName().replace( ".meta.xml", "" );
//            BaseResource resExisting = parentFolder.childRes( filename );
//            BaseResource res = null;
//            if( resExisting != null ) {
//                resExisting.loadFromXml( el );
//                res = resExisting;
//                log.debug( "found existing resource: " + res );
//            } else {
//                res = importResource( parentFolder, el, filename );
//                log.debug( "created resource: " + res );
//            }
//            res.save();
//            if( res instanceof BinaryFile ) {
//                BinaryFile bf = (BinaryFile) res;
//                File f = new File( metaFile.getParentFile(), res.getName() );
//                if( f.exists() ) {
//                    bf.setContent( f );
//                    long sz = bf.getContentLength();
//                    if( f.length() != sz )
//                        throw new RuntimeException( "file sizes dont match: " + f.length() + " != " + sz );
//                    log.debug( "NEW binary file: " + sz );
//                } else {
//                    log.warn( "no content file: " + f.getAbsolutePath() );
//                }
//            } else if( res instanceof TextFile ) {
//                TextFile tf = (TextFile) res;
//                File f = new File( metaFile.getParentFile(), res.getName() );
//                String s;
//                try {
//                    s = FileUtils.readFile( f );
//                } catch( FileNotFoundException ex ) {
//                    throw new RuntimeException( ex );
//                }
//                tf.setContent( s );
//                tf.save();
//            }
//            return res;
        }
    }

    public static BaseResource importResource( BaseResource parent, Element el, String filename ) {
        return BaseResource.importResource( parent, el, filename );
    }
}
