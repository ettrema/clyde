package com.bradmcevoy.ant;

import com.bradmcevoy.common.FrameworkBase;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamToStream;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.BinaryFile;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.TextFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.apache.tools.ant.BuildException;
import org.jdom.Document;
import org.jdom.Element;

public class ClydeExportTask extends AbstractClydeTask {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClydeExportTask.class);
    
    private File folder;
    
    private boolean reportOnly;
    
    public ClydeExportTask() {
    }
    
    @Override
    void execute(Context context) throws BuildException {
        try {
            log.debug("exporting: path: " + this.path);
            if( folder == null ) throw new NullPointerException("folder");
            final Path p = Path.path(path);
            VfsSession sess = context.get(VfsSession.class);
            NameNode n = sess.find(p);
            if( n == null ) {
                log.warn("node not found: " + p);
                return;
            }
            log.debug("exporting to folder: " + folder.getAbsolutePath() + " from node: " + n.getId());
            
            DataNode dn = n.getData();
            if( dn instanceof Folder ) {
                Folder folderDataNode = (Folder)dn;
                export(folderDataNode,folder);
            } else {
                throw new RuntimeException("not a folder: " + p + " : " + dn.getClass().getName());
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            log.error("exception doing export",ex);
            throw new RuntimeException(ex);
        }
    }
    
    public File getFolder() {
        return folder;
    }
    
    public void setFolder(File folder) {
        this.folder = folder;
    }

    public boolean isReportOnly() {
        return reportOnly;
    }

    public void setReportOnly(boolean reportOnly) {
        this.reportOnly = reportOnly;
    }
    

    private void export(Folder folderDataNode, File folder) {
        Exporter exporter = new Exporter(folderDataNode, folder);
        exporter.go();
        File destDir = new File(folder,folderDataNode.getName());
        destDir.mkdirs();
        
        for( Resource r : folderDataNode.getChildren() ) {
            if( r instanceof Folder ) {
                export((Folder) r, destDir);
            } else if( r instanceof BaseResource ) {
                exporter = new Exporter((BaseResource) r, destDir);
                exporter.go();
            }
        }
    }
    
    
    class Exporter extends FrameworkBase {
        final BaseResource res;
        final File destDir;
        
        Exporter(BaseResource res, File destDir) {
            this.res = res;
            this.destDir = destDir;
        }
        
        void go() {
            log.debug("exporting: " + res.getPath() + "(" + res.getName() + ")" );
            if( isReportOnly() ) return ;
            
            File dest = new File(destDir,res.getName() + ".meta.xml");
            Document doc = new Document();
            Element resRoot = new Element("res");
            doc.addContent(resRoot);
            res.toXml(resRoot);
            try {
                utilXml().saveXMLDocument(dest,doc); 
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            if( res instanceof BinaryFile ) {
                BinaryFile bf = (BinaryFile)res;
                File f = new File(destDir,bf.getName());
                InputStream in = bf.getInputStream();
                writeToFile(f, in);
            } else if( res instanceof TextFile ) {
                TextFile tf = (TextFile)res;
                File f = new File(destDir,tf.getName());
                ByteArrayInputStream in = new ByteArrayInputStream(tf.getContent().getBytes());
                writeToFile(f, in);                
            }
            
        }

        private void writeToFile(final File f, final InputStream in) throws RuntimeException {
            try {
                StreamToStream.readTo(in,f,true);
            } catch (ReadingException ex) {
                throw new RuntimeException(ex);
            } catch (WritingException ex) {
                throw new RuntimeException(ex);
            }
        }
        
    }
    
}
