package songbook.server;

import org.apache.lucene.document.Document;
import org.intellij.lang.annotations.Language;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by j5r on 01/05/2014.
 */
public class Templates {

    @Language("HTML")
    public static String getHeader(String key, String title) {
        return
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>"+ title +"</title>\n" +
                "    <meta charset='utf-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <!-- Bootstrap -->\n" +
                "    <!-- Latest compiled and minified CSS -->\n" +
                "    <link rel='stylesheet' href='//netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css'>\n" +
                "    <link href='"+ internalLink(key, "/css/main.css") +"' rel='stylesheet' media='screen'>\n" +
                "    <link href='"+ internalLink(key, "/css/song.css") +"' rel='stylesheet' media='screen'>\n" +
                "</head>\n" +
                "<body>\n"
                ;
    }

    @Language("HTML")
    public static String getNavigation(String key) {
        return
                "<nav class='navbar navbar-default' role='navigation'>\n" +
                        "    <div class='navbar-header'>\n" +
                        "        <button type='button' class='navbar-toggle' data-toggle='collapse' data-target='#bs-navbar-collapse'>\n" +
                        "            <span class='sr-only'>Toggle navigation</span>\n" +
                        "            <span class='icon-bar'></span>\n" +
                        "            <span class='icon-bar'></span>\n" +
                        "            <span class='icon-bar'></span>\n" +
                        "        </button>\n" +
                        "        <a class='navbar-brand' href='"+ internalLink(key, "/") +"'>My SongBook</a>\n" +
                        "    </div>\n" +
                        "    <!-- Collect the nav links, forms, and other content for toggling -->\n" +
                        "    <div class='collapse navbar-collapse' id='bs-navbar-collapse'>\n" +
                        "        <ul class='nav navbar-nav' id='tools'>\n" +
                        "        </ul>\n" +
                        "\n" +
                        "        <form id='search' onSubmit='return songbook.search(this[\"query\"].value)' class='navbar-form navbar-left' >\n" +
                        "          <div class='form-group'>\n" +
                        "            <input id='query' type='text' class='form-control' placeholder='Search'>\n" +
                        "          </div>\n" +
                        "          <button type='submit' class='btn btn-default'>Submit</button>\n" +
                        "        </form>\n" +
                        "\n" +
                        "    </div>\n" +
                        "</nav>\n"
                ;
    }


    @Language("HTML")
    public static String getKeyCreationAlert(String newKey, String path) {
        return "<div class=\"alert alert-success\" role=\"alert\">"+
                    "New administration key created. Remember to <a href=\""+ internalLink(newKey, path) +"\"> bookmark this link</a>." +
                    "This alert will disappear when the administration key will be used the first time." +
                "</div>\n";
    }

    @Language("HTML")
    public static String getFooter(String key, String functionToCall) {
        return
                "<!-- JavaScript plugins (requires jQuery) -->\n" +
                "<script src='http://code.jquery.com/jquery.js'></script>\n" +
                "<!-- Include all compiled plugins (below), or include individual files as needed -->\n" +
                "<script src='//netdna.bootstrapcdn.com/bootstrap/3.0.3/js/bootstrap.min.js'></script>\n" +
                "<script src='"+ internalLink(key, "/js/songbook.js") +"'></script>\n" +
                (functionToCall == null ? "" :
                    "<script type='text/javascript'>\n"+
                    "   " + functionToCall +"\n"+
                    "</script>\n" ) +
                "</body>\n" +
                "</html>\n"
                ;
    }

    @Language("HTML")
    public static String startResult() {
        return  "<div id='content' class='row'>\n" +
                "<div id='song-list' class='list-group'>\n";
    }

    @Language("HTML")
    public static String endResult() {
        return  "</div>\n</div>";
    }


    @Language("HTML")
    public static String showDocument(String key, Document document) {
        String id = document.get("id");
        String title = document.get("title");
        return  "<a class='list-group-item' href='"+ internalLink(key, "/songs/"+ id) +"'>\n" +
                "<h4 class='list-group-item-heading'>" + (title ==null ? id : title) + "</h4>\n" +
                "<p class='list-group-item-text'>" +
                Stream.of(document.getValues("author")).collect(Collectors.joining(", "))+
                "</p>" +
                "</a>\n";

    }

    @Language("HTML")
    public static String showAdminKey(String key) {
        return  "" +
                "" +
                "";
    }

    private static String internalLink(String key, String link) {
        if (key==null || key.length()==0) {
            return link;
        } else {
            return link + "?key=" + key;
        }
    }
}
