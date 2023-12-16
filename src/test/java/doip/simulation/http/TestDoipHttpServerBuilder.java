package doip.simulation.http;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import com.starcode88.jtest.TestExecutionError;

import doip.library.exception.DoipException;
import doip.library.properties.EmptyPropertyValue;
import doip.library.properties.MissingProperty;

public class TestDoipHttpServerBuilder {
	
	private static Logger logger = LogManager.getLogger(TestDoipHttpServerBuilder.class);
	
	@Test
	public void test() throws TestExecutionError  {
		boolean started = false;
		DoipHttpServer server = null;
		try {
			server = DoipHttpServerBuilder.newBuilder()
					.addPlatform("src/test/resources/X2024.properties")
					.build();
			server.start();
			started = true;
		} catch (IOException | MissingProperty | EmptyPropertyValue | DoipException e) {
			throw logger.throwing(new TestExecutionError(e));
		} finally {
			if (started) {
				server.stop();
			}
		}
	}
}
