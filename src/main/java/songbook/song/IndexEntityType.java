package songbook.song;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

/**
 * Is used by HtmlIndexer to configure the mapping between the index name and the css classname that comes from the html
 */
public record IndexEntityType(String name, String className, boolean store) {

    public void addEntity(Document document, String text) {
        document.add(new TextField(name, text, store ? Field.Store.YES : Field.Store.NO));
    }
}
