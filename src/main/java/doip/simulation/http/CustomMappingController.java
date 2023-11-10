package doip.simulation.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

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
	 *
	 */
	public void startHttpServer() {
		if (this.httpServer.isRunning())
		{
			return;
		}
		try {
			// Create custom POST and GET handlers
			HttpHandler postHandler = new PostHandlerCustom();
			HttpHandler getHandler = new GetHandlerCustom();

			// Create custom mapping contexts
			List<ContextHandler> customHandlers = List.of(new ContextHandler("/customPost", postHandler),
					new ContextHandler("/customGet", getHandler));

			// Create and configure the DoipHttpServer instance
			httpServer.createMappingContexts(customHandlers);
			httpServer.start();
		} catch (Exception e) {
			logger.error("Error starting the server: {}", e.getMessage(), e);
		}
	}

	/**
	 * Custom handler for processing POST requests in a custom context.
	 */
	class PostHandlerCustom implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO: Implement custom logic for handling POST requests
			// Read request body, process data, and generate response
			String response = "Custom POST request processed.";
			sendResponse(exchange, response, "text/plain", 200);
		}
	}

	/**
	 * Custom handler for processing GET requests in a custom context.
	 */
	class GetHandlerCustom implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO: Implement custom logic for handling GET requests
			// Process parameters, and generate response
			String response = "Custom GET request processed.";
			sendResponse(exchange, response, "text/plain", 200);
		}
	}

	/**
	 * Sends a response to the client with the given message.
	 *
	 * @param exchange    The HTTP exchange.
	 * @param message     The message to send in the response.
	 * @param contantType The response contant Type
	 * @param code        The response code to send
	 * @throws IOException If an I/O error occurs while sending the response.
	 */

	private void sendResponse(HttpExchange exchange, String message, String contantType, int code) throws IOException {
		exchange.getResponseHeaders().add("Content-Type", contantType);
		exchange.sendResponseHeaders(code, message.getBytes(StandardCharsets.UTF_8).length);
		try (OutputStream responseBody = exchange.getResponseBody()) {
			responseBody.write(message.getBytes(StandardCharsets.UTF_8));
		}
	}

}
