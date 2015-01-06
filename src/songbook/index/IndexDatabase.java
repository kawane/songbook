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
import org.apache.lucene.store.NIOFSDirectory;
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

    private final Path songsFolder;

    private StandardAnalyzer analyzer;

    private Directory index;

    public IndexDatabase(Path indexFolder, Path songsFolder) throws IOException {
        this.songsFolder = songsFolder;

        analyzer = new StandardAnalyzer(Version.LUCENE_48);
        index = new NIOFSDirectory(indexFolder.toFile());
        if (DirectoryReader.indexExists(index) == false) {
            // index is empty, analyze songs
            analyzeSongs();
        }
    }

    public void addOrUpdateDocument(Document document) throws IOException {
        Term term = new Term("id", document.get("id"));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        IndexWriter w = new IndexWriter(index, config);
        w.updateDocument(term, document);
        w.close();
    }

    public void removeDocument(String id) throws IOException {
        Term term = new Term("id", id);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        IndexWriter w = new IndexWriter(index, config);
        w.deleteDocuments(term);
        w.close();
    }

    private int analyzeSongs() throws IOException {
        int[] count = { 0 };
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        IndexWriter w = new IndexWriter(index, config);
        HtmlIndexer songIndexer = new HtmlIndexer();
        Files.walk(songsFolder).forEach(filePath -> {
            if (Files.isRegularFile(filePath) && filePath.toString().endsWith(".html")) {
                try {
                    Document document = songIndexer.indexSong(filePath);
                    document.add(new StringField("id", SongUtil.getIdFromTitle(filePath.getFileName().toString()), Field.Store.YES));
                    w.addDocument(document);

                    count[0] += 1;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        w.close();
        return count[0];
    }


    public void search(String key, String querystr, HttpServerRequest request) throws ParseException, IOException {
        HttpServerResponse response = request.response();
        response.setChunked(true);

        int hitsPerPage = 50;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        ScoreDoc[] hits;
        if (querystr == null || querystr.isEmpty()) {
            Query query = new MatchAllDocsQuery();
            TopFieldDocs topFieldDocs = searcher.search(query, hitsPerPage, new Sort(new SortField("title", Type.STRING)));
            hits = topFieldDocs.scoreDocs;
        } else {
            // the "lyrics" arg specifies the default field to use
            // when no field is explicitly specified in the query.
            Query query = new QueryParser(Version.LUCENE_48, "lyrics", analyzer).parse(querystr);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(query, collector);
            hits = collector.topDocs().scoreDocs;
        }

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
