
package com.bradmcevoy.web.search;

import com.bradmcevoy.context.RequestContext;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.exceptions.MiltonException;
import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ITemplate;
import com.bradmcevoy.web.RenderContext;
import com.bradmcevoy.web.component.ComponentDef;
import com.bradmcevoy.web.component.ComponentValue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class HostSearchManager {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HostSearchManager.class);
    
    private static final Map<String,HostSearchManager> map = new ConcurrentHashMap<String, HostSearchManager>();
    
    final String hostName;

    public static synchronized HostSearchManager getInstance(String hostName) {
        HostSearchManager mgr = map.get(hostName);
        if( mgr == null ) {
            mgr = new HostSearchManager(hostName);
            map.put(hostName, mgr);
        }
        return mgr;
    }
    
    private HostSearchManager(String hostName) {
        this.hostName = hostName;
    }
    
    public File getDir() {
        File f = new File("/webs/search");
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
            index = FSDirectory.getDirectory(f);
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
            w = new IndexWriter(index, new StandardAnalyzer(), create);
            Document doc = new Document();
            ITemplate t = res.getTemplate();
            if( t == null ) {
                log.warn("Cant index because no template. res:" + res.getPath());
                return ;
            }
            
            Field fId = new Field("id", res.getNameNodeId().toString(), Field.Store.YES,  Field.Index.TOKENIZED);
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
            Field html = new Field("html", content, Field.Store.NO,  Field.Index.TOKENIZED);
            doc.add(html);
            Field fName = new Field("name", res.getName(), Field.Store.YES,  Field.Index.TOKENIZED);
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
        doc.add(new Field(field, value, Field.Store.YES,   Field.Index.TOKENIZED));
    }    

    
    public Hits search(String query) throws ParseException {
        String[] fields = new String[]{"title","name","html"};
        Map boosts = new HashMap();
        boosts.put("name", new Float(10));
        boosts.put("title", new Float(5));
        return search(fields,query,boosts);
    }
    
    public Hits search(String field, String query) throws ParseException {
        String[] fields = new String[]{field};
        return search(fields,query, null);
    }
    
    public Hits search(String[] fields, String query, Map boosts) throws ParseException {
        StringBuffer sb = new StringBuffer();
        // add a search term which is the quoted query and give it a x4 boost
        //sb.append('"').append(query).append('"').append("^4 ").append(query);
        sb.append(query);
        StandardAnalyzer ana = new StandardAnalyzer();
        MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, ana); //, boosts);
        Query q = parser.parse(sb.toString());
        //Query q = new QueryParser(field, new StandardAnalyzer()).parse(sb.toString());
        BooleanQuery bq;
        return search(q);
    }
    
    public Hits search(Query q) throws ParseException {        
        File f = getDir();
        Directory index;
        IndexWriter w = null;
        try {
            index = FSDirectory.getDirectory(f,false);
        } catch (IOException ex) {
            throw new RuntimeException("EXception opening directory: " + f.getAbsolutePath(), ex);
        }
        
        VfsSession session = RequestContext.getCurrent().get(VfsSession.class);
        try {
            IndexSearcher s = new IndexSearcher(index);
            Hits hits = s.search(q);
            return hits;
        } catch (IOException ex) {
            throw new RuntimeException(f.getAbsolutePath(), ex);
        }
    }
    
    public static Document doc(Hits hits, int i) {
        try {
            return hits.doc(i);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
