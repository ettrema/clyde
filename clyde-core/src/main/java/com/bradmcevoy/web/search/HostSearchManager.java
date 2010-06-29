
package com.bradmcevoy.web.search;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.exceptions.MiltonException;
import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class HostSearchManager {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HostSearchManager.class);

    private final String baseDir;
    
    private final String hostName;

    private IndexSearcher searcher;
    
    public HostSearchManager(String baseDir, String hostName) throws CorruptIndexException {
        this.hostName = hostName;
        this.baseDir = baseDir;
        Directory index;
        IndexWriter w = null;
        File f = getDir();
        if( !f.exists() ) {
            log.debug("directory does not exist: " + f.getAbsolutePath());
        } else {
            try {
                index = FSDirectory.open(f);
                searcher = new IndexSearcher(index);
            } catch (IOException ex) {
                throw new RuntimeException("EXception opening directory: " + f.getAbsolutePath(), ex);
            }
        }
    }
    
    public File getDir() {
        File f = new File(baseDir);
        if( f.exists() ) f.mkdir();
        return new File(f,hostName);
    }
    
    public synchronized void index(BaseResource res) {
        if( res.isTrash()) {
            return ;
        }
        log.debug("indexing: " + res.getPath());
        File f = getDir();
        Directory index;
        boolean create = !f.exists();
        
        IndexWriter w = null;
        try {
            index = FSDirectory.open(f);
        } catch (IOException ex) {
            throw new RuntimeException("EXception opening directory: " + f.getAbsolutePath(), ex);
        }
        
        if( !create ) {
            IndexReader ir = null;
            try{
                ir = IndexReader.open(index);
                Term uidTerm = new Term("id", res.getNameNodeId().toString());
                int count = ir.deleteDocuments(uidTerm);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                FileUtils.close(ir);
            }
            
        } else {
            create = true;
        }        
                
        try {
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
            w = new IndexWriter(index, analyzer, create, IndexWriter.MaxFieldLength.LIMITED);
            Document doc = new Document();
            ITemplate t = res.getTemplate();
            if( t == null ) {
                log.warn("Cant index because no template. res:" + res.getPath());
                return ;
            }
            
            Field fId = new Field("id", res.getNameNodeId().toString(), Field.Store.NO, Field.Index.ANALYZED);
            doc.add(fId);
            
            RenderContext rc = new RenderContext(t, res, null, false);
            for( ComponentDef def : t.getComponentDefs().values() ) {
                ComponentValue cv = res.getValues().get(def.getName());                
                String s = "";
                if( cv != null ) {
                    s = cv.render(rc);
                }
                addField(doc,def.getName(),s);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if( res instanceof Folder ) {
                Folder folder = (Folder) res;
                GetableResource indexPage = folder.getIndexPage();
                try {
                    indexPage.sendContent(out, null, null, null);
                } catch (MiltonException ex) {
                    log.error("couldnt generate content for index page", ex);
                }
            } else {                
                res.sendContent(out, null, null, null);
            }

            String content = out.toString();
            Field html = new Field("html", content, Field.Store.NO,  Field.Index.ANALYZED);
            doc.add(html);
            Field fName = new Field("name", res.getName(), Field.Store.YES,  Field.Index.ANALYZED);
            doc.add(fName);
            w.addDocument(doc);
        } catch (IOException ex) {
            throw new RuntimeException("EXception opening directory: " + f.getAbsolutePath(), ex);
        } catch(MiltonException e) {
            throw new RuntimeException( e );
        } finally {
            FileUtils.close(w);
            FileUtils.close(index);
        }        
    }
    
    private void addField(Document doc, String field, String value) throws IOException {
        if( value == null ) return ;
        doc.add(new Field(field, value, Field.Store.YES,   Field.Index.ANALYZED));
    }    

    
    
    public Document[] search(Query q) throws ParseException {
        try {
            TopScoreDocCollector collector = TopScoreDocCollector.create(20, true) ;
            searcher.search(q, collector);
            TopDocs topDocs = collector.topDocs();
            return toArray(topDocs);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public Document doc(TopDocs hits, int i) {
        try {
            return searcher.doc(hits.scoreDocs[i].doc);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Document[] toArray(TopDocs topDocs) {
        List<Document> list = new ArrayList<Document>();
        for( int i=0; i<topDocs.totalHits; i++ ) {
            Document doc = doc(topDocs, i);
            list.add(doc);
        }
        Document[] arr = new Document[list.size()];
        return list.toArray(arr);
    }
}
