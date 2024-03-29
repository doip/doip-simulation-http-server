package doip.simulation.http;

import static com.starcode88.jtest.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.starcode88.http.HttpClient;
import com.starcode88.http.exception.HttpInvalidRequestBodyType;
import com.starcode88.http.exception.HttpInvalidResponseBodyType;
import com.starcode88.http.exception.HttpStatusCodeException;

import doip.simulation.api.SimulationManager;

class TestCustomMappingController {

	private static Logger logger = LogManager.getLogger(TestCustomMappingController.class);

	private static DoipHttpServer server = null;

	private static CustomMappingController customMapping = null;

	private static HttpClient clientForLocalHost = null;

	private static final int PORT = 8080;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// SimulationManager mockSimulationManager = new MockSimulationManager();
		// Create a mock instance of SimulationManager
		SimulationManager mockSimulationManager = Mockito.mock(SimulationManager.class);

		server = new DoipHttpServer(PORT, mockSimulationManager);

		customMapping = new CustomMappingController(server);
		List<ContextHandler> customHandlers = List.of(new ContextHandler("/customPost", new PostHandlerCustom()),
				new ContextHandler("/customGet", new GetHandlerCustom()));

		// Create and configure the DoipHttpServer instance
		server.createMappingContexts(customHandlers);

		customMapping.startHttpServer();

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
	void testCustomPOST() throws HttpStatusCodeException, URISyntaxException, IOException, InterruptedException,
			HttpInvalidRequestBodyType, HttpInvalidResponseBodyType {
		logger.info("-------------------------- testCustomPOST ------------------------------------");
		String testtContext = "/customPost";
		if (!server.contextExists(testtContext)) {

			logger.warn("Mapping context '{}' does not exists.", testtContext);
			return;
		}

		logger.info("Testing custom POST endpoint...");

		String postMessage = "How are you?";
		HttpResponse<String> response = clientForLocalHost.POST(testtContext, postMessage, String.class);

		assertNotNull(response, "The response from server is null");

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom POST test completed.");
	}

	@Test
	void testCustomGET() throws HttpStatusCodeException, HttpInvalidResponseBodyType, URISyntaxException, IOException,
			InterruptedException {
		logger.info("--------------------------  testCustomGET ------------------------------------");

		String testtContext = "/customGet";
		if (!server.contextExists(testtContext)) {

			logger.warn("Mapping context '{}' does not exists.", testtContext);
			return;
		}

		logger.info("Testing custom GET endpoint...");

		HttpResponse<String> response = clientForLocalHost.GET(testtContext, String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom GET test completed.");
	}
}
