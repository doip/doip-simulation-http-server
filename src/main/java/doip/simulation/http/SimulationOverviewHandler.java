package doip.simulation.http;

import java.io.IOException;

import com.starcode88.http.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import doip.simulation.api.ServiceState;
import doip.simulation.http.helpers.HttpServerHelper;

import java.net.URI;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Define a handler for the "/doip-simulation/" path
 */
public class SimulationOverviewHandler implements HttpHandler {
	private static Logger logger = LogManager.getLogger(SimulationOverviewHandler.class);

	// Reference to the DoipHttpServer instance
	// private final DoipHttpServer doipHttpServer;
	private final SimulationConnector simulationConnector;

	public SimulationOverviewHandler(SimulationConnector simulationConnector) {
		this.simulationConnector = simulationConnector;
		// this.doipHttpServer = doipHttpServer;
	}

	/**
	 * Handle method for processing incoming HTTP requests
	 * /doip-simulation/?status=RUNNING' /doip-simulation/
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		URI uri = exchange.getRequestURI();
		logger.info("Full URI: {}", uri.toString());

		String hostWithPort = HttpServerHelper.getHostWithPort(exchange);
		if (hostWithPort != null) {
			simulationConnector.setServerNameFromRequestHeader("http://" + hostWithPort);
		}

		if ("GET".equals(exchange.getRequestMethod())) {
			handleGetRequest(exchange);
		} else {
			// Respond with 405 Method Not Allowed for non-GET requests
			logger.error("Method not allowed. Received a {} request.", exchange.getRequestMethod());
			exchange.sendResponseHeaders(DoipHttpServer.HTTP_METHOD_NOT_ALLOWED, -1);
		}
	}

	private void handleGetRequest(HttpExchange exchange) throws IOException {
		try {
			// Get the query from the URI
			URI uri = exchange.getRequestURI();
			String query = uri.getQuery();
			
			logger.info("Path component of this URI :{} ", exchange.getRequestURI().getPath());

			logger.info("Returns the decoded query component of this URI: {}", query);
	
			String status = "";

			if (query != null && !query.trim().isEmpty()) {

				// Parse query parameters
				Map<String, String> queryParams = HttpServerHelper.parseQueryParameters(query);

				// Validate and process the 'status' parameter
				status = queryParams.get("status");
				if (status == null || !isValidStatus(status)) {
					// If 'status' is not empty and not a valid status, return Bad Request
					logger.error("Invalid status provided: {}", status);
					exchange.sendResponseHeaders(DoipHttpServer.HTTP_BAD_REQUEST, -1); // Bad Request
					return;
				}
			}
			// Build the JSON response based on the status
			String jsonResponse = simulationConnector.buildOverviewJsonResponse(status);

			// Set the response headers and body
			HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", DoipHttpServer.HTTP_OK);
			HttpServerHelper.responseServerLogging(exchange, DoipHttpServer.HTTP_OK, jsonResponse);

		} catch (IllegalArgumentException e) {
			// Handle invalid status
			logger.error("Invalid status provided: {}", e.getMessage());
			exchange.sendResponseHeaders(DoipHttpServer.HTTP_BAD_REQUEST, -1); // Bad Request
		} catch (IOException e) {
			// Handle I/O errors
			logger.error("I/O error processing request: {}", e.getMessage());
			exchange.sendResponseHeaders(DoipHttpServer.HTTP_INTERNAL_SERVER_ERROR, -1); // Internal Server Error
		} catch (Exception e) {
			// Catch unexpected exceptions
			logger.error("Unexpected error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(DoipHttpServer.HTTP_INTERNAL_SERVER_ERROR, -1); // Internal Server Error
		}
	}

	private boolean isValidStatus(String status) {
		// Validate the 'status' parameter against the allowed values
		try {
			// SimulationStatus simulationStatus = SimulationStatus.valueOf(status);
			ServiceState simulationStatus = ServiceState.valueOf(status);
			return true; // If no exception is thrown, the status is valid
		} catch (IllegalArgumentException e) {
			return false; // If an exception is thrown, the status is invalid
		}
	}

}
