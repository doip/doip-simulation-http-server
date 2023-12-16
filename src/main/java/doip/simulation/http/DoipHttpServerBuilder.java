package doip.simulation.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import doip.library.exception.DoipException;
import doip.library.properties.EmptyPropertyValue;
import doip.library.properties.MissingProperty;
import doip.simulation.PlatformConfig;
import doip.simulation.api.Platform;
import doip.simulation.standard.StandardPlatform;
import doip.simulation.standard.StandardSimulationManager;

public class DoipHttpServerBuilder {
	
	private static Logger logger = LogManager.getLogger(DoipHttpServerBuilder.class);
	
	private List<doip.simulation.api.Platform> platforms = new ArrayList<>();
	
	public static DoipHttpServerBuilder newBuilder() {
		String method = "public static DoipHttpServerBuilder newBuilder()";
		logger.trace(">>> {}", method);
		DoipHttpServerBuilder builder = new DoipHttpServerBuilder();
		logger.trace("<<< {}", method);
		return builder;
	}
	
	public DoipHttpServerBuilder addPlatform(Platform platform) {
		String method = "public DoipHttpServerBuilder addPlatform(Platform platform)";
		logger.trace(">>> {}", method);
		this.platforms.add(platform);
		logger.trace("<<< {}", method);
		return this;
	}
	
	public DoipHttpServerBuilder addPlatform(PlatformConfig config) throws DoipException {
		String method = "public DoipHttpServerBuilder addPlatform(PlatformConfig config)";
		try {
			logger.trace(">>> {}", method);
			Platform platform = createPlatform(config);
			this.addPlatform(platform);
		} finally {
			logger.trace("<<< {}", method);
		}
		return this;
	}
	
	public DoipHttpServerBuilder addPlatform(String filename) throws IOException, MissingProperty, EmptyPropertyValue, DoipException {
		String method = "public DoipHttpServerBuilder addPlatform(String filename)";
		try {
			logger.trace(">>> {}", method);
			PlatformConfig config = new PlatformConfig();
			config.loadFromFile(filename);
			this.addPlatform(config);
		} finally {
			logger.trace("<<< {}", method);
		}
		return this;
	}
	
	/**
	 * Creates a new instance of StandardPlatform. This method can be overridden
	 * when a different platform shall be created instead of StandardPlatorm.
	 * @param config
	 * @return
	 * @throws DoipException
	 */
	public Platform createPlatform(PlatformConfig config) throws DoipException {
		String method = "public Platform createPlatform(PlatformConfig config)";
		try {
			logger.trace(">>> {}", method);
			return new StandardPlatform(config);
		} finally {
			logger.trace("<<< {}", method);
		}
	}

	public DoipHttpServer build() throws IOException {
		String method = "public DoipHttpServer build()";
		DoipHttpServer server = null;
		try {
			logger.trace(">>> {}", method);
			
			StandardSimulationManager simulationManager = new StandardSimulationManager();
			for (Platform platform : this.platforms) {
				simulationManager.addPlatform(platform);
			}
			SimulationConnector simulationConnector = new SimulationConnector(simulationManager, "http://localhost:8080");
			SimulationOverviewHandler rootHandler = new SimulationOverviewHandler(simulationConnector);
			PlatformOverviewHandler platformHandler = new PlatformOverviewHandler(simulationConnector);
			
			server = new DoipHttpServer(simulationManager);
			server.addMappingContext(SimulationOverviewHandler.RESOURCE_PATH, rootHandler);
			server.addMappingContext(PlatformOverviewHandler.RESOURCE_PATH, platformHandler);
		} finally {
			logger.trace("<<< {}", method);
		}
		return server;
	}
}
