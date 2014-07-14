package songbook.server;

import org.apache.lucene.document.Document;
import org.intellij.lang.annotations.Language;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
                        "        <ul class='nav navbar-nav'>\n" +
                        "            <!--<li><a class='' href='/rest/song/'>View</a></li>-->\n" +
                        "        </ul>\n" +
                        "\n" +
                        "        <ul class='nav navbar-right'>\n" +
                        "            <!--<li><a href='{{.LoginURL}}'>Sign in</a></li>-->\n" +
                        "        </ul>\n" +
                        "        <ul class='nav navbar-right'>\n" +
                        "            <a href=\"http://www.jetbrains.com/idea/features/javascript.html\" style=\"display:block; background:#fff url(http://www.jetbrains.com/idea/opensource/img/all/banners/idea210x60_white.gif) no-repeat 0 0; border:solid 1px #0d3a9e; margin:0;padding:0;text-decoration:none;text-indent:0;letter-spacing:-0.001em; width:208px; height:58px\" alt=\"Java IDE with advanced HTML/CSS/JS editor for hardcore web-developers\" title=\"Java IDE with advanced HTML/CSS/JS editor for hardcore web-developers\"><span style=\"margin: -3px 0 0 41px;padding: 0;float: left;font-size: 10px;cursor:pointer;  background-image:none;border:0;color: #0d3a9e; font-family: trebuchet ms,arial,sans-serif;font-weight: normal;text-align:left;\">Developed with</span><span style=\"margin:33px 0 0 5px;padding:0 0 2px 0; line-height:11px;font-size:9px;word-spacing:-2;cursor:pointer;  background-image:none;border:0;display:block;width:210px; color:#0d3a9e; font-family:tahoma,arial,sans-serif;font-weight: normal;text-align:left;\">Java IDE with advanced HTML/CSS/JS<br/>editor for hardcore web-developers</span></a>\n" +
                        "        </ul>\n" +
                        "        <ul class='nav navbar-right'>\n" +
                        "            <li><a href='https://github.com/llgcode/songbook'>Participate in Development</a></li>\n" +
                        "        </ul>\n" +
                        "        <form onSubmit='return songbook.search(\""+ key +"\", this[\"query\"].value)' class='navbar-form navbar-left' >\n" +
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
    public static String getFooter(String key, String functionToCall) {
        return
                "<!-- IntelliJ banner -->\n" +
                "<div id='intellij-banner'><a href='http://www.jetbrains.com/idea/features/javascript.html' style='display:block; background:#fff url(http://www.jetbrains.com/idea/opensource/img/all/banners/idea468x60_white.gif) no-repeat 0 7px; border:solid 1px #0d3a9e; margin:0;padding:0;text-decoration:none;text-indent:0;letter-spacing:-0.001em; width:466px; height:58px' alt='Java IDE with advanced HTML/CSS/JS editor for hardcore web-developers' title='Java IDE with advanced HTML/CSS/JS editor for hardcore web-developers'><span style='margin: 5px 0 0 61px;padding: 0;float: left;font-size: 12px;cursor:pointer;  background-image:none;border:0;color: #0d3a9e; font-family: trebuchet ms,arial,sans-serif;font-weight: normal;text-align:left;'>Developed with</span><span style='margin:0 0 0 205px;padding:18px 0 2px 0; line-height:13px;font-size:11px;cursor:pointer;  background-image:none;border:0;display:block; width:255px; color:#0d3a9e; font-family: trebuchet ms,arial,sans-serif;font-weight: normal;text-align:left;'>Java IDE with advanced HTML/CSS/JS<br/>editor for hardcore web-developers</span></a></div>\n" +
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
        return  "<a class='list-group-item' href='"+ internalLink(key, "/songs/"+ encodeUrl(id)) +"'>\n" +
                "<h4 class='list-group-item-heading'>" + (title ==null ? id : title) + "</h4>\n" +
                "<p class='list-group-item-text'>" +
                Stream.of(document.getValues("author")).collect(Collectors.joining(", "))+
                "</p>" +
                "</a>\n";

    }

    private static String internalLink(String key, String link) {
        return link + "?key="+key;
    }

    private static String encodeUrl(String id) {
        try {
            return URLEncoder.encode(id, "utf-8");
        } catch (UnsupportedEncodingException e) {
            // Do nothing but logging
            e.printStackTrace();
        }
        return id;
    }

}
