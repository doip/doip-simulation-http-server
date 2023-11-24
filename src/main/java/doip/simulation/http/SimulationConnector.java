package doip.simulation.http;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import doip.simulation.api.Platform;
import doip.simulation.api.Gateway;
import doip.simulation.api.SimulationManager;

public class SimulationConnector {
	private static final Logger logger = LogManager.getLogger(SimulationConnector.class);

	protected SimulationManager simulationManager;

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
			//throw e;
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
}
