package doip.simulation.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import doip.simulation.api.SimulationManager;
import doip.simulation.http.helpers.HttpServerHelper;

import com.starcode88.http.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Represents an HTTP server for handling POST and GET requests.
 */
public class DoipHttpServer {

	private static Logger logger = LogManager.getLogger(DoipHttpServer.class);

	private static final int DEFAULT_PORT = 8080;
	private HttpServer server;
	private SimulationManager simulationManager = null;

	public SimulationManager getSimulationManager() {
		return simulationManager;
	}

	private List<ContextHandler> handlers;

	/**
	 * Checks if the server is currently running.
	 *
	 * @return true if the server is running, false otherwise.
	 */
	private boolean isRunning = false;

	public boolean isRunning() {
		return isRunning;
	}

	// To make the start() and stop() methods thread-safe
	private final Object lock = new Object(); // Object for synchronization

	/**
	 * Constructs a new DoipHttpServer with the default port.
	 *
	 * @param simulationManager The simulation manager for handling
	 *                          simulation-related functionality.
	 * @throws IOException If an I/O error occurs while creating the server.
	 */

	public DoipHttpServer(SimulationManager simulationManager) throws IOException {
		this(DEFAULT_PORT, simulationManager);
	}

	/**
	 * Constructs a new DoipHttpServer with a specified port.
	 *
	 * @param port              The port on which the server will listen.
	 * @param simulationManager The simulation manager for handling
	 *                          simulation-related functionality.
	 * @throws IOException If an I/O error occurs while creating the server.
	 */
	public DoipHttpServer(int port, SimulationManager simulationManager) throws IOException {
		this.simulationManager = simulationManager;

		server = HttpServer.create(new InetSocketAddress(port), 0);

		handlers = new ArrayList<ContextHandler>();

		createMappingContexts(); // TODO:

		for (ContextHandler contextHandler : handlers) {
			server.createContext(contextHandler.getContext(), contextHandler.getHandler());
		}
		server.setExecutor(null); // Use the default executor
	}

	/**
	 * Creates default mapping contexts for POST and GET requests.
	 */

	private void createMappingContexts() {
		List<ContextHandler> defaultHandlers = List.of(new ContextHandler("/posttest", new PostHandler()),
				new ContextHandler("/gettest", new GetHandler()));
		createMappingContexts(defaultHandlers);
	}

	/**
	 * Adds a custom mapping context for handling HTTP requests if it does not
	 * already exist.
	 *
	 * @param context The context path for the mapping.
	 * @param handler The HTTP handler for processing requests in the specified
	 *                context.
	 */
	public void addMappingContext(String context, HttpHandler handler) {
		// Check if the context already exists in handlers
		if (!contextExists(context)) {
			handlers.add(new ContextHandler(context, handler));
			logger.info("Added mapping context: {}", context);
		} else {
			logger.warn("Mapping context '{}' already exists. Not adding it again.", context);
		}
	}

	/**
	 * Adds a list of custom mapping contexts for handling HTTP requests.
	 *
	 * @param customHandlers The list of custom context handlers.
	 */
	public void createMappingContexts(List<ContextHandler> customHandlers) {
		for (ContextHandler customHandler : customHandlers) {
			// Check if the context already exists in handlers before adding
			if (!contextExists(customHandler.getContext())) {
				handlers.add(customHandler);
				logger.info("Added mapping context: {}", customHandler.getContext());
			} else {
				logger.warn("Mapping context '{}' already exists. Not adding it again.", customHandler.getContext());
			}
		}
	}

//	/**
//	 * Adds a list of custom mapping contexts for handling HTTP requests.
//	 *
//	 * @param customHandlers The list of custom context handlers.
//	 */
//	public void createMappingContextsAll(List<ContextHandler> customHandlers) {
//		handlers.addAll(customHandlers);
//	}

	/**
	 * Checks if a context path already exists in the handlers.
	 *
	 * @param context The context path to check.
	 * @return true if the context path already exists, false otherwise.
	 */
	private boolean contextExists(String context) {
		return handlers.stream().anyMatch(handler -> handler.getContext().equals(context));
	}

	/**
	 * Starts the HTTP server.
	 */
	public void start() {
		synchronized (lock) {
			try {
				if (server != null && !isRunning) {
					server.start();
					logger.info("Server is running on port {}.", server.getAddress().getPort());
					isRunning = true;
				}
			} catch (Exception e) {
				logger.error("Error starting the server: {}", e.getMessage(), e);
			}
		}
	}

	/**
	 * Stops the HTTP server.
	 */
	public void stop() {
		synchronized (lock) {
			if (server != null && isRunning) {
				server.stop(0);
				logger.info("Server stopped.");
				isRunning = false;
			}
		}
	}

	
}

class PostHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("POST".equals(exchange.getRequestMethod())) {
			// Read the request body as a string
			// String requestString = DoipHttpServer.readRequestBodyAsString(exchange);
			String requestString = HttpServerHelper.readRequestBody(exchange, String.class);
			HttpServerHelper.requestServerLogging(exchange, requestString);

			// Process the request string
			String response = "Received the following POST request: " + requestString;

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

class GetHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("GET".equals(exchange.getRequestMethod())) {
			// Create the GET response
			String response = "This is a GET request response.";

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

class ContextHandler {
	private String context;
	private HttpHandler handler;

	public ContextHandler(String context, HttpHandler handler) {
		this.context = context;
		this.handler = handler;
	}

	public String getContext() {
		return context;
	}

	public HttpHandler getHandler() {
		return handler;
	}
}
