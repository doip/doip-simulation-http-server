package doip.simulation.http;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import doip.simulation.http.helpers.HttpServerHelper;
import doip.simulation.http.lib.Gateway;
import doip.simulation.http.lib.Modifier;
import doip.simulation.http.lib.Platform;
import doip.simulation.http.lib.Action;
import doip.simulation.http.lib.ActionRequest;
import doip.simulation.http.lib.Ecu;
import doip.simulation.http.lib.LookupEntry;

/**
 * Define a handler for the "/doip-simulation/platform" path
 */
public class GetPlatformOverviewHandler extends SimulationConnector implements HttpHandler {
	private static Logger logger = LogManager.getLogger(GetPlatformOverviewHandler.class);

	// Reference to the DoipHttpServer instance
	private final DoipHttpServer doipHttpServer;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final String GATEWAY_PATH = "/gateway";

	// Constructor to receive the DoipHttpServer instance
	public GetPlatformOverviewHandler(DoipHttpServer doipHttpServer) {
		super(doipHttpServer.getSimulationManager(), doipHttpServer.getServerName());
		this.doipHttpServer = doipHttpServer;
	}

	/**
	 * Handle method for processing incoming HTTP requests
	 * /doip-simulation/platform/{platformName} (GET)
	 * /doip-simulation/platform/{platformName} (POST)
	 * /doip-simulation/platform/{platformName}/gateway/{gatewayName} (GET)
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
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
			exchange.sendResponseHeaders(405, -1);
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
					String jsonResponse = buildPlatformJsonResponse(platformParam);

					// Set the response headers and body
					HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
					HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);

				} else {

					// Deserialize the JSON string into a ActionRequest object
					ActionRequest receivedAction = HttpServerHelper.deserializeJsonToObject(requestString,
							ActionRequest.class);

					if (receivedAction != null) {
						// Process the received platform information as needed
						logger.info("Received action: {}", receivedAction.getAction().toString());

						// Retrieve the platform based on the specified platform name
						doip.simulation.api.Platform platform = getPlatformByName(platformParam);

						if (platform == null) {
							// Log an error if the specified platform is not found
							logger.error("Action cannot be executed because the specified platform name {} does not exist", platformParam);
						} else {
							performAction(platform, receivedAction.getAction());

						}

						// Build the JSON response
						String jsonResponse = buildPlatformJsonResponse(platformParam);

						// Set the response headers and body
						HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
						HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);
					} else {
						// Invalid JSON structure Platform deserialization failed.
						logger.error("Received JSON structure is invalid.");
						exchange.sendResponseHeaders(400, -1); // Bad Request
					}
				}

			} else {
				// Invalid URL parameters. Platform parameter is missing or invalid.
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(400, -1); // Bad Request
			}

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
		}
	}

	private void performAction(doip.simulation.api.Platform platform, Action action) {
	    switch (action) {
	        case start:
	            logger.info("Starting the process for platform: {}", platform.getName());
	            platform.start();
	            break;
	        case stop:
	            logger.info("Stopping the process for platform: {}", platform.getName());
	            platform.stop();
	            break;
	        default:
	            logger.error("Unknown action: " + action.toString());
	            break;
	    }
	}


	private void handleGetGatewayRequest(HttpExchange exchange) throws IOException {
		try {
			// Get the request URI
			String requestUri = exchange.getRequestURI().toString();

			// Extract platform and gateway from the path
			String platformParam = HttpServerHelper.getPathParam(requestUri, "platform");
			String gatewayParam = HttpServerHelper.getPathParam(requestUri, "gateway");

			if (platformParam != null && gatewayParam != null) {
				// Process the platform and gateway information
				String requestInfo = String.format("This is a GET request for Platform: %s Gateway: %s", platformParam,
						gatewayParam);
				logger.info(requestInfo);

				// Build the JSON response
				String jsonResponse = buildGatewayJsonResponse(platformParam, gatewayParam);

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
				HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);
			} else {
				// Invalid URL parameters
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(400, -1); // Bad Request
			}
		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
		}

	}

	private void handleGetPlatformRequest(HttpExchange exchange) throws IOException {
		try {
			// Extract platform parameter from the path
			String requestPath = exchange.getRequestURI().getPath();
			String platformParam = HttpServerHelper.getPathParam(requestPath, "platform");
			if (platformParam != null) {

				String requestInfo = String.format("This is a GET request for platform: %s", platformParam);
				logger.info(requestInfo);

				// Build the JSON response
				String jsonResponse = buildPlatformJsonResponse(platformParam);

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
				HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);
			} else {
				// Invalid URL parameters
				logger.error("Invalid URL parameters for POST request.");
				exchange.sendResponseHeaders(400, -1); // Bad Request
			}

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
		}
	}

	@Override
	protected String buildPlatformJsonResponse(String platformName) throws IOException {
		try {
			// Retrieve the platform based on the specified platform name
			doip.simulation.api.Platform platform = getPlatformByName(platformName);

			if (platform == null) {
				// Log an error if the specified platform is not found
				logger.error("The specified platform name {} does not exist", platformName);
				return "{}"; // Return an empty JSON object or handle it as needed!
			}

			// TODO:
			//Platform platformInfo = createPlatformSampleJson(platform);

			// Process the retrieved platform and create a real JSON object Platform
			Platform platformInfo = processPlatform(platform);

			// Convert the object to JSON
			return buildJsonResponse(platformInfo);
		} catch (Exception e) {
			// Log an error and return an empty JSON object in case of an exception
			logger.error("Error building platform JSON response: {}", e.getMessage(), e);
			return "{}";
		}
	}

	@Override
	protected String buildGatewayJsonResponse(String platformName, String gatewayName) throws IOException {
		try {
			// Retrieve the gateway based on the specified platform and gateway names
			doip.simulation.api.Gateway gateway = getGatewayByName(platformName, gatewayName);

			if (gateway == null) {
				// Log an error if the specified gateway is not found
				logger.error("The specified gateway name {} does not exist", gatewayName);
				return "{}"; // Return an empty JSON object or handle it as needed
			}

			// TODO:
			//Gateway gatewayInfo = createGatewaySampleJson(gateway, platformName);

			// Process the retrieved gateway and create a real JSON object Gateway
			Gateway gatewayInfo = processGateway(gateway, platformName);

			// Convert the object to JSON
			return buildJsonResponse(gatewayInfo);
		} catch (Exception e) {
			// Log an error and return an empty JSON object in case of an exception
			logger.error("Error building gateway JSON response: {}", e.getMessage(), e);
			return "{}";
		}
	}

	private Platform createPlatformSampleJson(doip.simulation.api.Platform platformCurrent) {
		// Get the server name from the DoipHttpServer
		String serverName = doipHttpServer.getServerName();

		// Create a Platform
		Platform platform = new Platform();
		platform.name = "X2024";
		// platform.url = "http://myserver.com/doip-simulation/platform/X2024";
		String currentPlatformUrl = serverName + PLATFORM_PATH + "/" + platform.name;
		// Update platform URL using the current server name
		platform.url = currentPlatformUrl;

		platform.status = "RUNNING";

		// Create a Gateway
		Gateway gateway = new Gateway();
		gateway.name = "GW";

		// gateway.url =
		// "http://myserver.com/doip-simulation/platform/X2024/gateway/GW";
		String currentGatewayUrl = currentPlatformUrl + "/gateway/" + gateway.name;
		gateway.url = currentGatewayUrl;

		gateway.status = "RUNNING";

		// Add error information for the gateway (if applicable)
		gateway.error = "Can't bind to port 13400 because it is already used by another gateway";

		// Add the gateway to the platform's gateways list
		platform.gateways = List.of(gateway);

		return platform;
	}

	private Gateway createGatewaySampleJson(doip.simulation.api.Gateway gatewayCurrent, String platformName) {

		// Get the server name from the DoipHttpServer
		String serverName = doipHttpServer.getServerName();

		// Create an instance of your classes and populate them with data
		Gateway gateway = new Gateway();
		gateway.name = "GW";

		// gateway.url =
		// "http://myserver.com/doip-simulation/platform/X2024/gateway/GW";
		String currentPlatformUrl = serverName + PLATFORM_PATH + "/" + platformName;
		String currentGatewayUrl = currentPlatformUrl + "/gateway/" + gateway.name;
		gateway.url = currentGatewayUrl;

		gateway.status = "RUNNING";
		gateway.error = "Can't bind to port 13400 because it is already used by other gateway";

		List<Ecu> ecus = new ArrayList<>();
		Ecu ecu = new Ecu();
		ecu.name = "EMS";

		// ecu.url =
		// "http://myserver.com/doip-simulation/platform/X2024/gateway/GW/ecu/EMS";
		String currentEcuUrl = currentGatewayUrl + "/ecu/" + ecu.name;
		ecu.url = currentEcuUrl;

		List<LookupEntry> lookupEntries = new ArrayList<>();
		LookupEntry lookupEntry = new LookupEntry();
		lookupEntry.regex = "10 03";
		lookupEntry.result = "50 03 00 32 01 F4";

		List<Modifier> modifiers = new ArrayList<>();
		Modifier modifier = new Modifier();
		modifier.regex = "22 F1 86";
		modifier.result = "62 F1 86 03";
		modifiers.add(modifier);

		lookupEntry.modifiers = modifiers;
		lookupEntries.add(lookupEntry);

		ecu.configuredLookupTable = lookupEntries;
		ecu.runtimeLookupTable = lookupEntries;

		ecus.add(ecu);
		gateway.ecus = ecus;

		return gateway;
	}

}
