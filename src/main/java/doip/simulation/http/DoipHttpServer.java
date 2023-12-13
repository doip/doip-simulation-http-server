package doip.simulation.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;
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
	private String serverName;
	private int port;

	// Add getter methods for serverName and port
	public String getServerName() {
		return serverName;
	}

	public int getPort() {
		return port;
	}

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

		this.port = port;
		this.serverName = buildServerName(port);

		// TODO: Possibly create default mapping contexts here if needed
		// createTestMappingContexts();

	}

	private String buildServerName(int port) {
		try {
			InetAddress localhost = InetAddress.getLocalHost();
			return "http://" + localhost.getHostAddress() + ":" + port;
			// getHostName
			// return "http://" + localhost.getHostName() + ":" + port;
			// return "http://" + localhost.getCanonicalHostName() + ":" + port;
			// return "http://" + "localhost" + ":" + port;
		} catch (UnknownHostException e) {
			// Handle the case where the local host address is not available

			logger.warn("Local host address is not available");
			// return null; // Or return a default server name
			return "http://localhost:" + port;
		}
	}

	/**
	 * Creates default mapping contexts for POST and GET requests.
	 */
	@SuppressWarnings("unused")
	private void createTestMappingContexts() {
		// Check if the server is running before creating test mapping contexts
		if (isRunning == false) {
//			List<ContextHandler> defaultHandlers = List.of(new ContextHandler("/posttest", new PostHandler()),
//					new ContextHandler("/gettest", new GetHandler()));
//			createMappingContexts(defaultHandlers);
		} else {
			logger.warn("Server is running. Test mapping contexts not created.");
		}
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
		// Check if the server is running before adding mapping context
		if (isRunning == false) {
			// Check if the context already exists in handlers
			if (!contextExists(context)) {
				handlers.add(new ContextHandler(context, handler));
				logger.info("Added mapping context: {}", context);
			} else {
				logger.warn("Mapping context '{}' already exists. Not adding it again.", context);
			}
		} else {
			logger.warn("Server is running. Mapping context not added.");
		}
	}

	/**
	 * Adds a list of custom mapping contexts for handling HTTP requests.
	 *
	 * @param customHandlers The list of custom context handlers.
	 */
	public void createMappingContexts(List<ContextHandler> customHandlers) {
		// Check if the server is running before creating custom mapping contexts
		if (isRunning == false) {
			for (ContextHandler customHandler : customHandlers) {
				// Check if the context already exists in handlers before adding
				if (!contextExists(customHandler.getContext())) {
					handlers.add(customHandler);
					logger.info("Added mapping context: {}", customHandler.getContext());
				} else {
					logger.warn("Mapping context '{}' already exists. Not adding it again.",
							customHandler.getContext());
				}
			}
		} else {
			logger.warn("Server is running. Custom mapping contexts not created.");
		}
	}

	/**
	 * Dynamically adds a custom mapping context for handling HTTP requests.
	 *
	 * @param context The context path for the mapping.
	 * @param handler The HTTP handler for processing requests in the specified
	 *                context.
	 */
	public void addDynamicContext(String contextPath, HttpHandler handler) {
		// Stop the server
		stop();

		// Modify the context configuration
		addMappingContext(contextPath, handler);

		// Restart the server
		start();
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
	public boolean contextExists(String context) {
		return handlers.stream().anyMatch(handler -> handler.getContext().equals(context));
	}

	/**
	 * Gets the list of registered context paths
	 * 
	 * @return A list of strings representing the context paths.
	 */
	public List<String> getRegisteredContextPaths() {
		return handlers.stream().map(ContextHandler::getContext).collect(Collectors.toList());
	}

	/**
	 * Starts the HTTP server.
	 */
	public void start() {
		// Set the executor before registering context handlers
		server.setExecutor(null); // Use the default executor

		// Register context handlers
		for (ContextHandler contextHandler : handlers) {
			server.createContext(contextHandler.getContext(), contextHandler.getHandler());
		}

		// Log the registered contexts and handlers
		logRegisteredHandlers();

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

	/**
	 * Method to log registered handlers
	 */
	private void logRegisteredHandlers() {
		logger.info("Registered Handlers:");
		for (ContextHandler contextHandler : handlers) {
			logger.info("Context: {}, Handler: {}", contextHandler.getContext(), contextHandler.getHandler());
		}
	}

}
