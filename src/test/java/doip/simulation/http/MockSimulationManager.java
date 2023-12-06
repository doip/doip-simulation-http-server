package doip.simulation.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import doip.simulation.api.SimulationManager;
import doip.library.util.LookupTable;
import doip.simulation.api.Ecu;
import doip.simulation.api.Gateway;
import doip.simulation.api.Platform;
import doip.simulation.api.ServiceState;
/*
public class MockSimulationManager implements SimulationManager {
	
	private static Logger logger = LogManager.getLogger(MockSimulationManager.class);

    private List<Platform> platforms;

    public MockSimulationManager() {
        this.platforms = new ArrayList<>();
        // Initialize with some mock platforms
        Platform platform1 = new MockPlatform("X2024");
        Platform platform2 = new MockPlatform("Platform1");
        Platform platform3 = new MockPlatform("Platform2");
        platforms.add(platform1);
        platforms.add(platform2);
        platforms.add(platform3);
    }

    @Override
    public void start(String platform) {
        // Implement the start logic for a specific platform
    }

    @Override
    public void start(String platform, String host) {
        // Implement the start logic for a specific platform with a given host
    }

    @Override
    public void stop() {
        // Implement the stop logic
    }

    @Override
    public Platform getPlatformByName(String name) {
        // Implement the logic to retrieve a platform by name
        return platforms.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Platform> getPlatforms() {
        // Return the list of mock platforms
        return platforms;
    }
}

class MockPlatform implements Platform {

    private String name;
    private List<Gateway> gateways;

    public MockPlatform(String name) {
        this.name = name;
        this.gateways = new ArrayList<>();
        // Initialize with some mock gateways
        Gateway gateway1 = new MockGateway("GW");
        Gateway gateway2 = new MockGateway("Gateway1");
        Gateway gateway3 = new MockGateway("Gateway2");
        gateways.add(gateway1);
        gateways.add(gateway2);
        gateways.add(gateway3);
    }

    @Override
    public void start() {
        // Implement the start logic for the platform
    }

    @Override
    public void stop() {
        // Implement the stop logic for the platform
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ServiceState getState() {
        // Implement the logic to retrieve the state of the platform
        return ServiceState.RUNNING; // Mocked state
    }

    @Override
    public Gateway getGatewayByName(String name) {
        // Implement the logic to retrieve a gateway by name
        return gateways.stream()
                .filter(g -> g.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Gateway> getGateways() {
        // Return the list of mock gateways
        return gateways;
    }
}

class MockGateway implements Gateway {

    private String name;
    private List<Ecu> ecus;

    public MockGateway(String name) {
        this.name = name;
        this.ecus = new ArrayList<>();
        // Initialize with some mock ECUs
        Ecu ecu1 = new MockEcu("EMS");
        Ecu ecu2 = new MockEcu("Ecu1");
        Ecu ecu3 = new MockEcu("Ecu2");
        ecus.add(ecu1);
        ecus.add(ecu2);
        ecus.add(ecu3);
    }

    @Override
    public void start() throws IOException {
        // Implement the start logic for the gateway
    }

    @Override
    public void stop() {
        // Implement the stop logic for the gateway
    }

    @Override
    public ServiceState getState() {
        // Implement the logic to retrieve the state of the gateway
        return ServiceState.RUNNING; // Mocked state
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Ecu getEcuByName(String name) {
        // Implement the logic to retrieve an ECU by name
        return ecus.stream()
                .filter(e -> e.getNamme().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Ecu> getEcus() {
        // Return the list of mock ECUs
        return ecus;
    }
}

class MockEcu implements Ecu {

    private String name;

    public MockEcu(String name) {
        this.name = name;
    }

	@Override
	public String getNamme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LookupTable getConfiguredLookupTable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LookupTable getRuntimeLookupTable() {
		// TODO Auto-generated method stub
		return null;
	}

    // Implement the ECU methods
    // ...
}
*/
