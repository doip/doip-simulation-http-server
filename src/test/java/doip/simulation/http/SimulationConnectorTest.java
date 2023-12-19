package doip.simulation.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import doip.simulation.api.SimulationManager;
import doip.simulation.http.lib.ActionRequest;
import doip.simulation.http.lib.Ecu;
import doip.simulation.http.lib.Gateway;
import doip.simulation.http.lib.LookupEntry;
import doip.simulation.http.lib.Modifier;
import doip.simulation.http.lib.Platform;
import doip.simulation.http.lib.ServerInfo;

public class SimulationConnectorTest extends SimulationConnector {
	private static final Logger logger = LogManager.getLogger(SimulationConnectorTest.class);
	private boolean createMockResponse = true;

	public SimulationConnectorTest(SimulationManager simulationManager, String alternativeHostName) {
		// super(doipHttpServer.getSimulationManager(), doipHttpServer.getServerName());
		super(simulationManager, alternativeHostName);
	}

	@Override
	public SimulationResponse handlePlatformAction(String platformParam, ActionRequest receivedAction)
			throws IOException {
		// Retrieve the platform based on the specified platform name
		doip.simulation.api.Platform platform = getPlatformByName(platformParam);

		if (platform == null) {
			// Log an error if the specified platform is not found
			logger.error("Action cannot be executed because the specified platform name {} does not exist",
					platformParam);
			String errorMessage = String.format("The specified platform name %s does not exist", platformParam);
			// return new SimulationResponse(HttpURLConnection.HTTP_NOT_FOUND,
			// buildJsonErrorResponse(errorMessage));
			return new SimulationResponse(HttpURLConnection.HTTP_OK, buildJsonErrorResponse(errorMessage));
		} else {
			return performAction(platform, receivedAction.getAction());
		}
	}

