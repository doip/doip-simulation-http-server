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
import com.starcode88.http.HttpUtils;
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

	// private static final String PLATFORM_PATH = "/doip-simulation/platform";
	// private static final String DOIP_SIMULATION_PATH = "/doip-simulation/";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		//SimulationManager mockSimulationManager = new MockSimulationManager();
		// Create a mock instance of SimulationManager
		SimulationManager mockSimulationManager = Mockito.mock(SimulationManager.class);

		server = new DoipHttpServer(PORT, mockSimulationManager);

		customController = new CustomMappingController(server);

//		customController.addExternalHandler(SimulationConnector.DOIP_SIMULATION_PATH, new GetSimulationOverviewHandler(server));
//		customController.addExternalHandler(SimulationConnector.PLATFORM_PATH, new GetPlatformOverviewHandler(server));
		customController.addExternalHandler(SimulationConnector.DOIP_SIMULATION_PATH, new GetSimulationOverviewHandler(
				server, new SimulationConnectorTest(server)));
		customController.addExternalHandler(SimulationConnector.PLATFORM_PATH, new GetPlatformOverviewHandler(server,
				new SimulationConnectorTest(server)));

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

		// "/doip-simulation/?status=RUNNING"
		HttpResponse<String> response = clientForLocalHost
				.GET(SimulationConnector.DOIP_SIMULATION_PATH + "?status=RUNNING", String.class);
		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// it must work without query parameters
		response = clientForLocalHost.GET(SimulationConnector.DOIP_SIMULATION_PATH, String.class);

		assertEquals(200, response.statusCode(), "The HTTP status code is not 200");

		assertNotNull(response.body(), "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom GET test completed.");
	}

	@Test
	void testGetPlatformOverviewHandler() throws HttpStatusCodeException, HttpInvalidResponseBodyType,
			URISyntaxException, IOException, InterruptedException {
		logger.info("-------------------------- testGetPlatformOverviewHandler ------------------------------------");

		// "/doip-simulation/platform/X2024"
		HttpResponse<String> response = clientForLocalHost.GET(SimulationConnector.PLATFORM_PATH + "/X2024",
				String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom GET test completed.");
	}

	@Test
	void testGetGatewayOverviewHandler() throws HttpStatusCodeException, HttpInvalidResponseBodyType,
			URISyntaxException, IOException, InterruptedException {
		logger.info("-------------------------- testGetGatewayOverviewHandler ------------------------------------");

		// "/doip-simulation/platform/X2024/gateway/GW"
		HttpResponse<String> response = clientForLocalHost.GET(SimulationConnector.PLATFORM_PATH + "/X2024/gateway/GW",
				String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom GET test completed.");
	}

	@Test
	void testPostPlatformOverviewHandler() throws HttpStatusCodeException, HttpInvalidResponseBodyType,
			URISyntaxException, IOException, InterruptedException, HttpInvalidRequestBodyType {
		logger.info("-------------------------- testPostPlatformOverviewHandler ------------------------------------");

		// String jsonPostString =
		// "{\"name\":\"X2024\",\"url\":\"http://myserver.com/doip-simulation/platform/X2024\",\"status\":\"RUNNING\",\"gateways\":[{\"name\":\"string\",\"url\":\"http://myserver.com/doip-simulation/platform/X2024/gateway/GW\",\"status\":\"RUNNING\",\"error\":\"Can't
		// bind to port 13400\"}]}";
		String jsonPostString = "{\"action\": \"start\"}";

		HttpClient clientForPost = new HttpClient("http://localhost:" + PORT);
		clientForPost.addHeader("Content-Type", "application/json");
		// "/doip-simulation/platform/X2024"
		HttpResponse<String> response = clientForPost.POST(SimulationConnector.PLATFORM_PATH + "/X2024", jsonPostString,
				String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom POST test completed.");
	}

	@Test
	void testPostPlatformEmptyRequestJson() throws HttpStatusCodeException, HttpInvalidResponseBodyType,
			URISyntaxException, IOException, InterruptedException, HttpInvalidRequestBodyType {
		logger.info("-------------------------- testPostPlatformEmptyRequestJson ------------------------------------");

		String jsonPostString = "";
		HttpClient clientForPost = new HttpClient("http://localhost:" + PORT);
		clientForPost.addHeader("Content-Type", "application/json");

		// "/doip-simulation/platform/X2024"
		HttpResponse<String> response = clientForPost.POST(SimulationConnector.PLATFORM_PATH + "/X2024", jsonPostString,
				String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom POST test completed.");
	}

	@Test
	void testPostPlatformWrongBodyType() throws HttpStatusCodeException, HttpInvalidResponseBodyType,
			URISyntaxException, IOException, InterruptedException, HttpInvalidRequestBodyType {
		logger.info("-------------------------- testPostPlatformWrongBodyType ------------------------------------");

		String postMessage = "Update or run an action for the platform given by the platformId";

		HttpClient clientForPost = new HttpClient("http://localhost:" + PORT);
		clientForPost.addHeader("Content-Type", "application/json");

		int statusCode = 0;
		HttpResponse<String> response = null;
		try {
			// "/doip-simulation/platform/X2024"
			response = clientForPost.POST(SimulationConnector.PLATFORM_PATH + "/X2024", postMessage, String.class);

		} catch (HttpStatusCodeException e) {
			statusCode = e.getResponse().statusCode();
			String statusText = HttpUtils.getStatusText(statusCode);
			logger.info("Status code = {} ({})", statusCode, statusText);
		}
		assertEquals(400, statusCode, "The status code does not match the value 400");

		// logger.info("response {}", response);
		assertNull(response, "The response from server is not null");

		logger.info("Custom POST test completed.");
	}

	@Test
	void testPostPlatformWrongRequestJson() throws HttpStatusCodeException, HttpInvalidResponseBodyType,
			URISyntaxException, IOException, InterruptedException, HttpInvalidRequestBodyType {
		logger.info("-------------------------- testPostPlatformWrongRequestJson ------------------------------------");

		// String jsonPostString =
		// "{\"urlurl\":\"http://myserver.com/doip-simulation/platform/X2024\",\"status\":\"RUNNING\",\"gateways\":[{\"name\":\"string\",\"url\":\"http://myserver.com/doip-simulation/platform/X2024/gateway/GW\",\"status\":\"RUNNING\",\"error\":\"Can't
		// bind to port 13400\"}]}";
		String jsonPostString = "{\"action\": \"Unknown\"}";
		HttpClient clientForPost = new HttpClient("http://localhost:" + PORT);
		clientForPost.addHeader("Content-Type", "application/json");
		// "/doip-simulation/platform/X2024"
		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
				() -> clientForPost.POST(SimulationConnector.PLATFORM_PATH + "/X2024", jsonPostString, String.class));
		int statusCode = e.getResponse().statusCode();
		String statusText = HttpUtils.getStatusText(statusCode);
		logger.info("Status code = {} ({})", statusCode, statusText);
		assertEquals(400, e.getResponse().statusCode(), "The status code does not match the value 400");

		logger.info("Custom POST test completed.");
	}

}
