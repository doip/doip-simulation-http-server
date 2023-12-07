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

	private final DoipHttpServer doipHttpServer;

	/**
	 * Constructs a CustomMappingController with the specified DoipHttpServer
	 * instance.
	 *
	 * @param httpServer The DoipHttpServer instance to use.
	 */
	public CustomMappingController(DoipHttpServer httpServer) {
		this.doipHttpServer = httpServer;
	}

	/**
	 * Starts an HTTP server with custom POST and GET mappings.
	 */
	public void startHttpServer() {
		if (doipHttpServer.isRunning()) {
			logger.info("Server is already running.");
			return;
		}
		try {

			configureCustomMappings();
			doipHttpServer.start();
		} catch (Exception e) {
			logger.error("Error starting the server: {}", e.getMessage(), e);
		}
	}

	/**
	 * Adds custom mappings.
	 */
	private void configureCustomMappings() {

		List<ContextHandler> customHandlers = List.of(
		// new ContextHandler("/customPost", new PostHandlerCustom()),
		// new ContextHandler("/customGet", new GetHandlerCustom())
		// ,new ContextHandler("/", new GetSimulationOverviewHandler(doipHttpServer))
		);

		// Create and configure the DoipHttpServer instance
		doipHttpServer.createMappingContexts(customHandlers);
		logger.info("Added custom POST and GET mappings.");
	}

	/**
	 * Adds an external HTTP handler with the specified context path.
	 *
	 * @param contextPath The context path for the handler.
	 * @param handler     The HTTP handler to be added.
	 */
	public void addExternalHandler(String contextPath, HttpHandler handler) {
		if (contextPath != null && handler != null) {

			doipHttpServer.addMappingContext(contextPath, handler);
			logger.info("Added external handler for context path: {}", contextPath);
		} else {
			logger.warn("Invalid parameters for adding external handler.");
		}
	}
	
}
