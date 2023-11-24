package doip.simulation.http;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import doip.simulation.api.Platform;
import doip.simulation.api.Gateway;
import doip.simulation.api.SimulationManager;

public class SimulationConnector {
	private static Logger logger = LogManager.getLogger(SimulationConnector.class);

	protected SimulationManager simulationManager;

	public SimulationConnector(SimulationManager simulationManager) {
		this.simulationManager = simulationManager;
	}

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
			logger.error("Error retrieving platform overview: {}", e.getMessage(), e);
			// throw e; // Rethrow the exception or handle it as needed
		}
		return platforms;
	}

	protected Platform getPlatformByName(String platformName) {
		Platform platform = simulationManager.getPlatformByName(platformName);
		if (platform != null) {
			logger.info("Platform: {}, State: {}", platform.getName(), platform.getState());
		} else {
			logger.warn("Platform: {} not found", platformName);
		}
		return platform;
	}

	protected Gateway getGatewayByName(String platformName, String gatewayName) {
		Gateway gataway = null;
		if (this.getPlatformByName(platformName) != null) {
			gataway = simulationManager.getPlatformByName(platformName).getGatewayByName(gatewayName);
			if (gataway != null) {
				logger.info("Gateway: {}, State: {}", gataway.getName(), gataway.getState());
			} else {
				logger.warn("Gateway: {} for Platform {} not found", gatewayName, platformName);
			}
		}
		return gataway;
	}

}
