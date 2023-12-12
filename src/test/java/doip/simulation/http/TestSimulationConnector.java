package doip.simulation.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;


import doip.simulation.http.lib.Action;
import doip.simulation.http.lib.ActionRequest;
import doip.simulation.api.ServiceState;
import doip.simulation.api.SimulationManager;

class TestSimulationConnector {
	private static Logger logger = LogManager.getLogger(TestSimulationConnector.class);

	private static SimulationConnector connector = null;
	
	//mock platform
	private static final String platformName = "X2024";
	private static doip.simulation.api.Platform mockPlatform  = null;
	
	private static final String GatewayName = "GW";
	private static doip.simulation.api.Gateway mockGateway  = null;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		SimulationManager mockSimulationManager = new MockSimulationManager();
		
		// Create a mock instance of SimulationManager
		//SimulationManager mockSimulationManager = mock(SimulationManager.class);
		connector = new SimulationConnector(mockSimulationManager, "http://localhost:8080");
		
		//createMockitos();
	}

	@SuppressWarnings("unused")
	private static void createMockitos() {
		// Create a mock platform
        mockPlatform = mock(doip.simulation.api.Platform.class);
        when(connector.getPlatformByName(platformName)).thenReturn(mockPlatform);
        when(mockPlatform.getState()).thenReturn(ServiceState.RUNNING);
        
        // Create a mock gateway
        mockGateway = mock(doip.simulation.api.Gateway.class);
        when(mockPlatform.getGatewayByName(GatewayName)).thenReturn(mockGateway);
        when(mockGateway.getState()).thenReturn(ServiceState.RUNNING);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		connector = null;
	}

	@Test
    public void testBuildOverviewJsonResponse() {
		logger.info("-------------------------- testBuildOverviewJsonResponse ------------------------------------");
        // Call the method being tested
        String jsonResponse = null;
		try {
			jsonResponse = connector.buildOverviewJsonResponse("RUNNING");
		} catch (IOException e) {
			logger.error("Unexpected IOException: " + e.getMessage(), e);
	        fail("Unexpected IOException: " + e.getMessage());
		}

        assertNotNull(jsonResponse);
        logger.info(jsonResponse);
    }

    @Test
    public void testBuildPlatformJsonResponse() {
    	logger.info("-------------------------- testBuildPlatformJsonResponse ------------------------------------");
       // Call the method being tested
        String jsonResponse = null;
		try {
			jsonResponse = connector.buildPlatformJsonResponse(platformName);
		} catch (IOException e) {
			logger.error("Unexpected IOException: " + e.getMessage(), e);
	        fail("Unexpected IOException: " + e.getMessage());
		}
        assertNotNull(jsonResponse);
        logger.info(jsonResponse);
    }

    @Test
    public void testBuildGatewayJsonResponse(){
    	logger.info("-------------------------- testBuildGatewayJsonResponse ------------------------------------");

        String jsonResponse = null;
		try {
			jsonResponse = connector.buildGatewayJsonResponse(platformName, GatewayName);
		} catch (IOException e) {
			//throw logger.throwing(e);
			logger.error("Unexpected IOException: " + e.getMessage(), e);
	        fail("Unexpected IOException: " + e.getMessage());
		}

        assertNotNull(jsonResponse);
        logger.info(jsonResponse);
        
    }
    
    @Test
    public void testHandlePlatformAction() {
    	logger.info("-------------------------- testHandlePlatformAction ------------------------------------");
    	//Create an action request
        ActionRequest actionRequest = new ActionRequest();
        actionRequest.setAction(Action.start); // or Action.stop

        // Call the method being tested
        connector.handlePlatformAction(platformName, actionRequest);

        // Verify that the performAction method is called with the correct arguments
        //verify(connector,times(1)).performAction(mockPlatform, Action.start); // or Action.stop
    }

}
