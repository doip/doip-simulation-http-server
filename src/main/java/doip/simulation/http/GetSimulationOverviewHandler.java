package doip.simulation.http;

import java.io.IOException;

import com.starcode88.http.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import doip.simulation.api.ServiceState;
import doip.simulation.http.helpers.HttpServerHelper;

import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import doip.simulation.http.lib.Gateway;
import doip.simulation.http.lib.ServerInfo;
import doip.simulation.http.lib.Platform;

/**
 * Define a handler for the "/doip-simulation/" path
 */
public class GetSimulationOverviewHandler extends SimulationConnector implements HttpHandler {
	private static Logger logger = LogManager.getLogger(GetSimulationOverviewHandler.class);

	// Reference to the DoipHttpServer instance
	private final DoipHttpServer doipHttpServer;

	private final ObjectMapper objectMapper = new ObjectMapper();

	// Constructor to receive the DoipHttpServer instance
	public GetSimulationOverviewHandler(DoipHttpServer doipHttpServer) {
		super(doipHttpServer.getSimulationManager(), doipHttpServer.getServerName());
		this.doipHttpServer = doipHttpServer;
	}

	/**
	 * Handle method for processing incoming HTTP requests
	 * /doip-simulation/?status=RUNNING' /doip-simulation/
	 */
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
			
			logger.info("Path component of this URI :{} ", exchange.getRequestURI().getPath());

			logger.info("Returns the decoded query component of this URI: {}", query);

			String status = "";

			if (query != null && !query.trim().isEmpty()) {

				// Parse query parameters
				Map<String, String> queryParams = HttpServerHelper.parseQueryParameters(query);

				// Validate and process the 'status' parameter
				status = queryParams.get("status");
				if (status == null || !isValidStatus(status)) {
					exchange.sendResponseHeaders(400, -1); // Bad Request
					return;
				}
			}

			// Build the JSON response based on the status
			String jsonResponse = buildOverviewJsonResponse(status);

			// Set the response headers and body
			HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
			HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);

		} catch (IllegalArgumentException e) {
			// Handle invalid status
			logger.error("Invalid status provided: {}", e.getMessage());
			exchange.sendResponseHeaders(400, -1); // Bad Request
		} catch (IOException e) {
			// Handle I/O errors
			logger.error("I/O error processing request: {}", e.getMessage());
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
		} catch (Exception e) {
			// Catch unexpected exceptions
			logger.error("Unexpected error processing request: {}", e.getMessage(), e);
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
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

	@Override
	protected String buildOverviewJsonResponse(String status) throws IOException {
		try {
			// Retrieve platform overview based on the status
			List<doip.simulation.api.Platform> platforms = getPlatformOverview(status);

			if (platforms == null) {
				// Log an error if platform overview retrieval fails
				logger.error("Failed to retrieve platform overview. Check logs for details.");
				// return "{}"; // Return an empty JSON object or handle it as needed
			}
			
			//TODO:
			// Create ServerInfo for platforms
			ServerInfo serverInfo = createSampleJson(platforms, status);
			
			// Create a real JSON object Platform
			//ServerInfo serverInfo = processOverview(platforms, status);

			// Process the retrieved platforms if needed
//			for (doip.simulation.api.Platform platform : platforms) {
//				// Do something with each platform if needed
//			}

			// Convert the ServerInfo object to JSON
			return buildJsonResponse(serverInfo);
		} catch (Exception e) {
			// Log an error and return an empty JSON object in case of an exception
			logger.error("Error building overview JSON response: {}", e.getMessage(), e);
			return "{}";
		}
	}

	// Method to build a sample JSON response
	private ServerInfo createSampleJson(List<doip.simulation.api.Platform> platforms, String status) {
		// Get the server name from the DoipHttpServer
        String serverName = doipHttpServer.getServerName();
		// Build a JSON response based on the specified 'status'
		ServerInfo serverInfo = new ServerInfo();

		// Create a Platform
		Platform platform = new Platform();
		platform.name = "X2024";
		
		//platform.url = "http://myserver.com/doip-simulation/platform/X2024";
		String currentPlatformUrl = serverName + DOIP_SIMULATION_PATH + "platform/" + platform.name ;
		// Update platform URL using the current server name
		platform.url = currentPlatformUrl;
		
		platform.status = status;

		// Create a Gateway
		Gateway gateway = new Gateway();
		gateway.name = "GW";
		
		//gateway.url = "http://myserver.com/doip-simulation/platform/X2024/gateway/GW";
		String currentGatewayUrl = currentPlatformUrl + "/gateway/" + gateway.name ;
		gateway.url = currentGatewayUrl;
		
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
