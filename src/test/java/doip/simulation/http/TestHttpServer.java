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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starcode88.http.HttpClient;
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
		
		SimulationManager  mockSimulation =  new SimulationManagerMock();

		server = new DoipHttpServer(PORT, mockSimulation);

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

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testDoipPOST() throws HttpStatusCodeException, URISyntaxException, IOException, InterruptedException,
			HttpInvalidRequestBodyType, HttpInvalidResponseBodyType {
		logger.info("---------------------------  testDoipPOST -----------------------------------");
	
		HttpResponse<String> response = clientForLocalHost.POST("/post", "How are you?", String.class);

		assertNotNull(response, "The response from server is null");

		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");
		String body = response.body();
		assertNotNull(body, "The response from server is null");
		
		logger.info("--------------------------------------------------------------");
	}

	@Test
	void testDoipGET() throws HttpStatusCodeException, HttpInvalidResponseBodyType, URISyntaxException, IOException,
			InterruptedException {
		logger.info("---------------------------  testDoipGet -----------------------------------");
		HttpResponse<String> response = clientForLocalHost.GET("/get", String.class);
		
		int statusCode = response.statusCode();
		assertEquals(200, statusCode, "The HTTP status code is not 200");
		String body = response.body();
		assertNotNull(body, "The response from server is null");
		
		logger.info("--------------------------------------------------------------");
	}

}
