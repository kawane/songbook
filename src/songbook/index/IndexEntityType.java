package songbook.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

/**
* Created by laurent on 16/06/2014.
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
