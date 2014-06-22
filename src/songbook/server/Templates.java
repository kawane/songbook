package songbook.server;

import org.apache.lucene.document.Document;
import org.intellij.lang.annotations.Language;
import songbook.index.Song;
import songbook.index.SongIndex;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by j5r on 01/05/2014.
 */
public class Templates {

    @Language("HTML")
    public static String getHeader(String title) {
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
                "    <link href='/css/main.css' rel='stylesheet' media='screen'>\n" +
                "    <link href='/css/song.css' rel='stylesheet' media='screen'>\n" +
                "</head>\n" +
                "<body>\n"
                ;
    }

    @Language("HTML")
    public static String getNavigation() {
        return
                "<nav class='navbar navbar-default' role='navigation'>\n" +
                        "    <div class='navbar-header'>\n" +
                        "        <button type='button' class='navbar-toggle' data-toggle='collapse' data-target='#bs-navbar-collapse'>\n" +
                        "            <span class='sr-only'>Toggle navigation</span>\n" +
                        "            <span class='icon-bar'></span>\n" +
                        "            <span class='icon-bar'></span>\n" +
                        "            <span class='icon-bar'></span>\n" +
                        "        </button>\n" +
                        "        <a class='navbar-brand' href='/'>My SongBook</a>\n" +
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
                        "        <!--<form class='navbar-form navbar-left' role='search'>\n" +
                        "          <div class='form-group'>\n" +
                        "            <input type='text' class='form-control' placeholder='Search'>\n" +
                        "          </div>\n" +
                        "          <button type='submit' class='btn btn-default'>Submit</button>\n" +
                        "        </form>-->\n" +
                        "\n" +
                        "    </div><!-- /.navbar-collapse -->\n" +
                        "</nav>\n"
                ;
    }

    @Language("HTML")
    public static String getFooter(String functionToCall) {
        return
                "<!-- JavaScript plugins (requires jQuery) -->\n" +
                "<script src='http://code.jquery.com/jquery.js'></script>\n" +
                "<!-- Include all compiled plugins (below), or include individual files as needed -->\n" +
                "<script src='//netdna.bootstrapcdn.com/bootstrap/3.0.3/js/bootstrap.min.js'></script>\n" +
                "<script src='/js/songbook.js'></script>\n" +
                (functionToCall == null ? "" :
                    "<script type='text/javascript'>\n"+
                    "   " + functionToCall +"\n"+
                    "</script>\n" ) +
                "</body>\n" +
                "</html>\n"
                ;
    }


    @Language("HTML")
    public static String showSongIndex(SongIndex songs) {
        return  getHeader("My SongBook") +
                getNavigation() +
                "<div id='content' class='row'>\n" +
                    "<div id='song-list' class='list-group'>\n" +
                    songs.getSongs().map(id -> {
                                final String title = songs.getTitle(id);
                                return "<a class='list-group-item' href=\"/songs/"+ encodeUrl(id) +"\">\n" +
                                    "<h4 class='list-group-item-heading'>" + (title ==null ? id : title) + "</h4>\n" +
                                    "<p class='list-group-item-text'>" +
                                        songs.getAuthors(id).collect(Collectors.joining(", ")) +
                                    "</p>" +
                                "</a>\n";
                            }
                    ).collect(Collectors.joining()) +
                    "</div>\n" +
                "</div>" +
                getFooter(null)
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
    public static String showDocument(Document document) {
        String id = document.get("id");
        String title = document.get("title");
        return  "<a class='list-group-item' href=\"/songs/"+ encodeUrl(id) +"\">\n" +
                "<h4 class='list-group-item-heading'>" + (title ==null ? id : title) + "</h4>\n" +
                "<p class='list-group-item-text'>" +
                Stream.of(document.getValues("author")).collect(Collectors.joining(", "))+
                "</p>" +
                "</a>\n";

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


    @Language("HTML")
    public static String showSong(Song song) {
        final String title = song.findTitle();
        return  getHeader(title + " - My SongBook") +
                getNavigation() +
                "<div id='song' class='song-content'>\n" +
                    "<div class='song'>\n" +
                        "<div class='song-title'>"+ title +"</div>\n" +
                        song.findAuthors().map(author ->
                            "<div class='song-author'>"+ author +"</div>\n"
                        ).collect(Collectors.joining()) +
                        "<pre>"+ song.lyrics +"</pre>\n" +
                    "</div>\n" +
                "</div>\n" +
                getFooter(null)
            ;
    }
    

}