	@Override
	public SimulationResponse buildOverviewJsonResponse(String status) throws IOException {
		try {
			// Retrieve platform overview based on the status
			List<doip.simulation.api.Platform> platforms = getPlatformOverview(status);

			if (platforms == null) {
				// Log an error if platform overview retrieval fails. Check logs for details.
				logger.error("Failed to retrieve platform overview.");
				String errorMessage = "Failed to retrieve platform overview";
				logger.error(errorMessage);
				if (createMockResponse == false) {
					return new SimulationResponse(HttpURLConnection.HTTP_NOT_FOUND,
							buildJsonErrorResponse(errorMessage)); // "{}" Return an empty JSON object or handle it as
																	// needed
				}
			}
			ServerInfo serverInfo;
			if (createMockResponse) {
				// Create ServerInfo for platforms
				serverInfo = createSampleJson(platforms, status);
			} else {
				// Create a real JSON object Platform
				serverInfo = processOverview(platforms, status);
			}
			// Process the retrieved platforms if needed
//			for (doip.simulation.api.Platform platform : platforms) {
//				// Do something with each platform if needed
//			}

			// Convert the ServerInfo object to JSON
			return new SimulationResponse(HttpURLConnection.HTTP_OK, buildJsonResponse(serverInfo));
		} catch (Exception e) {
			// Log an error and return an empty JSON object in case of an exception
			String errorMessage = "Error building overview JSON response: " + e.getMessage();
			logger.error(errorMessage, e);
			return new SimulationResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, buildJsonErrorResponse(errorMessage));
		}
	}

	// Method to build a sample JSON response
	private ServerInfo createSampleJson(List<doip.simulation.api.Platform> platforms, String status) {
		// Get the server name from the DoipHttpServer
		// String serverName = doipHttpServer.getServerName();
		String serverName = getServerNameFromRequestHeader();

		// Build a JSON response based on the specified 'status'
		ServerInfo serverInfo = new ServerInfo();

		// Create a Platform
		Platform platform = new Platform();
		platform.setName("X2024");

		// platform.url = "http://myserver.com/doip-simulation/platform/X2024";
		String currentPlatformUrl = serverName + SimulationConnector.PLATFORM_PATH + "/" + platform.getName();
		// Update platform URL using the current server name
		platform.setUrl(currentPlatformUrl);

		platform.setStatus(status);

		// Create a Gateway
		Gateway gateway = new Gateway();
		gateway.name = "GW";

		// gateway.url =
		// "http://myserver.com/doip-simulation/platform/X2024/gateway/GW";
		String currentGatewayUrl = currentPlatformUrl + "/gateway/" + gateway.name;
		gateway.url = currentGatewayUrl;

		gateway.status = status;

		// Add error information for the gateway (if applicable)
		if ("ERROR".equals(status)) {
			gateway.error = "Can't bind to port 13400 because it is already used by another gateway";
		}

		// Add the gateway to the platform's gateways list
		platform.setGateways(List.of(gateway));

		// Add the platform to the serverInfo's platforms list
		serverInfo.platforms = List.of(platform);

		return serverInfo;
	}

	@Override
	public SimulationResponse buildPlatformJsonResponse(String platformName) throws IOException {
		try {
			// Retrieve the platform based on the specified platform name
			doip.simulation.api.Platform platform = getPlatformByName(platformName);

			if (platform == null) {
				// Log an error if the specified platform is not found
				String errorMessage = String.format("The specified platform name {} does not exist", platformName);
				logger.error(errorMessage);
				if (createMockResponse == false) {
					return new SimulationResponse(HttpURLConnection.HTTP_NOT_FOUND,
							buildJsonErrorResponse(errorMessage)); // "{}"; // Return an empty JSON object or handle it
																	// as needed
				}
			}

			Platform platformInfo;
			if (createMockResponse) {
				platformInfo = createPlatformSampleJson(platform);
			} else {
				// Process the retrieved platform and create a real JSON object Platform
				platformInfo = processPlatform(platform);
			}
			// Convert the object to JSON
			String jsonResponse = buildJsonResponse(platformInfo);
			return new SimulationResponse(HttpURLConnection.HTTP_OK, jsonResponse);
		} catch (Exception e) {
			// Log an error and return an empty JSON object in case of an exception
			// logger.error("Error building platform JSON response: {}", e.getMessage(), e);
			// return "{}";
			String errorMessage = "Error building platform JSON response: " + e.getMessage();
			logger.error(errorMessage, e);
			return new SimulationResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, buildJsonErrorResponse(errorMessage));
		}
	}

	@Override
	public SimulationResponse buildGatewayJsonResponse(String platformName, String gatewayName) throws IOException {
		try {
			// Retrieve the gateway based on the specified platform and gateway names
			doip.simulation.api.Gateway gateway = getGatewayByName(platformName, gatewayName);

			if (gateway == null) {
				// Log an error if the specified gateway is not found
				String errorMessage = String.format("The specified gateway name {} does not exist", gatewayName);
				logger.error(errorMessage);
				if (createMockResponse == false) {
					return new SimulationResponse(HttpURLConnection.HTTP_NOT_FOUND,
							buildJsonErrorResponse(errorMessage)); // "{}" Return an empty JSON object or handle it as
																	// needed
				}
			}

			Gateway gatewayInfo;
			if (createMockResponse) {
				gatewayInfo = createGatewaySampleJson(gateway, platformName);
			} else {
				// Process the retrieved gateway and create a real JSON object Gateway
				gatewayInfo = processGateway(gateway, platformName);
			}

			// Convert the object to JSON
			String jsonResponse = buildJsonResponse(gatewayInfo);
			return new SimulationResponse(HttpURLConnection.HTTP_OK, jsonResponse);
		} catch (Exception e) {
			// Log an error and return an empty JSON object in case of an exception
			// logger.error("Error building gateway JSON response: {}", e.getMessage(), e);
			// return "{}";
			String errorMessage = "Error building gateway JSON response: " + e.getMessage();
			logger.error(errorMessage, e);
			return new SimulationResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, buildJsonErrorResponse(errorMessage));
		}
	}

	private Platform createPlatformSampleJson(doip.simulation.api.Platform platformCurrent) {
		// Get the server name from the DoipHttpServer
		// String serverName = doipHttpServer.getServerName();
		String serverName = getServerNameFromRequestHeader();

		// Create a Platform
		Platform platform = new Platform();
		platform.setName("X2024");
		// platform.url = "http://myserver.com/doip-simulation/platform/X2024";
		String currentPlatformUrl = serverName + SimulationConnector.PLATFORM_PATH + "/" + platform.getName();
		// Update platform URL using the current server name
		platform.setUrl(currentPlatformUrl);

		platform.setStatus("RUNNING");

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
		platform.setGateways(List.of(gateway));

		return platform;
	}

	private Gateway createGatewaySampleJson(doip.simulation.api.Gateway gatewayCurrent, String platformName) {

		// Get the server name from the DoipHttpServer
		// String serverName = doipHttpServer.getServerName();
		String serverName = getServerNameFromRequestHeader();

		// Create an instance of your classes and populate them with data
		Gateway gateway = new Gateway();
		gateway.name = "GW";

		// gateway.url =
		// "http://myserver.com/doip-simulation/platform/X2024/gateway/GW";
		String currentPlatformUrl = serverName + SimulationConnector.PLATFORM_PATH + "/" + platformName;
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
