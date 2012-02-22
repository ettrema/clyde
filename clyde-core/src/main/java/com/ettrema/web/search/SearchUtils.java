package com.ettrema.web.search;

import com.ettrema.vfs.NameNode;
import com.ettrema.web.BaseResource;
import com.ettrema.web.Template;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

import static com.ettrema.context.RequestContext._;
import com.ettrema.logging.LogUtils;
import com.ettrema.vfs.VfsSession;

/**
 *
 * @author brad
 */
public class SearchUtils {
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SearchUtils.class);  
    
    public static BaseResource getResource(Document doc) {
        String sId = doc.get("id");
        if (sId == null || sId.length() == 0) {
            log.warn("No document id in search result");
            return null;
        } else {
            UUID id = UUID.fromString(sId);
            NameNode nn = _(VfsSession.class).get(id);
            if (nn == null) {
                LogUtils.info(log, "getResource: no node found for id", id);
                return null;
            } else {
                BaseResource res = (BaseResource) nn.getData(); // todo: check type
                if (res == null) {
                    LogUtils.info(log, "getResource: no data node found for id", id);
                    return null;
                } else if (res instanceof Template) {
                    LogUtils.info(log, "getResource: not returning template", id);
                    return null;
                } else if (res.isTrash()) {
                    LogUtils.info(log, "getResource: not returning trashed file", id);
                    return null;
                } else {
                    return res;
                }
            }
        }
    }

    public static void addField(Document doc, String field, String value) throws IOException {
        if (value == null) {
            return;
        }
        doc.add(new Field(field, value, Field.Store.YES, Field.Index.ANALYZED));
    }

    public static Document doc(IndexSearcher searcher, TopDocs hits, int i) {
        if( i >= hits.scoreDocs.length ) {
            log.trace("index out of range");
            return null;
        }
        try {
            return searcher.doc(hits.scoreDocs[i].doc);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Document[] toArray(IndexSearcher searcher, TopDocs topDocs) {
        List<Document> list = new ArrayList<>();
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            Document doc = doc(searcher, topDocs, i);
            list.add(doc);
        }
        Document[] arr = new Document[list.size()];
        return list.toArray(arr);
    }

    public static void closeQuietly(IndexReader r) {
        if (r == null) {
            return;
        }
        try {
            r.close();
        } catch (IOException ex) {
        }
    }

    public static void closeQuietly(Directory index) {
        if (index == null) {
            return;
        }
        try {
            index.close();
        } catch (IOException ex) {
        }
    }

    public static void closeQuietly(IndexWriter w) {
        if (w == null) {
            return;
        }
        try {
            w.close();
        } catch (CorruptIndexException ex) {
        } catch (IOException ex) {
        }
    }

    public static void closeQuietly(Analyzer w) {
        if (w == null) {
            return;
        }

        w.close();
    }

    public static void closeQuietly(org.apache.lucene.search.SearcherManager w) {
        if (w == null) {
            return;
        }
        try {
            w.close();
        } catch (CorruptIndexException ex) {
        } catch (IOException ex) {
        }
    }
}
