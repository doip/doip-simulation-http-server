package doip.simulation.http;

import java.io.IOException;
import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import doip.library.exception.DoipException;
import doip.simulation.http.helpers.HttpServerHelper;

import doip.simulation.http.lib.Action;
import doip.simulation.http.lib.ActionRequest;

/**
 * Define a handler for the "/doip-simulation/platform" path
 */
public class GetPlatformOverviewHandler implements HttpHandler {
	private static Logger logger = LogManager.getLogger(GetPlatformOverviewHandler.class);

	// Reference to the DoipHttpServer instance
	// private final DoipHttpServer doipHttpServer;

	private final SimulationConnector simulationConnector;

	private static final String GATEWAY_PATH = "/gateway";

	// Constructor to receive the DoipHttpServer instance
	public GetPlatformOverviewHandler(DoipHttpServer doipHttpServer) {
		// super(doipHttpServer.getSimulationManager(), doipHttpServer.getServerName());
		this(new SimulationConnector(doipHttpServer.getSimulationManager(), doipHttpServer.getServerName()));
	}

	public GetPlatformOverviewHandler(SimulationConnector simulationConnector) {
		this.simulationConnector = simulationConnector;
		// this.doipHttpServer = doipHttpServer;
	}

	/**
	 * Handle method for processing incoming HTTP requests
	 * /doip-simulation/platform/{platformName} (GET)
	 * /doip-simulation/platform/{platformName} (POST)
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
		if ("GET".equals(exchange.getRequestMethod()) && requestPath.contains(GATEWAY_PATH)) {
			handleGetGatewayRequest(exchange);
		} else if ("GET".equals(exchange.getRequestMethod())) {
			handleGetPlatformRequest(exchange);
		} else if ("POST".equals(exchange.getRequestMethod())) {
			handlePostPlatformRequest(exchange);
		} else {
			// Respond with 405 Method Not Allowed for non-GET requests
			logger.error("Method not allowed. Received a {} request.", exchange.getRequestMethod());
			exchange.sendResponseHeaders(DoipHttpServer.HTTP_METHOD_NOT_ALLOWED, -1);
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
					String jsonResponse = simulationConnector.buildPlatformJsonResponse(platformParam);

					// Set the response headers and body
					HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", DoipHttpServer.HTTP_OK);
					HttpServerHelper.responseServerLogging(exchange, DoipHttpServer.HTTP_OK, jsonResponse);

				} else {

					// Deserialize the JSON string into a ActionRequest object
					ActionRequest receivedAction = HttpServerHelper.deserializeJsonToObject(requestString,
							ActionRequest.class);

					if (receivedAction != null) {
						// Process the received platform information
						logger.info("Received action: {}", receivedAction.getAction().toString());

						simulationConnector.handlePlatformAction(platformParam, receivedAction);

						// Build the JSON response
						String jsonResponse = simulationConnector.buildPlatformJsonResponse(platformParam);

						// Set the response headers and body
						HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", DoipHttpServer.HTTP_OK);
						HttpServerHelper.responseServerLogging(exchange, DoipHttpServer.HTTP_OK, jsonResponse);
					} else {
						// Invalid JSON structure Platform deserialization failed.
						logger.error("Received JSON structure is invalid.");
						exchange.sendResponseHeaders(DoipHttpServer.HTTP_BAD_REQUEST, -1); // Bad Request
					}
				}

			} else {
				// Invalid URL parameters. Platform parameter is missing or invalid.
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(DoipHttpServer.HTTP_BAD_REQUEST, -1); // Bad Request
			}

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(DoipHttpServer.HTTP_INTERNAL_SERVER_ERROR, -1); // Internal Server Error
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
				String jsonResponse = simulationConnector.buildGatewayJsonResponse(platformParam, gatewayParam);

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", DoipHttpServer.HTTP_OK);
				HttpServerHelper.responseServerLogging(exchange, DoipHttpServer.HTTP_OK, jsonResponse);
			} else {
				// Invalid URL parameters
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(DoipHttpServer.HTTP_BAD_REQUEST, -1); // Bad Request
			}
		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(DoipHttpServer.HTTP_INTERNAL_SERVER_ERROR, -1); // Internal Server Error
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
				String jsonResponse = simulationConnector.buildPlatformJsonResponse(platformParam);

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", DoipHttpServer.HTTP_OK);
				HttpServerHelper.responseServerLogging(exchange, DoipHttpServer.HTTP_OK, jsonResponse);
			} else {
				// Invalid URL parameters
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(DoipHttpServer.HTTP_BAD_REQUEST, -1); // Bad Request
			}

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(DoipHttpServer.HTTP_INTERNAL_SERVER_ERROR, -1); // Internal Server Error
		}
	}

}
