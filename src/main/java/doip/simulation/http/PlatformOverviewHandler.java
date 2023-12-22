package doip.simulation.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import doip.library.exception.DoipException;
import doip.simulation.api.ServiceState;
import doip.simulation.http.helpers.HttpServerHelper;

import doip.simulation.http.lib.Action;
import doip.simulation.http.lib.ActionRequest;

/**
 * Define a handler for the "/doip-simulation/platform" path
 */
public class PlatformOverviewHandler implements HttpHandler {
	private static Logger logger = LogManager.getLogger(PlatformOverviewHandler.class);

	private final SimulationConnector simulationConnector;

	public final static String RESOURCE_PATH = "/doip-simulation/platform";

	private static final String GATEWAY_PATH = "/gateway";

	public PlatformOverviewHandler(SimulationConnector simulationConnector) {
		this.simulationConnector = simulationConnector;
	}

	/**
	 * Handle method for processing incoming HTTP requests
	 * /doip-simulation/platform/{platformName} (GET)
	 * /doip-simulation/platform/{platformName} (POST)
	 * /doip-simulation/platform/{platformName}?action=start (GET)
	 * /doip-simulation/platform/{platformName}/gateway/{gatewayName} (GET)
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {

		URI uri = exchange.getRequestURI();
		logger.info("Full URI: {}", uri.toString());

		String hostWithPort = HttpServerHelper.getHostWithPort(exchange);
		if (hostWithPort != null) {
			simulationConnector.setServerNameFromRequestHeader("http://" + hostWithPort);
		}

		String requestPath = exchange.getRequestURI().getPath();
		String requestMethod = exchange.getRequestMethod();

		if ("GET".equals(requestMethod) && requestPath.contains(GATEWAY_PATH)) {
			handleGetGatewayRequest(exchange);
		} else if ("GET".equals(requestMethod)) {
			if (isStartActionRequest(exchange)) {
				handleStartActionRequest(exchange);
			} else {
				handleGetPlatformRequest(exchange);
			}
		} else if ("POST".equals(requestMethod)) {
			handlePostPlatformRequest(exchange);
		} else {
			// Respond with 405 Method Not Allowed for non-GET requests
			logger.error("Method not allowed. Received a {} request.", requestMethod);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, -1);
		}
	}

	// Method to handle the special case for starting an action
	private void handleStartActionRequest(HttpExchange exchange) throws IOException {
		try {
			// Extract platform parameter from the path
			String requestPath = exchange.getRequestURI().getPath();

			logger.info("Path component of this URI :{} ", requestPath);

			String platformParam = HttpServerHelper.getPathParam(requestPath, "platform");
			if (platformParam != null) {

				String requestInfo = String.format("This is a POST request for platform: %s", platformParam);
				logger.info(requestInfo);

				// Deserialize the JSON string into a ActionRequest object
				String actionParam = HttpServerHelper.getQueryParam(exchange, "action");
				if (actionParam == null || !isValidAction(actionParam)) {
					// If 'status' is not empty and not a valid status, return Bad Request
					logger.error("Invalid status provided: {}", actionParam);
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1); // Bad Request
					return;
				}

				Action currentAction = Action.valueOf(actionParam);
				ActionRequest receivedAction = new ActionRequest();
				receivedAction.setAction(currentAction);

				// Process the received platform information
				logger.info("Received action: {}", receivedAction.getAction().toString());

				SimulationResponse simulationResponse = simulationConnector.handlePlatformAction(platformParam,
						receivedAction);

				// Build the JSON response based on the outcome of handlePlatformAction
				String jsonResponse;
				if (simulationResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
					jsonResponse = simulationResponse.getJsonResponse();
				} else {
					jsonResponse = simulationConnector.buildPlatformJsonResponse(platformParam).getJsonResponse();
				}

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json",
						simulationResponse.getStatusCode());
				HttpServerHelper.responseServerLogging(exchange, simulationResponse.getStatusCode(), jsonResponse);

//				HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", HttpURLConnection.HTTP_OK);
//				HttpServerHelper.responseServerLogging(exchange, HttpURLConnection.HTTP_OK, jsonResponse);

			} else {
				// Invalid URL parameters. Platform parameter is missing or invalid.
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1); // Bad Request
			}

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1); // Internal Server Error
		}
	}

	private void handlePostPlatformRequest(HttpExchange exchange) throws IOException {
		try {
			// Extract platform parameter from the path
			String requestPath = exchange.getRequestURI().getPath();

			logger.info("Path component of this URI :{} ", requestPath);

			String platformParam = HttpServerHelper.getPathParam(requestPath, "platform");
			if (platformParam != null) {

				// Check the Content-Type header
				String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
				if (contentType == null || !contentType.equalsIgnoreCase("application/json")) {
					// Invalid or missing Content-Type header
					// exchange.sendResponseHeaders(415, -1); // Unsupported Media Type
					// Log a warning for invalid or missing Content-Type header
					logger.warn(
							"Invalid or missing Content-Type header. Actual Content-Type: {}. Proceeding with the request.",
							contentType);
				}

				String requestInfo = String.format("This is a POST request for platform: %s", platformParam);
				logger.info(requestInfo);

				String requestString = HttpServerHelper.readRequestBodyAsString(exchange);
				HttpServerHelper.requestServerLogging(exchange, requestString);

				// Check if the requestString is empty or null
				if (requestString == null || requestString.trim().isEmpty()) {
					// Empty JSON request
//TODO:					
//	                logger.warn("Received an empty JSON request.");
//	                exchange.sendResponseHeaders(400, -1); // Bad Request
//	                return;

					// If an empty request JSON is allowed
					// Build the JSON response
					SimulationResponse simulationResponse = simulationConnector
							.buildPlatformJsonResponse(platformParam);

					// Set the response headers and body
					HttpServerHelper.sendResponse(exchange, simulationResponse.getJsonResponse(), "application/json",
							simulationResponse.getStatusCode());
					HttpServerHelper.responseServerLogging(exchange, simulationResponse.getStatusCode(),
							simulationResponse.getJsonResponse());
				} else {

					// Deserialize the JSON string into a ActionRequest object
					ActionRequest receivedAction = HttpServerHelper.deserializeJsonToObject(requestString,
							ActionRequest.class);

					if (receivedAction != null) {
						// Process the received platform information
						logger.info("Received action: {}", receivedAction.getAction().toString());

						SimulationResponse simulationResponse = simulationConnector.handlePlatformAction(platformParam,
								receivedAction);

						// Build the JSON response based on the outcome of handlePlatformAction
						String jsonResponse;
						if (simulationResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
							jsonResponse = simulationResponse.getJsonResponse();
						} else {
							jsonResponse = simulationConnector.buildPlatformJsonResponse(platformParam)
									.getJsonResponse();
						}

						// Set the response headers and body
						HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json",
								simulationResponse.getStatusCode());
						HttpServerHelper.responseServerLogging(exchange, simulationResponse.getStatusCode(),
								jsonResponse);

					} else {
						// Invalid JSON structure Platform deserialization failed.
						logger.error("Received JSON structure is invalid.");
						exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1); // Bad Request
					}
				}

			} else {
				// Invalid URL parameters. Platform parameter is missing or invalid.
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1); // Bad Request
			}

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1); // Internal Server Error
		}
	}

	private void handleGetGatewayRequest(HttpExchange exchange) throws IOException {
		try {
			// Get the request URI
			String requestUri = exchange.getRequestURI().toString();

			logger.info("Path component of this URI :{} ", exchange.getRequestURI().getPath());

			// Extract platform and gateway from the path
			String platformParam = HttpServerHelper.getPathParam(requestUri, "platform");
			String gatewayParam = HttpServerHelper.getPathParam(requestUri, "gateway");

			if (platformParam != null && gatewayParam != null) {
				// Process the platform and gateway information
				String requestInfo = String.format("This is a GET request for Platform: %s Gateway: %s", platformParam,
						gatewayParam);
				logger.info(requestInfo);

				// Build the JSON response
				SimulationResponse simulationResponse = simulationConnector.buildGatewayJsonResponse(platformParam,
						gatewayParam);

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, simulationResponse.getJsonResponse(), "application/json",
						simulationResponse.getStatusCode());
				HttpServerHelper.responseServerLogging(exchange, simulationResponse.getStatusCode(),
						simulationResponse.getJsonResponse());

			} else {
				// Invalid URL parameters
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1); // Bad Request
			}
		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1); // Internal Server Error
		}

	}

	private void handleGetPlatformRequest(HttpExchange exchange) throws IOException {
		try {
			// Extract platform parameter from the path
			String requestPath = exchange.getRequestURI().getPath();
			logger.info("Path component of this URI :{} ", exchange.getRequestURI().getPath());

			String platformParam = HttpServerHelper.getPathParam(requestPath, "platform");
			if (platformParam != null) {

				String requestInfo = String.format("This is a GET request for platform: %s", platformParam);
				logger.info(requestInfo);

				// Build the JSON response
				SimulationResponse simulationResponse = simulationConnector.buildPlatformJsonResponse(platformParam);

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, simulationResponse.getJsonResponse(), "application/json",
						simulationResponse.getStatusCode());
				HttpServerHelper.responseServerLogging(exchange, simulationResponse.getStatusCode(),
						simulationResponse.getJsonResponse());
			} else {
				// Invalid URL parameters
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, -1); // Bad Request
			}

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1); // Internal Server Error
		}
	}

	// Helper method to check if it's a special case for starting an action
	private boolean isStartActionRequest(HttpExchange exchange) {
		String requestPath = exchange.getRequestURI().getPath();
		String platformParam = HttpServerHelper.getPathParam(requestPath, "platform");
		String actionParam = HttpServerHelper.getQueryParam(exchange, "action");

		boolean isValidRequest = actionParam != null && platformParam != null; // && isValidAction(actionParam)

		if (!isValidRequest) {
			//logger.error("Invalid action request. Platform: {}, Action: {}", platformParam, actionParam);
		}

		return isValidRequest;
	}

	private boolean isValidAction(String action) {
		// Validate the 'action' parameter against the allowed values
		/*
		 * TODO: @Nataliya: Using exceptions to validate correctness is expensive.
		 * Building exceptions by JVM takes a lot of time.
		 */
		try {
			Action currentAction = Action.valueOf(action);
			return true; // If no exception is thrown, the status is valid
		} catch (IllegalArgumentException e) {
			return false; // If an exception is thrown, the status is invalid
		}
	}
}
