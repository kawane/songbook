package songbook.client;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

import java.io.IOException;

/**
 * Created by laurent on 12/11/14.
 */
public class SongApi {

    protected HttpClient client;

    public SongApi(HttpClient client) {
        this.client = client;
    }

    public void requestSong(String id, Handler<HttpClientResponse> responseHandler) {
        HttpClientRequest request = client.get("/songs/" + id, responseHandler);
        request.end();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Vertx vertx = VertxFactory.newVertx();

        SongApi api = new SongApi(vertx.createHttpClient().setHost("localhost").setPort(8080));
        api.requestSong("Adrift", (resp) -> {
            resp.dataHandler((handler) -> {
                System.out.print(handler.toString());
            });
        });
        System.in.read();
    }

}
