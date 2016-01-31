package songbook.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains functions that generate a string from a template
 * Created by j5r on 01/05/2014.
 */
public class Templates {

    private final static Map<String, TemplateCache> cache = new HashMap<>();

    private static Path TEMPLATES_PATH = Paths.get("web/templates");

    public static void setTemplatesPath(Path templatesPath) {
        cache.clear();
        TEMPLATES_PATH = templatesPath;
    }

    public static <A extends Appendable> A header(A out, CharSequence title, CharSequence role) {
        return print(out, "header.html", "title", title, "role", role);
	}

    public static <A extends Appendable> A footer(A out) {
        return print(out, "footer.html");
	}

    public static <A extends Appendable> A search(A out, CharSequence searchResult, CharSequence role) {
        return print(out, "search.html", "searchResult", searchResult, "role", role);
    }

    public static <A extends Appendable> A editSong(A out, CharSequence songId, CharSequence song, CharSequence role) {
        return print(out, "editSong.html", "songId", songId, "song", song, "role", role);
    }

    public static <A extends Appendable> A viewSong(A out, CharSequence songId, CharSequence song, CharSequence role) {
        return print(out, "viewSong.html", "songId", songId, "song", song, "role", role);
    }

    public static <A extends Appendable> A newSong(A out) {
        return print(out, "newSong.song");
    }

    public static <A extends Appendable> A startItems(A out) {
        return print(out, "startItems.html");
	}

    public static <A extends Appendable> A songItem(A out, CharSequence songId, CharSequence songTitle, CharSequence songArtist) {
        return print(out, "songItem.html", "songId", songId, "songTitle", songTitle, "songArtist", songArtist);
	}

    public static <A extends Appendable> A artistItem(A out, CharSequence artist, int songCount) {
        return print(out, "artistItem.html", "artist", artist, "songCount", Integer.toString(songCount));
    }

    public static <A extends Appendable> A endItems(A out) {
        return print(out, "endItems.html");
	}


    public static <A extends Appendable> A signin(A out) {
        return print(out, "signin.html");
    }

    public static <A extends Appendable> A admin(A out) {
        return print(out, "admin.html");
    }

    public static <A extends Appendable> A consoleApi(A out) {
        return print(out, "consoleApi.html");
    }

    public static <A extends Appendable> A alertSongDoesNotExist(A out, CharSequence songId) {
        return print(out, "alerts/songDoesNotExist.html", "songId", songId);
	}

    public static <A extends Appendable> A alertMissingArguments(A out, CharSequence arguments) {
        return print(out, "alerts/songDoesNotExist.html", "arguments", arguments);
	}

    public static <A extends Appendable> A alertKeyCreation(A out, CharSequence adminSessionKey, CharSequence path) {
        return print(out, "alerts/keyCreation.html", "adminSessionKey", adminSessionKey, "path", path);
	}

    public static <A extends Appendable> A alertSongReindexed(A out) {
        return print(out, "alerts/songReindexed.html");
	}

    public static <A extends Appendable> A alertAccessForbidden(A out, CharSequence path) {
        return print(out, "alerts/accessForbidden.html", "path", path);
    }

    public static <A extends Appendable> A alertIndexingError(A out) {
        return print(out, "alerts/indexingError.html");
    }

    public static <A extends Appendable> A alertCommandNotSupported(A out) {
        return print(out, "alerts/commandNotSupported.html");
    }

    public static <A extends Appendable> A alertSongRemovedSuccessfully(A out, CharSequence songTitle) {
        return print(out, "alerts/songRemovedSuccessfully.html", "songTitle", songTitle);
    }

    protected static <A extends Appendable> A print(A out, String templateName, CharSequence... vars) {
        try {
            String content = getContent(templateName);
            for (int i = 0; i < vars.length; i += 2) {
                CharSequence value = vars[i+1];
                if (value == null) {
                    value = "";
                }
                content = content.replace("${" + vars[i] + "}", value);
            }
            out.append(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    private static class TemplateCache {
        long time;
        String content;

        public TemplateCache(long time, String content) {
            this.time = time;
            this.content = content;
        }
    }

    private static String getContent(String templateName) throws IOException {
        TemplateCache templateCache = cache.get(templateName);
        Path templatePath = TEMPLATES_PATH.resolve(templateName);
        boolean needLoading;
        long lastModified = -1;
        if (templateCache == null) {
            needLoading = true;
        } else {
            lastModified = Files.getLastModifiedTime(templatePath).toMillis();
            needLoading = lastModified > templateCache.time;
        }
        if (needLoading) {
            cache.remove(templateName);
            templateCache = new TemplateCache(lastModified, new String(Files.readAllBytes(templatePath), StandardCharsets.UTF_8));
            cache.put(templateName, templateCache);
        }
        return templateCache.content;
    }
}
