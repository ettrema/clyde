package com.ettrema.web.search;

import com.bradmcevoy.http.exceptions.MiltonException;
import com.ettrema.logging.LogUtils;
import com.ettrema.web.*;
import com.ettrema.web.component.ComponentDef;
import com.ettrema.web.component.ComponentValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import static com.ettrema.web.search.SearchUtils.*;
import java.util.Collection;
import org.apache.lucene.queryParser.MultiFieldQueryParser;

public class HostSearchManager {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HostSearchManager.class);
    private final DirectoryFactory directoryFactory;
    private final String hostName;
    private Directory dir;
    private final StandardAnalyzer analyzer;
    private final IndexWriter indexWriter;
    private org.apache.lucene.search.SearcherManager mgr;
    private boolean closed;

    public HostSearchManager(DirectoryFactory directoryFactory, String hostName) throws CorruptIndexException {
        this.hostName = hostName;
        this.directoryFactory = directoryFactory;
        try {
            dir = this.directoryFactory.open(hostName);
            analyzer = new StandardAnalyzer(Version.LUCENE_35);
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, analyzer);
            indexWriter = new IndexWriter(dir, conf);
            mgr = new SearcherManager(indexWriter, true, null, null);
        } catch (IOException ex) {
            throw new RuntimeException("EXception opening directory: " + hostName, ex);
        }
    }

    public void close() {
        closed = true;
        closeQuietly(mgr);
        closeQuietly(indexWriter);
        closeQuietly(analyzer);
        closeQuietly(dir);
    }

    public synchronized void index(BaseResource res) throws CorruptIndexException {
        try {
            deleteOldDoc(res);
            doIndex(res);
        } catch (CorruptIndexException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void deleteOldDoc(BaseResource res) throws CorruptIndexException, IOException {
        Term uidTerm = new Term("id", res.getNameNodeId().toString());
        try {
            indexWriter.deleteDocuments(uidTerm);
        } catch (OutOfMemoryError e) {
            close();
        }
    }

    private void doIndex(BaseResource res) {
        if (res.isTrash()) {
            log.trace("not indexing trashed resource");
            return;
        }
        if (res.isInTemplates()) {
            log.trace("not indexing template resource");
            return;
        }

        try {
            Document doc = new Document();
            Field fId = new Field("id", res.getNameNodeId().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED);
            doc.add(fId);
            Field fName = new Field("name", res.getName(), Field.Store.YES, Field.Index.ANALYZED);
            doc.add(fName);
            addField(doc, "path", res.getUrl());

            ITemplate t = res.getTemplate();
            if (t != null) {
                RenderContext rc = new RenderContext(t, res, null, false);
                Collection<ComponentDef> vals = t.getComponentDefs().values();
                if (vals == null || vals.isEmpty()) {
                    log.trace("No values to index");
                } else {
                    for (ComponentDef def : vals) {
                        ComponentValue cv = res.getValues().get(def.getName());
                        String s = "";
                        if (cv != null) {
                            s = cv.render(rc);
                        }
                        LogUtils.trace(log, "doIndex: add field", def.getName(), s);
                        addField(doc, def.getName(), s);
                    }
                }
                if (res instanceof Page) {
                    if (!res.hasValue("body")) {
                        String content = res.render(null);
                        LogUtils.trace(log, "doIndex: add body field", content);
                        addField(doc, "body", content);
                    }
                }
            } else {
                log.trace("No template so not indexing fields");
            }
            indexWriter.addDocument(doc);
            LogUtils.trace(log, "doIndex. Indexed page", doc.get("id"), res.getName());
        } catch (IOException ex) {
            throw new RuntimeException("EXception opening directory: " + hostName, ex);
        }
    }

    public Document[] search(Query q) throws ParseException {
        try {
            mgr.maybeReopen();
            IndexSearcher searcher = null;
            try {
                searcher = mgr.acquire();
                try {
                    TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
                    searcher.search(q, collector);
                    TopDocs topDocs = collector.topDocs();
                    return toArray(searcher, topDocs);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } finally {
                mgr.release(searcher);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public StandardAnalyzer getAnalyzer() {
        return analyzer;
    }

    public boolean isClosed() {
        return closed;
    }
}
