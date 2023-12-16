package doip.simulation.http;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import doip.simulation.http.helpers.HttpServerHelper;
/**
 * Custom handler for processing GET requests in a custom context.
 */
class GetHandlerCustom implements HttpHandler {
	private static Logger logger = LogManager.getLogger(GetHandlerCustom.class);

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		if ("GET".equals(exchange.getRequestMethod())) {
			// Create the GET response
			String response = "Custom GET request processed.";

			// TODO:
			// httpServer.getSimulationManager().start("Test");

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