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

class TestCustomMappingController {

	private static Logger logger = LogManager.getLogger(TestCustomMappingController.class);

	private static DoipHttpServer server = null;

	private static CustomMappingController customMapping = null;

	private static HttpClient clientForLocalHost = null;

	private static final int PORT = 8080;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// SimulationManagerMock mockSimulationManager = new SimulationManagerMock();
		// Create a mock instance of SimulationManager
		SimulationManager mockSimulationManager = Mockito.mock(SimulationManager.class);

		server = new DoipHttpServer(PORT, mockSimulationManager);

		customMapping = new CustomMappingController(server);

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
		logger.info("--------------------------------------------------------------");
		logger.info("Testing custom POST endpoint...");

		String postMessage = "How are you?";
		HttpResponse<String> response = clientForLocalHost.POST("/customPost", postMessage, String.class);

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
		logger.info("--------------------------------------------------------------");
		logger.info("Testing custom GET endpoint...");

		HttpResponse<String> response = clientForLocalHost.GET("/customGet", String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom GET test completed.");
	}
}
