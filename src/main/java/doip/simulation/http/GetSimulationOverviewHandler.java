package doip.simulation.http;

import java.io.IOException;

import com.starcode88.http.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import doip.simulation.http.helpers.HttpServerHelper;

import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import doip.simulation.http.lib.Gateway;
import doip.simulation.http.lib.ServerInfo;
import doip.simulation.http.lib.SimulationStatus;
import doip.simulation.http.lib.Platform;

public class GetSimulationOverviewHandler implements HttpHandler {
	private static Logger logger = LogManager.getLogger(GetSimulationOverviewHandler.class);
	
	private final DoipHttpServer doipHttpServer;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// Constructor to receive the DoipHttpServer instance
	public GetSimulationOverviewHandler(DoipHttpServer doipHttpServer) {
		this.doipHttpServer = doipHttpServer;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("GET".equals(exchange.getRequestMethod())) {
			handleGetRequest(exchange);
		} else {
			// Respond with 405 Method Not Allowed for non-GET requests
			logger.error("Method not allowed. Received a {} request.", exchange.getRequestMethod());
			exchange.sendResponseHeaders(405, -1);
		}
	}

	private void handleGetRequest(HttpExchange exchange) throws IOException {
		try {
			// Get the query from the URI
			URI uri = exchange.getRequestURI();
			String query = uri.getQuery();

			// Parse query parameters
			Map<String, String> queryParams = HttpServerHelper.parseQueryParameters(query);

			// Validate and process the 'status' parameter
			String status = queryParams.get("status");
			if (status == null || !isValidStatus(status)) {
				exchange.sendResponseHeaders(400, -1); // Bad Request
				return;
			}

			// Build the JSON response based on the status
			String jsonResponse = buildJsonResponse(status);

			// Set the response headers and body
			HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
			HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			logger.error("Error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
		}
	}

	private boolean isValidStatus(String status) {
		// Validate the 'status' parameter against the allowed values
		try {
			SimulationStatus simulationStatus = SimulationStatus.valueOf(status);
			return true; // If no exception is thrown, the status is valid
		} catch (IllegalArgumentException e) {
			return false; // If an exception is thrown, the status is invalid
		}
	}

	private String buildJsonResponse(String status) throws IOException {

		// TODO !!!
		// doipHttpServer.getSimulationManager().getPlatforms();

		ServerInfo serverInfo = createSampleJson(status);

		// Convert the ServerInfo object to JSON
		return objectMapper.writeValueAsString(serverInfo);
	}

	private ServerInfo createSampleJson(String status) {
		// Build a JSON response based on the specified 'status'
		ServerInfo serverInfo = new ServerInfo();

		// Create a Platform
		Platform platform = new Platform();
		platform.name = "X2024";
		platform.url = "http://myserver.com/doip-simulation/platform/X2024";
		platform.status = status;

		// Create a Gateway
		Gateway gateway = new Gateway();
		gateway.name = "string";
		gateway.url = "http://myserver.com/doip-simulation/platform/X2024/gateway/GW";
		gateway.status = status;

		// Add error information for the gateway (if applicable)
		if ("ERROR".equals(status)) {
			gateway.error = "Can't bind to port 13400 because it is already used by another gateway";
		}

		// Add the gateway to the platform's gateways list
		platform.gateways = List.of(gateway);

		// Add the platform to the serverInfo's platforms list
		serverInfo.platforms = List.of(platform);

		return serverInfo;
	}

}
