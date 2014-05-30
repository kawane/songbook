package songbook.index;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by laurent on 08/05/2014.
 */
public class IndexDatabase {

    public static void main(String[] args) throws IOException, ParseException {
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);

        // 1. create the index
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);

        IndexWriter w = new IndexWriter(index, config);
        Files.walk(Paths.get("data/songs/")).forEach(filePath -> {
            if (Files.isRegularFile(filePath) && filePath.toString().endsWith(".cho")) {
                try {
                    addSong(w, filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        w.close();

        // 2. query
        String querystr = args.length > 0 ? args[0] : "feeling";

        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        Query q = new QueryParser(Version.LUCENE_48, "lyrics", analyzer).parse(querystr);

        // 3. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("title"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
    }

    private static void addSong(IndexWriter w, Path songPath) throws IOException {
        BufferedReader reader = Files.newBufferedReader(songPath, Charset.forName("UTF-8"));
        try {
            Song song = new SongParser().parse(songPath.getFileName().toString(), reader);
            Document doc = new Document();
            song.directives.forEach(dir -> {
                if (dir.data.value != null) {
                    doc.add(new TextField(dir.data.name, dir.data.value, Field.Store.YES));
                }
            });

            doc.add(new TextField("lyrics", song.lyrics, Field.Store.NO));
            w.addDocument(doc);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }

    }
}
