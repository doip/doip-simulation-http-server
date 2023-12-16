package doip.simulation.http;


import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpHandler;

import doip.simulation.http.helpers.HttpServerHelper;
public class PostHandlerCustom implements HttpHandler {
	private static Logger logger = LogManager.getLogger(PostHandlerCustom.class);

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		if ("POST".equals(exchange.getRequestMethod())) {
			// Read the request body as a string
			// String requestString = DoipHttpServer.readRequestBodyAsString(exchange);
			String requestString = HttpServerHelper.readRequestBody(exchange, String.class);
			HttpServerHelper.requestServerLogging(exchange, requestString);

			// Custom POST request processed.
			String response = "Received the following Custom POST request: " + requestString;

			// Set the response headers and body
			HttpServerHelper.sendResponse(exchange, response, "text/plain", 200);
			HttpServerHelper.responseServerLogging(exchange, 200, response);

		} else {
			// Method not allowed
			logger.error("Method not allowed. Received a {} request.", exchange.getRequestMethod());
			exchange.sendResponseHeaders(405, -1);
		}
		exchange.close();
	}
}
