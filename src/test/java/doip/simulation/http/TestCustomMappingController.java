package doip.simulation.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starcode88.http.HttpClient;
import com.starcode88.http.exception.HttpInvalidRequestBodyType;
import com.starcode88.http.exception.HttpInvalidResponseBodyType;
import com.starcode88.http.exception.HttpStatusCodeException;

import doip.simulation.api.SimulationManager;

class TestCustomMappingController {
	
	private static Logger logger = LogManager.getLogger(TestHttpServer.class);

	private static DoipHttpServer server = null;
	
	private static CustomMappingController customMapping = null;

	private static HttpClient clientForLocalHost = null;

	private static final int PORT = 8080;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		SimulationManager mockSimulation = new SimulationManagerMock();

		server = new DoipHttpServer(PORT, mockSimulation);
		
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

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testCustomPOST() throws HttpStatusCodeException, URISyntaxException, IOException, InterruptedException,
			HttpInvalidRequestBodyType, HttpInvalidResponseBodyType {
		logger.info("---------------------------  testCustomPOST -----------------------------------");

		String postMessage = "How are you?";
		HttpResponse<String> response = clientForLocalHost.POST("/customPost", postMessage, String.class);

		assertNotNull(response, "The response from server is null");

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");
		String body = response.body();
		assertNotNull(body, "The response from server is null");
		
		logger.info("--------------------------------------------------------------");
	}

	@Test
	void testCustomGET() throws HttpStatusCodeException, HttpInvalidResponseBodyType, URISyntaxException, IOException,
			InterruptedException {
		logger.info("---------------------------  testCustomGET -----------------------------------");
		HttpResponse<String> response = clientForLocalHost.GET("/customGet", String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");
		String body = response.body();
		assertNotNull(body, "The response from server is null");

		logger.info("--------------------------------------------------------------");
	}

}
