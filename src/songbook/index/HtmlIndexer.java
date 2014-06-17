package songbook.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is not thread safe you may use multiple instance for multi-thread purpose
 * Created by laurent on 16/06/2014.
 */
public class HtmlIndexer extends DefaultHandler {

    /** Create a SaxParser factory */
    protected final SAXParserFactory factory = SAXParserFactory.newInstance();

    protected final IndexEntityType[] indexEntityTypes;

    protected StringBuilder text = new StringBuilder();

    protected List<Entity> stack = new ArrayList<>();

    protected Document document;

    public HtmlIndexer(IndexEntityType[] indexEntityTypes) {
        this.indexEntityTypes = indexEntityTypes;
    }

    public void indexSong(IndexWriter indexWriter, Path songPath) throws ParserConfigurationException, SAXException, IOException {
        this.document = new Document();

        SAXParser parser = factory.newSAXParser();

        InputStream in = Files.newInputStream(songPath);
        try {
            parser.parse(in, this);
        } finally {
            in.close();
            indexWriter.addDocument(document);
        }
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        String classNameStr = attrs.getValue("class");
        String[] classNames = new String[0];
        if (classNameStr != null) {
            classNames = classNameStr.split(" ");
        }
        stack.add(new Entity(classNames, text.length()));
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        text.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Entity entity = stack.remove(stack.size() - 1);
        for (String className : entity.classNames) {
            Arrays.stream(indexEntityTypes)
                    .filter((type) -> type.className.equals(className))
                    .forEach((type) -> type.addEntity(document, text.substring(entity.textOffset)));
        }
    }


    protected static class Entity {

        protected final String[] classNames;

        protected final int textOffset;

        protected Entity(String[] classNames, int textOffset) {
            this.classNames = classNames;
            this.textOffset = textOffset;
        }
    }

}
