package songbook.index;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import songbook.server.Templates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by laurent on 08/05/2014.
 */
public class IndexDatabase {

    public final static String SONG_EXTENSION = ".html";

    private StandardAnalyzer analyzer;

    private Directory index;

    public IndexDatabase(Path indexFolder) throws IOException {
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        analyzer = new StandardAnalyzer(Version.LUCENE_48);

        // 1. create the index
        index = new RAMDirectory();
    }

    public void addOrUpdateDocument(Document document) throws IOException, ParseException {
        Term term = new Term("id", document.get("id"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        IndexWriter w = new IndexWriter(index, config);
        w.updateDocument(term, document);
        //w.addDocument(document);
        w.close();
    }

    public void analyzeSongs(Path songsFolder) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        IndexWriter w = new IndexWriter(index, config);
        HtmlIndexer songIndexer = new HtmlIndexer();
        Files.walk(songsFolder).forEach(filePath -> {
            if (Files.isRegularFile(filePath) && filePath.toString().endsWith(".html")) {
                try {
                    Document document = songIndexer.indexSong(filePath);
                    document.add(new StringField("id", SongUtil.getId(filePath.getFileName().toString()), Field.Store.YES));
                    w.addDocument(document);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        w.close();
    }


    public void search(String key, String querystr, HttpServerRequest request) throws ParseException, IOException {
        HttpServerResponse response = request.response();
        response.setChunked(true);


        // 3. search
        int hitsPerPage = 50;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        ScoreDoc[] hits;
        if (querystr == null || querystr.isEmpty()) {
            Query query = new MatchAllDocsQuery();
            TopFieldDocs topFieldDocs = searcher.search(query, hitsPerPage, new Sort(new SortField("id", Type.STRING)));
            hits = topFieldDocs.scoreDocs;
        } else {
            // the "lyrics" arg specifies the default field to use
            // when no field is explicitly specified in the query.
            Query query = new QueryParser(Version.LUCENE_48, "lyrics", analyzer).parse(querystr);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(query, collector);
            hits = collector.topDocs().scoreDocs;
        }

        // 4. display results
        response.write(Templates.startResult());
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            response.write(Templates.showDocument(key, d));
        }
        response.write(Templates.endResult());

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
    }

}
