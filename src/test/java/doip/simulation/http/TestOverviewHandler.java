package doip.simulation.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.starcode88.http.HttpClient;
import com.starcode88.http.exception.HttpInvalidRequestBodyType;
import com.starcode88.http.exception.HttpInvalidResponseBodyType;
import com.starcode88.http.exception.HttpStatusCodeException;

import doip.simulation.api.SimulationManager;

class TestOverviewHandler {

	private static Logger logger = LogManager.getLogger(TestOverviewHandler.class);

	private static DoipHttpServer server = null;

	private static CustomMappingController customController = null;

	private static HttpClient clientForLocalHost = null;

	private static final int PORT = 8080;
	
	private static final String PLATFORM_PATH = "/doip-simulation/platform";
	private static final String DOIP_SIMULATION_PATH = "/doip-simulation/";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// SimulationManagerMock mockSimulationManager = new SimulationManagerMock();
		// Create a mock instance of SimulationManager
		SimulationManager mockSimulationManager = Mockito.mock(SimulationManager.class);

		server = new DoipHttpServer(PORT, mockSimulationManager);

		customController = new CustomMappingController(server);

		customController.addExternalHandler(DOIP_SIMULATION_PATH, new GetSimulationOverviewHandler(server));
		customController.addExternalHandler(PLATFORM_PATH, new GetPlatformOverviewHandler(server));

		customController.startHttpServer();

		clientForLocalHost = new HttpClient("http://localhost:" + PORT);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		if (server != null) {
			server.stop();
			server = null;
		}
	}

	@Test
	void testGetOverviewHandler() throws HttpStatusCodeException, HttpInvalidResponseBodyType, URISyntaxException,
			IOException, InterruptedException {
		logger.info("-------------------------- testGetOverviewHandler ------------------------------------");

		HttpResponse<String> response = clientForLocalHost.GET("/doip-simulation/?status=RUNNING", String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom GET test completed.");
	}
	
	
	@Test
	void testGetPlatformOverviewHandler() throws HttpStatusCodeException, HttpInvalidResponseBodyType, URISyntaxException,
			IOException, InterruptedException {
		logger.info("-------------------------- testGetPlatformOverviewHandler ------------------------------------");

		HttpResponse<String> response = clientForLocalHost.GET("/doip-simulation/platform/X2024", String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom GET test completed.");
	}
	
	@Test
	void testGetGatewayOverviewHandler() throws HttpStatusCodeException, HttpInvalidResponseBodyType, URISyntaxException,
			IOException, InterruptedException {
		logger.info("-------------------------- testGetGatewayOverviewHandler ------------------------------------");

		HttpResponse<String> response = clientForLocalHost.GET("/doip-simulation/platform/X2024/gateway/GW", String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom GET test completed.");
	}

}
