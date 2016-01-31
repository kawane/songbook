package songbook.song;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import songbook.server.Server;
import songbook.server.Templates;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * Index songs
 *
 * Created by laurent on 08/05/2014.
 */
public class IndexDatabase {

    private final Logger logger = Logger.getLogger("Songbook");

    private final SongDatabase songDb;

    private final IndexWriter indexWriter;

    private StandardAnalyzer analyzer;

    private Directory index;

    public IndexDatabase(Path indexFolder, SongDatabase songDb) throws IOException {
        this.songDb = songDb;

        analyzer = new StandardAnalyzer(Version.LUCENE_48);
        index = new NIOFSDirectory(indexFolder.toFile());
        indexWriter = new IndexWriter(index, new IndexWriterConfig(Version.LUCENE_48, analyzer));
        if (!DirectoryReader.indexExists(index)) {
            analyzeSongs();
        }
    }

    public void addOrUpdateDocument(Document document) throws IOException {
        indexWriter.updateDocument(new Term("id", document.get("id")), document);
        indexWriter.commit();
    }



    public String getTitle(String id) throws IOException {
        DirectoryReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        ScoreDoc[] scoreDocs = searcher.search(new TermQuery(new Term("id", id)), 1).scoreDocs;
        String title = null;
        if (scoreDocs.length > 0) {
            title = reader.document(scoreDocs[0].doc).get("title");
        }
        reader.close();
        return title;
    }

    public void removeDocument(String id) throws IOException {
        indexWriter.deleteDocuments(new Term("id", id));
        indexWriter.commit();
    }

    public void analyzeSongs() throws IOException {
        // clears index
        indexWriter.deleteAll();
        indexWriter.commit();

        songDb.listSongIds().forEach(
            (id) -> {
                String contents = songDb.getSongContents(id);
                if (contents != null) {
                    Document document = SongUtils.indexSong(contents);
                    document.add(new StringField("id", id, Field.Store.YES));
                    try {
                        indexWriter.addDocument(document);
                        indexWriter.commit();
                    } catch (IOException e) {
                        logger.log(Level.WARNING, "Can't index song '" + id + "'", e);
                    }
                }
            }
        );
        indexWriter.commit();

    }

    public void search(String querystr, Appendable out, String mimeType) throws ParseException, IOException {
        int hitsPerPage = 500;
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
            Query query = new QueryParser(Version.LUCENE_48, "song", analyzer).parse(querystr);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
            searcher.search(query, collector);
            hits = collector.topDocs().scoreDocs;
        }

        if (Server.MIME_TEXT_HTML.equals(mimeType)) {
            Templates.startSongItems(out);
        }
        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            Document doc = searcher.doc(docId);
            switch (mimeType) {
                case Server.MIME_TEXT_HTML:
                    String artists = Stream.of(doc.getValues("artist")).collect(Collectors.joining(", "));
                    Templates.songItem(out, doc.get("id"), doc.get("title"), artists);
                    break;
                case Server.MIME_TEXT_PLAIN:
                default:
                    out.append(doc.get("id")).append("\n");
                    break;
            }
        }
        if (Server.MIME_TEXT_HTML.equals(mimeType)) {
            Templates.endSongItems(out);
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
    }

}
