package songbook.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.nio.file.Path;

/**
 * Created by LF5 on 06/04/2015.
 */
public class SongIndexer {

	public Document indexSong(String songData) {
		Document document = new Document();
		String[] songLines = songData.split("\n");
		document.add(new TextField("title", songLines[0], Field.Store.YES));
		for (String line: songLines) {
			String lineLC = line.toLowerCase();
			if (lineLC.startsWith("artist:")) {
				document.add(new TextField("artist", songLines[0], Field.Store.YES));
			} else if (lineLC.startsWith("album:")) {
				document.add(new TextField("album", songLines[0], Field.Store.YES));
			}
		}
		return document;
	}
}
