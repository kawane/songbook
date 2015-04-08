package songbook.server;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by j5r on 01/05/2014.
 */
public class Templates {

    private final static Map<String, TemplateCache> cache = new HashMap<>();

    private static Path TEMPLATES_PATH = Paths.get("web/templates");

    public static void setTemplatesPath(Path templatesPath) {
        if (cache != null) {
            cache.clear();
        }
        TEMPLATES_PATH = templatesPath;
    }

    public static String header(String title) {
        StringBuilder out = new StringBuilder();
        print(out, "header.html", "title", title);
        return out.toString();
	}

    public static String footer() {
        StringBuilder out = new StringBuilder();
        print(out, "footer.html");
        return out.toString();
	}

    public static String startSongItems() {
        StringBuilder out = new StringBuilder();
        print(out, "startSongItems.html");
        return out.toString();
	}

    public static String songItem(String songId, String songTitle, String songArtist) {
        StringBuilder out = new StringBuilder();
        print(out, "songItem.html", "songId", songId, "songTitle", songTitle, "songArtist", songArtist);
        return out.toString();
	}

    public static String endSongItems() {
        StringBuilder out = new StringBuilder();
        print(out, "endSongItems.html");
        return out.toString();
	}

    public static String alertSongDoesNotExist(String songId) {
        StringBuilder out = new StringBuilder();
        print(out, "alert/songDoesNotExist.html", "songId", songId);
        return out.toString();
	}

    public static String alertKeyCreation(String adminSessionKey, String path) {
        StringBuilder out = new StringBuilder();
        print(out, "alert/keyCreation.html", "adminSessionKey", adminSessionKey, "path", path);
        return out.toString();
	}

    public static String alertSongReindexed() {
        StringBuilder out = new StringBuilder();
        print(out, "alert/songReindexed.html");
        return out.toString();
	}

    public static String alertAccessForbidden(String path) {
        StringBuilder out = new StringBuilder();
        print(out, "alert/accessForbidden.html", "path", path);
        return out.toString();
    }

    public static String alertIndexingError() {
        StringBuilder out = new StringBuilder();
        print(out, "alert/indexingError.html");
        return out.toString();
    }

    public static String alertCommandNotSupported() {
        StringBuilder out = new StringBuilder();
        print(out, "alert/commandNotSupported.html");
        return out.toString();
    }

    protected static void print(Appendable out, String templateName, String... vars) {
        try {
            String content = getContent(templateName);
            for (int i = 0; i < vars.length; i += 2) {
                content = content.replace("${" + vars[i] + "}", vars[i+1]);
            }
            out.append(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class TemplateCache {
        long time;
        String content;

        public TemplateCache(long time, String content) {
            this.time = time;
            this.content = content;
        }
    }

    @NotNull
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
