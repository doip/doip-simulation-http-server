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


import com.starcode88.http.HttpClient;
import com.starcode88.http.exception.HttpInvalidRequestBodyType;
import com.starcode88.http.exception.HttpInvalidResponseBodyType;
import com.starcode88.http.exception.HttpStatusCodeException;

class TestOverviewHandler {
	
	private static Logger logger = LogManager.getLogger(TestOverviewHandler.class);

	private static DoipHttpServer server = null;

	private static CustomMappingController customController  = null;

	private static HttpClient clientForLocalHost = null;

	private static final int PORT = 8080;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		SimulationManagerMock mockSimulation = new SimulationManagerMock();

		server = new DoipHttpServer(PORT, mockSimulation);

		customController  = new CustomMappingController(server);
		
		customController.addExternalHandler("/", new GetSimulationOverviewHandler(server));

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
	void testGetOverviewHandler() throws HttpStatusCodeException, HttpInvalidResponseBodyType, URISyntaxException, IOException, InterruptedException {
		logger.info("-------------------------- testGetOverviewHandler ------------------------------------");
		
		HttpResponse<String> response = clientForLocalHost.GET("/?status=RUNNING", String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");

		String responseBody = response.body();
		assertNotNull(responseBody, "The response body from server is null");

		// TODO Add more assertions if needed

		logger.info("Custom GET test completed.");
	}

}
