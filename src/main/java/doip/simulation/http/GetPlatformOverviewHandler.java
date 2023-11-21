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
import doip.simulation.http.lib.Ecu;
import doip.simulation.http.lib.LookupEntry;
import doip.simulation.http.lib.Modifier;

//Define a handler for the "/doip-simulation/platform" path
public class GetPlatformOverviewHandler implements HttpHandler {
	private static Logger logger = LogManager.getLogger(GetPlatformOverviewHandler.class);

	private final DoipHttpServer doipHttpServer;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final String GATEWAY_PATH = "/gateway";

	// Constructor to receive the DoipHttpServer instance
	public GetPlatformOverviewHandler(DoipHttpServer doipHttpServer) {
		this.doipHttpServer = doipHttpServer;
	}

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
			exchange.sendResponseHeaders(405, -1);
		}
	}

	private void handlePostPlatformRequest(HttpExchange exchange) throws IOException {
		try {
			// Extract platform parameter from the path
			String requestPath = exchange.getRequestURI().getPath();
			String platformParam = HttpServerHelper.getPathParam(requestPath, "platform");
			if (platformParam != null) {

				String requestInfo = String.format("This is a POST request for platform: %s", platformParam);
				logger.info(requestInfo);
				
				String requestString = HttpServerHelper.readRequestBodyAsString(exchange);
				HttpServerHelper.requestServerLogging(exchange, requestString);


				// Build the JSON response
				String jsonResponse = buildPlatformJsonResponse();

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
				HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);
			} else {
				// Invalid URL parameters
				exchange.sendResponseHeaders(400, -1); // Bad Request
			}

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
		}
	}

	private void handleGetGatewayRequest(HttpExchange exchange) throws IOException {
		try {
			// Get the request URI
			String requestUri = exchange.getRequestURI().toString();

			// Extract platform and gateway from the path
			String platform = HttpServerHelper.getPathParam(requestUri, "platform");
			String gateway = HttpServerHelper.getPathParam(requestUri, "gateway");

			if (platform != null && gateway != null) {
				// Process the platform and gateway information
				String requestInfo = String.format("This is a GET request for Platform: %s Gateway: %s", platform,
						gateway);
				logger.info(requestInfo);

				// Build the JSON response
				String jsonResponse = buildGatewayJsonResponse();

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
				HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);
			} else {
				// Invalid URL parameters
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
				String jsonResponse = buildPlatformJsonResponse();

				// Set the response headers and body
				HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
				HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);
			} else {
				// Invalid URL parameters
				exchange.sendResponseHeaders(400, -1); // Bad Request
			}

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
		}
	}

	private String buildPlatformJsonResponse() throws IOException {

		// TODO !!!
		// doipHttpServer.getSimulationManager().getPlatforms();

		Platform platformInfo = createPlatformSampleJson();

		// Convert theobject to JSON
		return buildJsonResponse(platformInfo);
	}

	private String buildGatewayJsonResponse() throws IOException {

		// TODO !!!
		// doipHttpServer.getSimulationManager().getPlatforms();

		Gateway gatawayInfo = createGatewaySampleJson();

		// Convert the object to JSON
		return buildJsonResponse(gatawayInfo);
	}

	private String buildJsonResponse(Object info) throws IOException {
		return objectMapper.writeValueAsString(info);
	}

	private Platform createPlatformSampleJson() {

		// Create a Platform
		Platform platform = new Platform();
		platform.name = "X2024";
		platform.url = "http://myserver.com/doip-simulation/platform/X2024";
		platform.status = "RUNNING";

		// Create a Gateway
		Gateway gateway = new Gateway();
		gateway.name = "string";
		gateway.url = "http://myserver.com/doip-simulation/platform/X2024/gateway/GW";
		gateway.status = "RUNNING";

		// Add error information for the gateway (if applicable)
		gateway.error = "Can't bind to port 13400 because it is already used by another gateway";

		// Add the gateway to the platform's gateways list
		platform.gateways = List.of(gateway);

		return platform;
	}

	private Gateway createGatewaySampleJson() {

		// Create an instance of your classes and populate them with data
		Gateway gateway = new Gateway();
		gateway.name = "string";
		gateway.url = "http://myserver.com/doip-simulation/platform/X2024/gateway/GW";
		gateway.status = "RUNNING";
		gateway.error = "Can't bind to port 13400 because it is already used by other gateway";

		List<Ecu> ecus = new ArrayList<>();
		Ecu ecu = new Ecu();
		ecu.name = "EMS";
		ecu.url = "http://myserver.com/doip-simulation/platform/X2024/gateway/GW/ecu/EMS";

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
