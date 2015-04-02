package songbook.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

/**
 * Is used by HtmlIndexer to configure the mapping between the index name and the css classname that comes from the html
 */
public class IndexEntityType {

    public final String name;

    public final String className;

    public final boolean store;

    public IndexEntityType(String name, String className, boolean store) {
        this.name = name;
        this.className = className;
        this.store = store;
    }

    public void addEntity(Document document, String text) {
        document.add(new TextField(name, text, store ? Field.Store.YES : Field.Store.NO));
    }
}
