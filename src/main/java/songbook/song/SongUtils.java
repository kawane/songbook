package songbook.song;

import org.apache.lucene.document.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by LF5 on 06/04/2015.
 */
public class SongUtils {

	public static String  CHORD_REGEXP_STR = "(C|D|E|F|G|A|B)(b|#)?(m|M|min|maj|dim|Δ|°|ø|Ø)?((sus|add)?(b|#)?(2|4|5|6|7|9|10|11|13)?)*(\\+|aug|alt)?(\\/(C|D|E|F|G|A|B)(b|#)?)?";

	public static Pattern CHORD_REGEXP = Pattern.compile(CHORD_REGEXP_STR);

	public static Pattern REPEAT_REGEXP = Pattern.compile("\\(x[0-9]+\\)");

	public static String getTitle(String songData) {
		int indexOfFirstLine = songData.replace("\r\n", "\n").replace("\r", "\n").indexOf("\n");
		if (indexOfFirstLine != -1) {
			return songData.substring(0, indexOfFirstLine);
		}
		return songData;
	}

	public static Document indexSong(String songData) {
		Document document = new Document();
		String[] songLines = getSongLines(songData);
		document.add(new TextField("song", songData, Field.Store.NO));

        document.add(new SortedDocValuesField("title", new BytesRef(songLines[0])));
        document.add(new StringField("title", songLines[0], Field.Store.YES));

        for (int i = 1; i < songLines.length; i++) {
			String line = songLines[i].trim();
            int indexOfCol = line.indexOf(":");

			if (indexOfCol != -1) {
				String propName = line.substring(0, indexOfCol).toLowerCase().trim();
				String propValue = line.substring(indexOfCol + 1).trim();
				if (!propValue.isEmpty()) {

                    document.add(new StringField(propName, propValue, Field.Store.YES));
				}
			}
		}
		return document;
	}

	public static String[] getSongLines(String songData) {
		return songData.replace("\r\n", "\n").replace("\r", "\n").split("\n");
	}

	public static <A extends Appendable> A writeHtml(A w, String songData) {
		try {
			String[] songLines = SongUtils.getSongLines(songData);
			w.append("<div class='song' itemscope='' itemtype='http://schema.org/MusicComposition'>\n");

			w.append("<div class='song-title' itemprop='name'>");
			w.append(escape(songLines[0]));
			w.append("</div>\n");
            w.append("<div class='song-header'>");
            boolean songHeader = true;
			boolean verse = false;
			for (int i = 1; i < songLines.length; i++) {
				String line = songLines[i];
				String lowercaseLine = line.trim().toLowerCase();
				int indexOfCol = line.indexOf(":");

				if (indexOfCol != -1) {
					String propName = line.substring(0, indexOfCol).toLowerCase().trim();
					String propValue = line.substring(indexOfCol + 1).trim();
					if (!propValue.isEmpty()) {
						if (verse) {
							// close verse
							w.append("</div>\n");
							verse = false;
						}
					} else {
                        if (songHeader) {
                            songHeader = false;
                            w.append("</div>\n<div class='song-content'>\n");
                        }
                    }
					w.append("<div class='song-");
					w.append(escape(propName.replace(" ", "-")));
					w.append("'>\n");
					w.append("<span class='song-metadata-name'>");
					w.append(escape(propName));
					w.append(": </span>\n");
					if (!propValue.isEmpty()) {
						w.append("<span class='song-metadata-value'");
						switch (propName) {
							case "author", "artist" -> w.append(" itemprop='composer'");
							case "album" -> w.append(" itemprop='inAlbum'");
							case "tone", "key" -> w.append(" itemprop='musicalKey'");
							default -> { /* no schema.org mapping for this property */ }
						}

						w.append("data-name='");
						w.append(escape(propName));
						w.append("'>");
						boolean isLink = propName.equals("video") || propName.equals("audio") || propName.equals("link");
						boolean safeLink = isLink && isSafeHref(propValue);
						if (safeLink) {
							w.append("<a href='");
							w.append(escape(propValue));
							w.append("'>");
						}
						w.append(escape(propValue));
						if (safeLink) {
							w.append("</a>");
						}
						w.append("</span>\n");
						w.append("</div>\n");
					} else {
						// Start of verse don't close div
						verse = true;
					}

				} else if (lowercaseLine.isEmpty()) {
					if (verse) {
						// close verse
						w.append("</div>\n");
						verse = false;
					}
				} else if ("chorus".equals(lowercaseLine.toLowerCase())
						|| "intro".equals(lowercaseLine.toLowerCase())
						|| "bridge".equals(lowercaseLine.toLowerCase())) { // chorus, intro, or bridge recall
					if (verse) {
						// close verse
						w.append("</div>\n");
						verse = false;
					}
					w.append("<div class='song-" + lowercaseLine +"-recall'>");
					w.append(escape(line));
					w.append("</div>");
				} else {
					String[] tokens = line.replace("|", " ").split(" ");
					boolean isLineChords = true;
					for (int j = 0; isLineChords && j < tokens.length; j++) {
						if (!tokens[j].isEmpty()) {
							isLineChords = isLineChords && (CHORD_REGEXP.matcher(tokens[j]).matches() || REPEAT_REGEXP.matcher(tokens[j]).matches());
						}
					}
					if (isLineChords) {
						if (!verse) {
                            if (songHeader) {
                                songHeader = false;
                                w.append("</div>\n<div class='song-content'>\n");
                            }
							w.append("<div class='song-verse'>");
							verse = true;
						}
						w.append("<div class='song-chords'>");
						// escape first (chords are ASCII and still match afterwards),
						// then wrap each chord in a span
						w.append(CHORD_REGEXP.matcher(escape(line)).replaceAll("<span class='song-chord'>$0</span>"));
						w.append("</div>\n");
					} else {
						if (!verse) {
                            if (songHeader) {
                                songHeader = false;
                                w.append("</div>\n<div class='song-content'>\n");
                            }
							w.append("<div class='song-verse'>");
							verse = true;
						}
						w.append("<div class='song-line'>");
						w.append(escape(line));
						w.append(" </div>\n");
					}
				}

			}
			if (verse) {
				// close verse
				w.append("</div>\n");
				verse = false;
			}
			w.append("</div>\n</div>\n");
		} catch (IOException e) {
			System.err.println("An appendable must not failed here!");
		}
		return w;
	}

	/** Escapes text so song content can't inject HTML (stored XSS / broken markup). */
	static String escape(String s) {
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '&' -> sb.append("&amp;");
				case '<' -> sb.append("&lt;");
				case '>' -> sb.append("&gt;");
				case '"' -> sb.append("&quot;");
				case '\'' -> sb.append("&#39;");
				default -> sb.append(c);
			}
		}
		return sb.toString();
	}

	/** Only allow link schemes safe to click; anything else is rendered as plain text. */
	static boolean isSafeHref(String url) {
		String u = url.trim().toLowerCase();
		return u.startsWith("http://") || u.startsWith("https://")
				|| u.startsWith("mailto:") || u.startsWith("/") || u.startsWith("#");
	}

}
