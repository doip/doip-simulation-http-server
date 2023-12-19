package doip.simulation.http;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.List;

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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import doip.simulation.api.SimulationManager;
import doip.simulation.http.helpers.HttpServerHelper;
import static com.starcode88.jtest.Assertions.*;

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

		// SimulationManager mockSimulationManager = new MockSimulationManager();
		// Create a mock instance of SimulationManager
		SimulationManager mockSimulationManager = Mockito.mock(SimulationManager.class);

		server = new DoipHttpServer(PORT, mockSimulationManager);
		List<ContextHandler> defaultHandlers = List.of(new ContextHandler("/posttest", new PostHandler()),
				new ContextHandler("/gettest", new GetHandler()));
		server.createMappingContexts(defaultHandlers);

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

		String testtContext = "/posttest";
		if (!server.contextExists(testtContext)) {

			logger.warn("Mapping context '{}' does not exists.", testtContext);
			return;
		}

		String postMessage = "How are you?";
		HttpResponse<String> response = clientForLocalHost.POST(testtContext, postMessage, String.class);

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
		String testtContext = "/gettest";
		if (!server.contextExists(testtContext)) {

			logger.warn("Mapping context '{}' does not exists.", testtContext);
			return;
		}

		HttpResponse<String> response = clientForLocalHost.GET(testtContext, String.class);

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

class PostHandler implements HttpHandler {
	private static Logger logger = LogManager.getLogger(PostHandler.class);

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("POST".equals(exchange.getRequestMethod())) {
			// Read the request body as a string
			// String requestString = DoipHttpServer.readRequestBodyAsString(exchange);
			String requestString = HttpServerHelper.readRequestBody(exchange, String.class);
			HttpServerHelper.requestServerLogging(exchange, requestString);

			// Process the request string
			String response = "Received the following POST request: " + requestString;

			// Set the response headers and body
			HttpServerHelper.sendResponse(exchange, response, "text/plain", 200);
			HttpServerHelper.responseServerLogging(exchange, 200, response);

		} else {
			// Method not allowed
			logger.error("Method not allowed. Received a {} request.", exchange.getRequestMethod());
			exchange.sendResponseHeaders(405, -1);
		}
		exchange.close();
	}

}

class GetHandler implements HttpHandler {
	private static Logger logger = LogManager.getLogger(GetHandler.class);

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("GET".equals(exchange.getRequestMethod())) {
			// Create the GET response
			String response = "This is a GET request response.";

			// Set the response headers and body
			HttpServerHelper.sendResponse(exchange, response, "text/plain", 200);
			HttpServerHelper.responseServerLogging(exchange, 200, response);

		} else {
			// Method not allowed
			logger.error("Method not allowed. Received a {} request.", exchange.getRequestMethod());
			exchange.sendResponseHeaders(405, -1);
		}
		exchange.close();
	}

}
