package songbook.index;

import org.apache.lucene.document.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This class is not thread safe you may use multiple instance for multi-thread purpose
 * Created by laurent on 16/06/2014.
 */
public class HtmlIndexer {

    public final static IndexEntityType[] INDEX_ENTITY_TYPES = {
            new IndexEntityType("lyrics", "song", false),
            new IndexEntityType("title", "song-title", true),
            new IndexEntityType("author", "song-author", true),
            new IndexEntityType("album", "song-album", true),
    };

    protected final IndexEntityType[] indexEntityTypes;

    public HtmlIndexer() {
        this(INDEX_ENTITY_TYPES);
    }

    public HtmlIndexer(IndexEntityType[] indexEntityTypes) {
        this.indexEntityTypes = indexEntityTypes;
    }

    public Document indexSong(Path songPath) throws IOException {
        return indexHtmlDocument(Jsoup.parse(songPath.toFile(), "UTF-8"));
    }

    public Document indexSong(String songData) throws IOException {
        return indexHtmlDocument(Jsoup.parse(songData));
    }

    private Document indexHtmlDocument(org.jsoup.nodes.Document htmlDocument) {
        Document luceneDocument = new Document();
        for (IndexEntityType type : INDEX_ENTITY_TYPES) {
            Elements elementsByClass = htmlDocument.getElementsByClass(type.className);
            for (Element element : elementsByClass) {
                type.addEntity(luceneDocument, element.text());
            }
        }
        return luceneDocument;
    }
}
