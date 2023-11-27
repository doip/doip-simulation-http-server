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

	/**
	 * Build a JSON response for the overview of platforms based on the specified status.
	 *
	 * @param status The status parameter.
	 * @return The JSON response as a string.
	 * @throws IOException If an I/O error occurs during the process.
	 */
	protected String buildOverviewJsonResponse(String status) throws IOException {
	    try {
	        // Initialize ServerInfo to hold platform overview
	        ServerInfo serverInfo = new ServerInfo();

	        // Retrieve platform overview based on the status
	        List<doip.simulation.api.Platform> platforms = getPlatformOverview(status);

	        if (platforms == null) {
	            // Log an error if platform overview retrieval fails
	            logger.error("Failed to retrieve platform overview. Check logs for details.");
	            return "{}"; // Return an empty JSON object or handle it as needed
	        }

	        // Process the retrieved platforms and populate serverInfo
	        for (doip.simulation.api.Platform platform : platforms) {
	            // Do something with each platform if needed
	        }

	        // Convert the ServerInfo object to JSON
	        return buildJsonResponse(serverInfo);
	    } catch (Exception e) {
	        // Log an error and return an empty JSON object in case of an exception
	        logger.error("Error building overview JSON response: {}", e.getMessage(), e);
	        return "{}";
	    }
	}

	/**
	 * Build a JSON response for a specific platform based on the specified platform name.
	 *
	 * @param platformName The name of the platform.
	 * @return The JSON response as a string.
	 * @throws IOException If an I/O error occurs during the process.
	 */
	protected String buildPlatformJsonResponse(String platformName) throws IOException {
	    try {
	        // Retrieve the platform based on the specified platform name
	        doip.simulation.api.Platform platform = getPlatformByName(platformName);

	        if (platform == null) {
	            // Log an error if the specified platform is not found
	            logger.error("The specified platform name {} does not exist", platformName);
	            return "{}"; // Return an empty JSON object or handle it as needed!
	        }

	        // Process the retrieved platform and create a real JSON object Platform
	        doip.simulation.http.lib.Platform platformInfo = processPlatform(platform);

	        // Convert the object to JSON
	        return buildJsonResponse(platformInfo);
	    } catch (Exception e) {
	        // Log an error and return an empty JSON object in case of an exception
	        logger.error("Error building platform JSON response: {}", e.getMessage(), e);
	        return "{}";
	    }
	}

	/**
	 * Build a JSON response for a specific gateway based on the specified platform and gateway names.
	 *
	 * @param platformName The name of the platform.
	 * @param gatewayName  The name of the gateway.
	 * @return The JSON response as a string.
	 * @throws IOException If an I/O error occurs during the process.
	 */
	protected String buildGatewayJsonResponse(String platformName, String gatewayName) throws IOException {
	    try {
	        // Retrieve the gateway based on the specified platform and gateway names
	        doip.simulation.api.Gateway gateway = getGatewayByName(platformName, gatewayName);

	        if (gateway == null) {
	            // Log an error if the specified gateway is not found
	            logger.error("The specified gateway name {} does not exist", gatewayName);
	            return "{}"; // Return an empty JSON object or handle it as needed
	        }

	        // Process the retrieved gateway and create a real JSON object Gateway
	        doip.simulation.http.lib.Gateway gatewayInfo = processGateway(gateway);

	        // Convert the object to JSON
	        return buildJsonResponse(gatewayInfo);
	    } catch (Exception e) {
	        // Log an error and return an empty JSON object in case of an exception
	        logger.error("Error building gateway JSON response: {}", e.getMessage(), e);
	        return "{}";
	    }
	}

	
	// Additional methods to process Platform and Gateway objects
	private doip.simulation.http.lib.Platform processPlatform(doip.simulation.api.Platform platform) {
	    // Implement the logic to process the platform and create a doip.simulation.http.lib.Platform object
		//TODO:
	    return new doip.simulation.http.lib.Platform();
	}

	private doip.simulation.http.lib.Gateway processGateway(doip.simulation.api.Gateway gateway) {
	    // Implement the logic to process the gateway and create a doip.simulation.http.lib.Gateway object
		//TODO:
	    return new doip.simulation.http.lib.Gateway();
	}
	
	public String buildJsonResponse(Object info) throws IOException {
		return objectMapper.writeValueAsString(info);
	}


}