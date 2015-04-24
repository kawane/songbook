package songbook.server;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatcher;

import java.util.Map;

/** Simple Path template handler with a fallthrough. */
public class PathTemplateHandler implements HttpHandler {

    private final PathTemplateMatcher<HttpHandler> pathTemplateMatcher = new PathTemplateMatcher<>();

    private final HttpHandler otherHandler;

    public PathTemplateHandler(HttpHandler otherHandler) {
        this.otherHandler = otherHandler;
    }
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // Uses request URI to match path to avoid decoding.
        PathTemplateMatcher.PathMatchResult<HttpHandler> match = pathTemplateMatcher.match(exchange.getRequestURI());
        if (match != null) {
            for (Map.Entry<String, String> entry : match.getParameters().entrySet()) {
                exchange.addQueryParam(entry.getKey(), entry.getValue());
            }
            match.getValue().handleRequest(exchange);
        } else {
            otherHandler.handleRequest(exchange);
        }
    }

    public void add(String uriTemplate, HttpHandler handler) {
        pathTemplateMatcher.add(uriTemplate, handler);
    }

    public void remove(String uriTemplate) {
        pathTemplateMatcher.remove(uriTemplate);
    }

}
