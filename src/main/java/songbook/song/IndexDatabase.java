package songbook.song;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.BytesRef;

import songbook.server.Server;
import songbook.server.Templates;

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

        analyzer = new StandardAnalyzer();
        index = new NIOFSDirectory(indexFolder);
        indexWriter = new IndexWriter(index, new IndexWriterConfig(analyzer));
        if (!DirectoryReader.indexExists(index)) {
            analyzeSongs();
        }
    }

    public void addOrUpdateDocument(Document document) throws IOException {
        indexWriter.updateDocument(new Term("id", document.get("id")), document);
        indexWriter.commit();
    }

    /** Returns the title of a song */
    public String getTitle(String id) throws IOException {
        DirectoryReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        var storeFields = reader.storedFields();
        ScoreDoc[] scoreDocs = searcher.search(new TermQuery(new Term("id", id)), 1).scoreDocs;
        String title = null;
        if (scoreDocs.length > 0) {
            title = storeFields.document(scoreDocs[0].doc).get("title");
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
                });
        indexWriter.commit();

    }

    public void listArtists(Appendable out, String mimeType) throws IOException, ParseException {
        var reader = DirectoryReader.open(index);

        Terms artists = MultiTerms.getTerms(reader, "artist");
        TermsEnum termsEnum = artists.iterator();
        BytesRef term;
        if (Server.MIME_TEXT_HTML.equals(mimeType)) {
            Templates.startItems(out);
        }
        while ((term = termsEnum.next()) != null) {
            String artist = term.utf8ToString();
            switch (mimeType) {
                case Server.MIME_TEXT_HTML:
                    Templates.artistItem(out, artist, termsEnum.docFreq());
                    break;
                case Server.MIME_TEXT_PLAIN:
                default:
                    out.append(artist).append(": ").append(Integer.toString(termsEnum.docFreq()));
                    break;
            }
        }
        if (Server.MIME_TEXT_HTML.equals(mimeType)) {
            Templates.endItems(out);
        }
    }

    public void search(String querystr, Appendable out, String mimeType) throws ParseException, IOException {
        int hitsPerPage = 500;
        int totalHitsThreshold = 500;
        var reader = DirectoryReader.open(index);
        var searcher = new IndexSearcher(reader);
        var storeFields = reader.storedFields();

        ScoreDoc[] hits;
        if (querystr == null || querystr.isEmpty()) {
            Query query = new MatchAllDocsQuery();
            TopFieldDocs topFieldDocs = searcher.search(query, hitsPerPage,
                    new Sort(new SortField("title", Type.STRING)));
            hits = topFieldDocs.scoreDocs;
        } else {
            // the "song" arg specifies the default field to use
            // when no field is explicitly specified in the query.
            Query query = new QueryParser("song", analyzer).parse(querystr);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, totalHitsThreshold);
            searcher.search(query, collector);
            hits = collector.topDocs().scoreDocs;
        }

        if (Server.MIME_TEXT_HTML.equals(mimeType)) {
            Templates.startItems(out);
        }

        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            Document doc = storeFields.document(docId);
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
            Templates.endItems(out);
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
    }

    public void songsByArtist(String artist, Appendable out, String mimeType) throws ParseException, IOException {
        int hitsPerPage = 500;
        int totalHitsThreshold = 500;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);

        ScoreDoc[] hits;

        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, totalHitsThreshold);
        Query tq = new TermQuery(new Term("artist", artist));
        searcher.search(tq, collector);
        hits = collector.topDocs().scoreDocs;

        if (Server.MIME_TEXT_HTML.equals(mimeType)) {
            Templates.startItems(out);
        }
        for (ScoreDoc hit : hits) {
            int docId = hit.doc;
            Document doc = reader.storedFields().document(docId);
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
            Templates.endItems(out);
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
    }

}
