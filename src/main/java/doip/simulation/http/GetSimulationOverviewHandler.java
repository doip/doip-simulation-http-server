package doip.simulation.http;

import java.io.IOException;

import com.starcode88.http.HttpUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import doip.simulation.http.helpers.HttpServerHelper;

import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetSimulationOverviewHandler implements HttpHandler {
	private final DoipHttpServer doipHttpServer;
	private final ObjectMapper objectMapper = new ObjectMapper();

	// Constructor to receive the DoipHttpServer instance
	public GetSimulationOverviewHandler(DoipHttpServer doipHttpServer) {
		this.doipHttpServer = doipHttpServer;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Gateway {
		public String name;
		public String url;
		public String status;
		public String error;
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ServerInfo {
		public String name;
		public String url;
		public String status;
		public List<Gateway> gateways;
	}

	public enum SimulationStatus {
		RUNNING, STOPPED, ERROR
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

			// Set response headers
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, jsonResponse.length());

			// Write the JSON response to the output stream
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(jsonResponse.getBytes());
			}
		} catch (Exception e) {
			// Handle exceptions and send an appropriate response
			exchange.sendResponseHeaders(500, -1); // Internal Server Error
		}
	}

//	    private boolean isValidStatus(String status) {
//	        // Validate the 'status' parameter against the allowed values
//	        return status.equals("RUNNING") || status.equals("STOPPED") || status.equals("ERROR");
//	    }

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
		// Build a JSON response based on the specified 'status'
		ServerInfo serverInfo = new ServerInfo();
		serverInfo.name = "X2024";
		serverInfo.url = "http://myserver.com/doip-simulation/platform/X2024";
		serverInfo.status = status;

		// Add gateway information
		Gateway gateway = new Gateway();
		gateway.name = "string";
		gateway.url = "http://myserver.com/doip-simulation/platform/X2024/gateway/GW";
		gateway.status = status;

		// Add error information for the gateway (if applicable)
		if ("ERROR".equals(status)) {
			gateway.error = "Can't bind to port 13400 because it is already used by another gateway";
		}

		serverInfo.gateways = Collections.singletonList(gateway);

		// Convert the ServerInfo object to JSON
		return objectMapper.writeValueAsString(serverInfo);
	}

}
