package doip.simulation.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import doip.simulation.http.lib.Action;
import doip.simulation.http.lib.ActionRequest;
import doip.simulation.api.ServiceState;
import doip.simulation.api.SimulationManager;

class TestSimulationConnector {
	private static Logger logger = LogManager.getLogger(TestSimulationConnector.class);

	private static SimulationConnector connector = null;
	private static SimulationConnector connectorMockito = null;

	// mock platform
	private static final String platformName = "X2024";
	private static doip.simulation.api.Platform mockPlatform = null;

	private static final String GatewayName = "GW";
	private static doip.simulation.api.Gateway mockGateway = null;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		SimulationManager mockSimulationManager = new MockSimulationManager();

		// Create a mock instance of SimulationManager
		// SimulationManager mockSimulationManager = mock(SimulationManager.class);
		connector = new SimulationConnector(mockSimulationManager, "http://localhost:8080");

		SimulationManager stubSimulationManager = Mockito.mock(SimulationManager.class);
		connectorMockito = new SimulationConnector(stubSimulationManager, "http://localhost:8080");

		// createMockitos();
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
	public void testBuildOverviewJsonResponseWithStub() {
		logger.info(
				"-------------------------- testBuildOverviewJsonResponseWithStub() ------------------------------------");
		// Call the method being tested
		String jsonResponse = null;
		try {
			jsonResponse = connectorMockito.buildOverviewJsonResponse("RUNNING");
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
	public void testBuildPlatformJsonResponseWithStub() {
		logger.info(
				"-------------------------- testBuildPlatformJsonResponseWithStub ------------------------------------");
		// Call the method being tested
		String jsonResponse = null;
		try {
			jsonResponse = connectorMockito.buildPlatformJsonResponse(platformName);
		} catch (IOException e) {
			logger.error("Unexpected IOException: " + e.getMessage(), e);
			fail("Unexpected IOException: " + e.getMessage());
		}
		assertNotNull(jsonResponse);
		logger.info(jsonResponse);
	}

	@Test
	public void testBuildJsonResponseUnknownPlatform() {
		logger.info(
				"-------------------------- testBuildJsonResponseUnknownPlatform( ------------------------------------");
		// Call the method being tested
		String jsonResponse = null;
		try {
			jsonResponse = connector.buildPlatformJsonResponse("Unknown");
		} catch (IOException e) {
			logger.error("Unexpected IOException: " + e.getMessage(), e);
			fail("Unexpected IOException: " + e.getMessage());
		}
		assertNotNull(jsonResponse);
		logger.info(jsonResponse);
	}

	@Test
	public void testBuildGatewayJsonResponse() {
		logger.info("-------------------------- testBuildGatewayJsonResponse ------------------------------------");

		String jsonResponse = null;
		try {
			jsonResponse = connector.buildGatewayJsonResponse(platformName, GatewayName);
		} catch (IOException e) {
			// throw logger.throwing(e);
			logger.error("Unexpected IOException: " + e.getMessage(), e);
			fail("Unexpected IOException: " + e.getMessage());
		}

		assertNotNull(jsonResponse);
		logger.info(jsonResponse);

	}
	
	@Test
	public void testBuildGatewayJsonResponseWithStub() {
		logger.info("-------------------------- testBuildGatewayJsonResponseWithStub ------------------------------------");

		String jsonResponse = null;
		try {
			jsonResponse = connectorMockito.buildGatewayJsonResponse(platformName, GatewayName);
		} catch (IOException e) {
			// throw logger.throwing(e);
			logger.error("Unexpected IOException: " + e.getMessage(), e);
			fail("Unexpected IOException: " + e.getMessage());
		}

		assertNotNull(jsonResponse);
		logger.info(jsonResponse);

	}

	@Test
	public void testBuildJsonResponseUnknownGateway() {
		logger.info(
				"-------------------------- testBuildJsonResponseUnknownGateway ------------------------------------");

		String jsonResponse = null;
		try {
			jsonResponse = connector.buildGatewayJsonResponse(platformName, "Unknown");
		} catch (IOException e) {
			// throw logger.throwing(e);
			logger.error("Unexpected IOException: " + e.getMessage(), e);
			fail("Unexpected IOException: " + e.getMessage());
		}

		assertNotNull(jsonResponse);
		logger.info(jsonResponse);

	}

	@Test
	public void testHandlePlatformAction() {
		logger.info("-------------------------- testHandlePlatformAction ------------------------------------");
		// Create an action request
		ActionRequest actionRequest = new ActionRequest();
		actionRequest.setAction(Action.start); // or Action.stop
		try {
			// Call the method being tested
			connector.handlePlatformAction(platformName, actionRequest);
		} catch (Exception e) {
			// throw logger.throwing(e);
			logger.error("Unexpected Exception: " + e.getMessage(), e);
			fail("Unexpected Exception: " + e.getMessage());
		}

		// Verify that the performAction method is called with the correct arguments
		// verify(connector,times(1)).performAction(mockPlatform, Action.start); // or
		// Action.stop

		actionRequest.setAction(Action.stop);
		try {
			// Call the method being tested
			connector.handlePlatformAction(platformName, actionRequest);
		} catch (Exception e) {
			// throw logger.throwing(e);
			logger.error("Unexpected Exception: " + e.getMessage(), e);
			fail("Unexpected Exception: " + e.getMessage());
		}
	}

	@Test
	public void testHandlePlatformActionWithUnknownPlatform() {
		// Simulate the case where the platform is not found
		// Calls the method being tested and assert the expected behavior
		logger.info(
				"-------------------------- testHandlePlatformActionWithUnknownPlatform ------------------------------------");
		// Create an action request
		ActionRequest actionRequest = new ActionRequest();
		actionRequest.setAction(Action.start); // or Action.stop
		try {
			// Call the method being tested
			connector.handlePlatformAction("UnknownName", actionRequest);
		} catch (Exception e) {
			// throw logger.throwing(e);
			logger.error("Unexpected Exception: " + e.getMessage(), e);
			fail("Unexpected Exception: " + e.getMessage());
		}
	}

	@Test
	public void testPerformAction() {
		logger.info("-------------------------- testPerformAction ------------------------------------");

		doip.simulation.api.Platform testPlatfotm = mock(doip.simulation.api.Platform.class);
		try {
			// Call the method being tested
			connector.performAction(testPlatfotm, Action.start);
		} catch (Exception e) {
			logger.error("Unexpected Exception: " + e.getMessage(), e);
			fail("Unexpected Exception: " + e.getMessage());
		}
	}

}
