package doip.simulation.http;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import doip.simulation.http.helpers.HttpServerHelper;

/**
 * Controller for managing custom HTTP mappings using DoipHttpServer.
 */
public class CustomMappingController {

	private static Logger logger = LogManager.getLogger(CustomMappingController.class);

	private final DoipHttpServer httpServer;

	/**
	 * Constructs a CustomMappingController with the specified DoipHttpServer
	 * instance.
	 *
	 * @param httpServer The DoipHttpServer instance to use.
	 */
	public CustomMappingController(DoipHttpServer httpServer) {
		this.httpServer = httpServer;
	}

	/**
	 * Starts an HTTP server with custom POST and GET mappings.
	 */
	public void startHttpServer() {
		if (httpServer.isRunning()) {
			logger.info("Server is already running.");
			return;
		}
		try {

			addCustomMappings();
			httpServer.start();
		} catch (Exception e) {
			logger.error("Error starting the server: {}", e.getMessage(), e);
		}
	}

	/**
	 * Adds custom mappings.
	 */
	private void addCustomMappings() {
		HttpHandler postHandler = new PostHandlerCustom();
		HttpHandler getHandler = new GetHandlerCustom();

		List<ContextHandler> customHandlers = List.of(new ContextHandler("/customPost", postHandler),
				new ContextHandler("/customGet", getHandler));

		// Create and configure the DoipHttpServer instance
		httpServer.createMappingContexts(customHandlers);
		logger.info("Added custom POST and GET mappings.");
	}

	/**
	 * Custom handler for processing POST requests in a custom context.
	 */
	class PostHandlerCustom implements HttpHandler {

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
				exchange.sendResponseHeaders(405, -1);
			}
			exchange.close();
		}
	}

	/**
	 * Custom handler for processing GET requests in a custom context.
	 */
	class GetHandlerCustom implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {

			if ("GET".equals(exchange.getRequestMethod())) {
				// Create the GET response
				String response = "Custom GET request processed.";
				
				//TODO:
				//httpServer.getSimulationManager().start("Test");

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, response, "text/plain", 200);
				HttpServerHelper.responseServerLogging(exchange, 200, response);

			} else {
				// Method not allowed
				exchange.sendResponseHeaders(405, -1);
			}
			exchange.close();
		}
	}

}
