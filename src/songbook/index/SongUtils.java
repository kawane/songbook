package songbook.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Created by LF5 on 06/04/2015.
 */
public class SongUtils {

	public static Pattern CHORD_REGEXP = Pattern.compile("^(C|D|E|F|G|A|B)(b|#)?(m|M|min|maj)?((sus)?(b|#)?(2|4|5|6|7|9|10|11|13)?)*(\\+|aug|alt)?(\\/(C|D|E|F|G|A|B))?$");

	public static Document indexSong(String songData) {
		Document document = new Document();
		String[] songLines = getSongLines(songData);
		document.add(new TextField("title", songLines[0], Field.Store.YES));
		for (String line: songLines) {
			String lineLC = line.toLowerCase();
			if (lineLC.startsWith("artist:")) {
				document.add(new TextField("artist", line.substring("artist:".length()), Field.Store.YES));
			} else if (lineLC.startsWith("album:")) {
				document.add(new TextField("album", line.substring("album:".length()), Field.Store.YES));
			}
		}
		return document;
	}

	public static String[] getSongLines(String songData) {
		return songData.replace("\r\n", "\n").replace("\r", "").split("\n");
	}

	public static void writeHtml(Appendable w, String songData) {
		try {
			String[] songLines = SongUtils.getSongLines(songData);
			w.append("<div class='song'>");
			w.append("<div class='song-title'>");
			w.append(songLines[0]);
			w.append("</div>");
			boolean verse = false;
			for (int i = 1; i < songLines.length; i++) {
				String line = songLines[i].trim();
				int indexOfCol = line.indexOf(":");
				if (line.isEmpty() && verse) {
					// close verse
					w.append("</div>");
					verse = false;
				}
				if (indexOfCol != -1) {
					String propName = line.substring(0, indexOfCol).toLowerCase().trim();
					String propValue = line.substring(indexOfCol + 1).trim();
					if (!propValue.isEmpty()) {
						if (verse) {
							// close verse
							w.append("</div>");
							verse = false;
						}
					}
					w.append("<div class='song-");
					w.append(propName);
					w.append("'>");
					w.append("<span class='song-metadata-name'>");
					w.append(propName);
					w.append(": </span>");
					if (!propValue.isEmpty()) {
						w.append("<span class='song-metadata-value'>");
						boolean isLink = propName.equals("video") || propName.equals("audio") || propName.equals("link");
						if (isLink) {
							w.append("<a href='");
							w.append(propValue);
							w.append("'>");
						}
						w.append(propValue);
						if (isLink) {
							w.append("</a>");
						}
						w.append("</span>");
						w.append("</div>");
					} else {
						// Start of verse don't close div
						verse = true;
					}

				} else {
					String[] tokens = line.split(" ");
					boolean isChord = true;
					for (int j = 0; isChord && j < tokens.length; j++) {
						if (!tokens[j].isEmpty()) {
							isChord = isChord && SongUtils.CHORD_REGEXP.matcher(tokens[j]).matches();
						}
					}
					if (line.isEmpty()) {
						w.append("<div class='song-line'> </div>");
					} else if (isChord) {
						w.append("<div class='song-chords'>");
						w.append(line);
						w.append("</div>");
					} else {
						w.append("<div class='song-line'>");
						w.append(line);
						w.append(" </div>");
					}
				}
			}
			if (verse) {
				// close verse
				w.append("</div>");
				verse = false;
			}
			w.append("</div>");
		} catch (IOException e) {
			System.err.println("An appendable must not failed here!");
		}
	}
}
