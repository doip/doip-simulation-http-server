package doip.simulation.http;

import static org.junit.jupiter.api.Assertions.*;

//import static asd.junit.Assertions.assertEquals;
//import static asd.junit.Assertions.assertNotNull;
//import static asd.junit.Assertions.assertThrows;

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

class TestHttpServer {

	private static Logger logger = LogManager.getLogger(TestHttpServer.class);

	private static DoipHttpServer server = null;

	private static HttpClient clientForLocalHost = null;

	private static final int PORT = 8080;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
//		GatewayConfig config = new GatewayConfig();
//		//String path = "C:\DISK_D\Diagnose\doip-custom-simulation\build\install\doip-custom-simulation\gateway.properties";
//		String path = "src/test/resources/gateway.properties";
//		config.loadFromFile(path);		
//		CustomGateway gateway = new CustomGateway(config);		
//		server = new DoipHttpServer(gateway);

		// SimulationManagerMock mockSimulationManager = new SimulationManagerMock();
		// Create a mock instance of SimulationManager
		SimulationManager mockSimulationManager = Mockito.mock(SimulationManager.class);

		server = new DoipHttpServer(PORT, mockSimulationManager);

		server.start();
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
	void testDoipPOST() throws HttpStatusCodeException, URISyntaxException, IOException, InterruptedException,
			HttpInvalidRequestBodyType, HttpInvalidResponseBodyType {
		logger.info("---------------------------  testDoipPOST -----------------------------------");

		String postMessage = "How are you?";
		HttpResponse<String> response = clientForLocalHost.POST("/posttest", postMessage, String.class);

		assertNotNull(response, "The response from server is null");

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");
		String body = response.body();
		assertNotNull(body, "The response from server is null");
		assertTrue(body.contains(postMessage), "The response from server is not completely");

		logger.info("--------------------------------------------------------------");
	}

	@Test
	void testDoipGET() throws HttpStatusCodeException, HttpInvalidResponseBodyType, URISyntaxException, IOException,
			InterruptedException {
		logger.info("---------------------------  testDoipGet -----------------------------------");
		HttpResponse<String> response = clientForLocalHost.GET("/gettest", String.class);

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");
		String body = response.body();
		assertNotNull(body, "The response from server is null");

		logger.info("--------------------------------------------------------------");
	}
	
/*
	@Test
	void testCheckWrongHttpMethod() throws HttpStatusCodeException, HttpInvalidResponseBodyType, URISyntaxException,
			IOException, InterruptedException, HttpInvalidRequestBodyType {
		logger.info("---------------------------  testCheckWrongHttpMethod -----------------------------------");
		

//		HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class, () -> clientForLocalHost.POST("/gettest","??????", String.class));
//		int statusCode = e.getResponse().statusCode();
//		String statusText = HttpUtils.getStatusText(statusCode);
//		logger.info("Status code = {} ({})", statusCode, statusText);
//		assertEquals(405, e.getResponse().statusCode(), "The status code does not match the value 405");

		int statusCode = 0;
		try {
			clientForLocalHost.POST("/gettest", "??????", String.class);
		} catch (HttpStatusCodeException e) {
			statusCode = e.getResponse().statusCode();
			String statusText = HttpUtils.getStatusText(statusCode);
			logger.info("Status code = {} ({})", statusCode, statusText);
		}

		assertEquals(405, statusCode, "The status code does not match the value 405");

		logger.info("--------------------------------------------------------------");
	}
*/
}
