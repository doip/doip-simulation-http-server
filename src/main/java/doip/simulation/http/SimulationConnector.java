package doip.simulation.http;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import doip.simulation.api.Platform;
import doip.simulation.api.Gateway;
import doip.simulation.api.SimulationManager;
import doip.simulation.http.lib.ServerInfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimulationConnector {
	private static final Logger logger = LogManager.getLogger(SimulationConnector.class);

	protected SimulationManager simulationManager;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public SimulationConnector(SimulationManager simulationManager) {
		this.simulationManager = simulationManager;
	}

	/**
	 * Retrieve an overview of platforms.
	 *
	 * @param status The status parameter.
	 * @return A list of platforms.
	 * @throws IOException If an I/O error occurs during platform retrieval.
	 */
	protected List<Platform> getPlatformOverview(String status) throws IOException {
		List<Platform> platforms = null;
		try {
			// Access simulationManager
			platforms = simulationManager.getPlatforms();
			logger.info("Retrieved platform overview. Total platforms: {}", platforms.size());
			for (Platform platform : platforms) {
				logger.info("Platform: {}, State: {}", platform.getName(), platform.getState());
			}
		} catch (Exception e) {
			// Log the error and rethrow the exception
			logger.error("Error retrieving platform overview: {}", e.getMessage(), e);
			// throw e;
		}
		return platforms;
	}

	/**
	 * Retrieve a platform by name.
	 *
	 * @param platformName The name of the platform.
	 * @return The platform if found, otherwise null.
	 */
	protected Platform getPlatformByName(String platformName) {
		Platform platform = null;
		try {
			// Attempt to retrieve the platform
			platform = simulationManager.getPlatformByName(platformName);
			if (platform != null) {
				logger.info("Platform: {}, State: {}", platform.getName(), platform.getState());
			} else {
				// Log a warning if the platform is not found
				logger.warn("Platform: {} not found", platformName);
			}
		} catch (Exception e) {
			// Log the error and return null
			logger.error("Error retrieving platform by name: {}", e.getMessage(), e);
		}
		return platform;
	}

	/**
	 * Retrieve a gateway by name for a specific platform.
	 *
	 * @param platformName The name of the platform.
	 * @param gatewayName  The name of the gateway.
	 * @return The gateway if found, otherwise null.
	 */
	protected Gateway getGatewayByName(String platformName, String gatewayName) {
		Gateway gateway = null;
		try {
			// Check if the platform exists
			Platform platform = this.getPlatformByName(platformName);
			if (platform != null) {
				// Attempt to retrieve the gateway from the platform
				gateway = platform.getGatewayByName(gatewayName);
				if (gateway != null) {
					logger.info("Gateway: {}, State: {}", gateway.getName(), gateway.getState());
				} else {
					// Log a warning if the gateway is not found
					logger.warn("Gateway: {} for Platform {} not found", gatewayName, platformName);
				}
			}
		} catch (Exception e) {
			// Log the error and return null
			logger.error("Error retrieving gateway by name: {}", e.getMessage(), e);
		}
		return gateway;
	}

	protected String buildOverviewJsonResponse(String status) throws IOException {

		List<doip.simulation.api.Platform> platforms = getPlatformOverview(status);

		if (platforms == null) {

			// Handle the case where platforms is null
			logger.error("Failed to retrieve platform overview. Check logs for details.");
			return "{}"; // Return an empty JSON object or handle it as needed
		}

		// Create ServerInfo for platforms

		// Build a JSON response based on the specified 'status'
		ServerInfo serverInfo = new ServerInfo();

		// Process the retrieved platforms
		for (doip.simulation.api.Platform platform : platforms) {
			// Do something with the platform
		}

		// Convert the ServerInfo object to JSON
		return buildJsonResponse(serverInfo);
	}

	protected String buildPlatformJsonResponse(String platformName) throws IOException {

		doip.simulation.api.Platform platform = getPlatformByName(platformName);

		if (platform == null) {
		
			// Handle the case where platform is null
			logger.error("The specified platform name {} does not exist", platformName);
			return "{}"; // Return an empty JSON object or handle it as needed!
		}
		
		// Create real JSON object Platform
		// Process the retrieved platform
		
		doip.simulation.http.lib.Platform platformInfo = new doip.simulation.http.lib.Platform();

		// Convert the object to JSON
		return buildJsonResponse(platformInfo);
	}

	protected String buildGatewayJsonResponse(String platformName, String gatewayName) throws IOException {

		doip.simulation.api.Gateway gateway = getGatewayByName(platformName, gatewayName);

		if (gateway == null) {

		} else {
			// Handle the case where gateway is null
			logger.error("The specified gateway name {} does not exist", gatewayName);
			return "{}"; // Return an empty JSON object or handle it as needed
		}

		// Create real JSON object Gateway
		// Process the retrieved gateway
		doip.simulation.http.lib.Gateway gatewayInfo = new doip.simulation.http.lib.Gateway();

		// Convert the object to JSON
		return buildJsonResponse(gatewayInfo);
	}
	
	public String buildJsonResponse(Object info) throws IOException {
		return objectMapper.writeValueAsString(info);
	}


}
