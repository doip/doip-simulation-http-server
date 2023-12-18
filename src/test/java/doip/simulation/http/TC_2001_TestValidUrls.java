package doip.simulation.http;

import static com.starcode88.jtest.Assertions.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.starcode88.http.HttpClient;
import com.starcode88.http.exception.HttpInvalidResponseBodyType;
import com.starcode88.http.exception.HttpStatusCodeException;
import com.starcode88.jtest.InitializationError;
import com.starcode88.jtest.TestCaseDescribed;
import com.starcode88.jtest.TestCaseDescription;
import com.starcode88.jtest.TestExecutionError;

import doip.library.exception.DoipException;
import doip.library.properties.EmptyPropertyValue;
import doip.library.properties.MissingProperty;
import doip.simulation.http.lib.utils.JsonUtils;

public class TC_2001_TestValidUrls {
	
	public static final String BASE_ID = "TC-2001";
	
	private static Logger logger = LogManager.getLogger(TC_2001_TestValidUrls.class);
	
	private static DoipHttpServer server = null;
	
	public static final String host = "http://localhost:8080";
	
	@BeforeAll
	public static void setUpBeforeClass() throws InitializationError {
		String method = "public static void setUpBeforeClass()";
		try {
			TestCaseDescribed.setUpBeforeClass(BASE_ID);
			logger.trace(">>> {}", method);
			server = DoipHttpServerBuilder.newBuilder()
					.addPlatform("src/test/resources/X2024.properties")
					.build();
			server.start();
		} catch (IOException | MissingProperty | EmptyPropertyValue | DoipException e) {
			throw logger.throwing(new InitializationError(e));
		} finally {
			logger.trace("<<< {}", method);
		}
	}
	
	@AfterAll
	public static void tearDownAfterClass() {
		String method = "public static void tearDownAfterClass()";
		logger.trace(">>> {}", method);
		server.stop();
		logger.trace("<<< {}", method);
	}
	
	@ParameterizedTest(name = BASE_ID + "-01[url=\"{0}\"]")
	@ValueSource(strings = {
		"/doip-simulation",
		"/doip-simulation/platform/X2024",
		"/doip-simulation/platform/X2024/gateway/GW",
		"/doip-simulation/platform/X2024/gateway/GW/ecu/EMS",
		"/doip-simulation/platform/X2024/gateway/GW/ecu/TCU"
	})
	public void test_01(String url) throws TestExecutionError {
		String method = "public void test_01(String url)";
		try {
			logger.trace(">>> {}", method);
			TestCaseDescription desc = new TestCaseDescription(
					BASE_ID + "-01[url=\""+url+"\"]",
					"Request valid URL",
					"Send HTTP request for URL " + url + " to the server",
					"Server sends HTTP code 200 with a valid JSON body");
			TestCaseDescribed.runTest(desc, () -> testImpl_01(url));
		} finally {
			logger.trace("<<< {}", method);
		}
	}
	
	public void testImpl_01(String url) throws TestExecutionError {
		String method = "public void testImpl_01(String url)";
		try {
			logger.trace(">>> {}", method);
			HttpClient client = new HttpClient(host);
			HttpResponse<String> response = client.GET(url, String.class);
			assertNotNull(response);
			String jsonString = response.body();
			//????? logger.info("HTTP body\n" + JsonUtils.prettyPrint(jsonString));
			logger.info("HTTP body\n" + jsonString);
		} catch (HttpStatusCodeException | HttpInvalidResponseBodyType | URISyntaxException | IOException
				| InterruptedException e) {
			throw logger.throwing(new TestExecutionError(e));
		} finally {
			logger.trace("<<< {}", method);
		}
	}
}
