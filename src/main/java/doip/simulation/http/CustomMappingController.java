package doip.simulation.http;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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

        List<ContextHandler> customHandlers = List.of(
                new ContextHandler("/customPost", postHandler),
                new ContextHandler("/customGet", getHandler)
        );

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
			 try {
	                // TODO: Implement custom logic for handling POST requests
	                // Read request body, process data, and generate response
	                String response = "Custom POST request processed.";
	                DoipHttpServer.sendResponse(exchange, response, "text/plain", 200);
	            } catch (IOException e) {
	                logger.error("Error processing POST request: {}", e.getMessage(), e);
	                throw e;
	            }
		}
	}

	/**
	 * Custom handler for processing GET requests in a custom context.
	 */
	class GetHandlerCustom implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			 try {
	                // TODO: Implement custom logic for handling GET requests
	                // Process parameters, and generate response
	                String response = "Custom GET request processed.";
	                httpServer.getSimulationManager().start("Test");

	                DoipHttpServer.sendResponse(exchange, response, "text/plain", 200);
	            } catch (IOException e) {
	                logger.error("Error processing GET request: {}", e.getMessage(), e);
	                throw e;
	            }
		}
	}

}
