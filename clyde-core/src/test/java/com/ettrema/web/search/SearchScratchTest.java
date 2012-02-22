package com.ettrema.web.search;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author brad
 */
public class SearchScratchTest {

    File indexDir;
    Directory dir;
    IndexWriter indexWriter;
    SearcherManager mgr;
    StandardAnalyzer analyzer;

    @Before
    public void setup() throws IOException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        indexDir = new File(tmpDir, "searchTest" + System.currentTimeMillis());
        dir = FSDirectory.open(indexDir);
        analyzer = new StandardAnalyzer(Version.LUCENE_35);
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        indexWriter = new IndexWriter(dir, conf);
        mgr = new SearcherManager(indexWriter, true, null, null);

        populateIndex();
    }
    
    @After
    public void shutdown() throws IOException {
        mgr.close();
        indexWriter.close();
        analyzer.close();
        dir.close();
    }

    @Test
    public void test() throws ParseException, IOException, InterruptedException {
        doSearching();
        deleteSome();
        Thread.sleep(1000);
        doSearching();
    }

    private void populateIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
        for (int i = 0; i < 10; i++) {
            Document doc = new Document();
            Field fId = new Field("id", "ID" + i, Field.Store.YES, Field.Index.NOT_ANALYZED);
            System.out.println("added field: " + fId);
            doc.add(fId);
            doc.add(new Field("text", "some test goes here", Field.Store.YES, Field.Index.ANALYZED));
            indexWriter.addDocument(doc);
        }
    }

    private void doSearching() throws ParseException, IOException {
        mgr.maybeReopen();
        IndexSearcher searcher = mgr.acquire();
        TopScoreDocCollector collector = TopScoreDocCollector.create(20, true);
        String[] fields = {"text"};
        MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, fields, analyzer); //, boosts);
        Query q = parser.parse("here");
        
        searcher.search(q, collector);
        TopDocs topDocs = collector.topDocs();
        System.out.println("searched: " + topDocs.totalHits);
        mgr.release(searcher);
    }

    private void deleteSome() {
        try {
            Term uidTerm = new Term("id", "ID1");
            indexWriter.deleteDocuments(uidTerm);
//            indexWriter.commit();
//            indexWriter.forceMergeDeletes();
        } catch (CorruptIndexException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
