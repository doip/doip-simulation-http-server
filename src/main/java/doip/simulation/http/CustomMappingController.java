package doip.simulation.http;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import doip.simulation.api.SimulationManager;

/**
 * Controller for managing custom HTTP mappings using DoipHttpServer.
 */
public class CustomMappingController {

	private static Logger logger = LogManager.getLogger(CustomMappingController.class);

	public DoipHttpServer httpServer;

	/**
	 * Starts an HTTP server with custom POST and GET mappings.
	 *
	 * @param simulationManager The simulation manager for handling
	 *                          simulation-related functionality.
	 */
	public void startHttpServer(SimulationManager simulationManager) {
		try {

			HttpHandler postHandler = new PostHandlerCustom();
			HttpHandler getHandler = new GetHandlerCustom();

			List<ContextHandler> customHandlers = List.of(new ContextHandler("/customPost", postHandler),
					new ContextHandler("/customGet", getHandler));

			httpServer = new DoipHttpServer(simulationManager);
			httpServer.createMappingContexts(customHandlers);
			httpServer.start();
		} catch (IOException e) {
			logger.error("Error starting the server: {}", e.getMessage(), e);
		}
	}

	/**
	 * Custom handler for processing POST requests in a custom context.
	 */
	class PostHandlerCustom implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO Auto-generated method stub

		}
		// ... (PostHandler implementation)
	}

	/**
	 * Custom handler for processing GET requests in a custom context.
	 */

	class GetHandlerCustom implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO Auto-generated method stub

		}
		// ... (GetHandler implementation)
	}

}
