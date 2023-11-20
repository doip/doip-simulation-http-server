package doip.simulation.http;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import com.starcode88.http.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import doip.simulation.http.helpers.HttpServerHelper;
import doip.simulation.http.lib.Gateway;
import doip.simulation.http.lib.Platform;
import doip.simulation.http.lib.ServerInfo;

//Define a handler for the "/doip-simulation/platform" path
public class GetPlatformOverviewHandler implements HttpHandler {
	private final DoipHttpServer doipHttpServer;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// Constructor to receive the DoipHttpServer instance
	public GetPlatformOverviewHandler(DoipHttpServer doipHttpServer) {
		this.doipHttpServer = doipHttpServer;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("GET".equals(exchange.getRequestMethod())) {
			handleGetRequest(exchange);
		} else {
			// Respond with 405 Method Not Allowed for non-GET requests
			exchange.sendResponseHeaders(405, -1);
		}
	}

	private void handleGetRequest(HttpExchange exchange) throws IOException {
		try {
			// Extract platform parameter from the path
			String requestPath = exchange.getRequestURI().getPath();
			String platformParam = getPlatformParam(requestPath, "platform");

			// Create the GET response
			//String jsonResponse = "This is a GET request for platform: " + platformParam;
			// Build the JSON response 
			String jsonResponse = buildJsonResponse();

			// Set the response headers and body
			HttpServerHelper.sendResponse(exchange, jsonResponse, "application/json", 200);
			HttpServerHelper.responseServerLogging(exchange, 200, jsonResponse);

		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
		}
	}

	// Helper method to extract the platform parameter from the path
	private String getPlatformParam(String path, String paramName) {
		// Split the path into segments
		String[] segments = path.split("/");

		// Find the segment after "platform"
		for (int i = 0; i < segments.length - 1; i++) {
			if (paramName.equals(segments[i])) {
				return segments[i + 1];
			}
		}
		return null;
	}
	private String buildJsonResponse() throws IOException {

		// TODO !!!
		// doipHttpServer.getSimulationManager().getPlatforms();

		Platform platformInfo = createSampleJson();

		// Convert the ServerInfo object to JSON
		return objectMapper.writeValueAsString(platformInfo);
	}
	
	private Platform createSampleJson() {
		
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

}
