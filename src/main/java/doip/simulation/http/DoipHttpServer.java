package doip.simulation.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import doip.simulation.api.SimulationManager;

import com.starcode88.http.HttpUtils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Represents an HTTP server for handling POST and GET requests.
 */
public class DoipHttpServer {

	private static Logger logger = LogManager.getLogger(DoipHttpServer.class);

	private static final int DEFAULT_PORT = 8080;
	private HttpServer server;
	private SimulationManager simulationManager = null;

	public SimulationManager getSimulationManager() {
		return simulationManager;
	}

	private List<ContextHandler> handlers;

	/**
	 * Checks if the server is currently running.
	 *
	 * @return true if the server is running, false otherwise.
	 */
	private boolean isRunning = false;

	public boolean isRunning() {
		return isRunning;
	}

	// To make the start() and stop() methods thread-safe
	private final Object lock = new Object(); // Object for synchronization

	/**
	 * Constructs a new DoipHttpServer with the default port.
	 *
	 * @param simulationManager The simulation manager for handling
	 *                          simulation-related functionality.
	 * @throws IOException If an I/O error occurs while creating the server.
	 */

	public DoipHttpServer(SimulationManager simulationManager) throws IOException {
		this(DEFAULT_PORT, simulationManager);
	}

	/**
	 * Constructs a new DoipHttpServer with a specified port.
	 *
	 * @param port              The port on which the server will listen.
	 * @param simulationManager The simulation manager for handling
	 *                          simulation-related functionality.
	 * @throws IOException If an I/O error occurs while creating the server.
	 */
	public DoipHttpServer(int port, SimulationManager simulationManager) throws IOException {
		this.simulationManager = simulationManager;

		server = HttpServer.create(new InetSocketAddress(port), 0);

		handlers = new ArrayList<ContextHandler>();

		createMappingContexts(); //TODO:

		for (ContextHandler contextHandler : handlers) {
			server.createContext(contextHandler.getContext(), contextHandler.getHandler());
		}
		server.setExecutor(null); // Use the default executor
	}

	/**
	 * Creates default mapping contexts for POST and GET requests.
	 */

	private void createMappingContexts() {
		List<ContextHandler> defaultHandlers  = List.of(
				new ContextHandler("/post", new PostHandler()),
				new ContextHandler("/get", new GetHandler()));
		createMappingContexts(defaultHandlers);
	}

	/**
	 * Adds a custom mapping context for handling HTTP requests if it does not
	 * already exist.
	 *
	 * @param context The context path for the mapping.
	 * @param handler The HTTP handler for processing requests in the specified
	 *                context.
	 */
	public void addMappingContext(String context, HttpHandler handler) {
		// Check if the context already exists in handlers
		if (!contextExists(context)) {
			handlers.add(new ContextHandler(context, handler));
			logger.info("Added mapping context: {}", context);
		} else {
			logger.warn("Mapping context '{}' already exists. Not adding it again.", context);
		}
	}

	/**
	 * Adds a list of custom mapping contexts for handling HTTP requests.
	 *
	 * @param customHandlers The list of custom context handlers.
	 */
	public void createMappingContexts(List<ContextHandler> customHandlers) {
		for (ContextHandler customHandler : customHandlers) {
			// Check if the context already exists in handlers before adding
			if (!contextExists(customHandler.getContext())) {
				handlers.add(customHandler);
				logger.info("Added mapping context: {}", customHandler.getContext());
			} else {
				logger.warn("Mapping context '{}' already exists. Not adding it again.", customHandler.getContext());
			}
		}
	}

//	/**
//	 * Adds a list of custom mapping contexts for handling HTTP requests.
//	 *
//	 * @param customHandlers The list of custom context handlers.
//	 */
//	public void createMappingContextsAll(List<ContextHandler> customHandlers) {
//		handlers.addAll(customHandlers);
//	}

	/**
	 * Checks if a context path already exists in the handlers.
	 *
	 * @param context The context path to check.
	 * @return true if the context path already exists, false otherwise.
	 */
	private boolean contextExists(String context) {
		return handlers.stream().anyMatch(handler -> handler.getContext().equals(context));
	}

	/**
	 * Starts the HTTP server.
	 */
	public void start() {
		synchronized (lock) {
			try {
				if (server != null && !isRunning) {
					server.start();
					logger.info("Server is running on port {}.", server.getAddress().getPort());
					isRunning = true;
				}
			} catch (Exception e) {
				logger.error("Error starting the server: {}", e.getMessage(), e);
			}
		}
	}

	/**
	 * Stops the HTTP server.
	 */
	public void stop() {
		synchronized (lock) {
			if (server != null && isRunning) {
				server.stop(0);
				logger.info("Server stopped.");
				isRunning = false;
			}
		}
	}

	/**
	 * Sends a response to the client with the given message.
	 *
	 * @param exchange    The HTTP exchange.
	 * @param message     The message to send in the response.
	 * @param contentType The response content Type
	 * @param code        The response code to send
	 * @throws IOException If an I/O error occurs while sending the response.
	 */
	public static void sendResponseAsString(HttpExchange exchange, String message, String contentType, int code)
			throws IOException {
		try {
			exchange.getResponseHeaders().add("Content-Type", contentType);
			exchange.sendResponseHeaders(code, message.getBytes(StandardCharsets.UTF_8).length);

			OutputStream responseBody = exchange.getResponseBody();
			responseBody.write(message.getBytes(StandardCharsets.UTF_8));
			responseBody.close();
		} catch (IOException e) {
			logger.error("Error sending response: {}", e.getMessage(), e);
			throw e; // Re-throw the exception for higher-level handling
		}
	}
	
	/**
     * Sends a response to the client with the given message.
     *
     * @param exchange    The HTTP exchange.
     * @param message     The message to send in the response.
     * @param contentType The response content Type.
     * @param code        The response code to send.
     * @param <T>         The type of the message.
     * @throws IOException If an I/O error occurs while sending the response.
     */
    public static <T> void sendResponse(HttpExchange exchange, T message, String contentType, int code)
            throws IOException {
        try {
            // Convert the message to bytes based on its type
            byte[] messageBytes;
            if (message instanceof String) {
                messageBytes = ((String) message).getBytes(StandardCharsets.UTF_8);
            } else if (message instanceof byte[]) {
                messageBytes = (byte[]) message;
            } else {
                // Handle other types or throw an exception based on your requirements
                throw new IllegalArgumentException("Unsupported message type: " + message.getClass().getName());
            }

            // Set response headers
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(code, messageBytes.length);

            // Write the message bytes to the response body
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(messageBytes);
            }
        } catch (IOException e) {
            logger.error("Error sending response: {}", e.getMessage(), e);
            throw e; // Re-throw the exception for higher-level handling
        }
    }

	/**
	 * Reads the request body of an HTTP exchange and converts it to a String.
	 *
	 * @param exchange The HTTP exchange containing the request body.
	 * @return The request body as a String.
	 * @throws IOException If an I/O error occurs while reading the request body.
	 */
	public static String readRequestBodyAsString(HttpExchange exchange) throws IOException {
		try {
			// Get the input stream of the request body
			InputStream requestBody = exchange.getRequestBody();

			// Create a StringBuilder to accumulate the request body content
			StringBuilder requestStringBuilder = new StringBuilder();

			// Read each byte from the input stream and append it to the StringBuilder
			int byteRead;
			while ((byteRead = requestBody.read()) != -1) {
				requestStringBuilder.append((char) byteRead);
			}

			// Convert the StringBuilder content to a String
			String requestString = requestStringBuilder.toString();

			// Return the resulting String representing the request body
			return requestString;
		} catch (IOException e) {
			logger.error("Error reading request body: {}", e.getMessage(), e);
			throw e; // Re-throw the exception for higher-level handling
		}
	}
	
	 /**
     * Reads the request body of an HTTP exchange and converts it to the specified type.
     * 
     * Usage : 
     * String requestString = readRequestBody(exchange, String.class);
     * int intValue = readRequestBody(exchange, Integer.class);
     * byte[] byteArrayRequestBody = DoipHttpServer.readRequestBody(exchange, byte[].class);
     * 
     * @param exchange The HTTP exchange containing the request body.
     * @param targetType The target type class.
     * @param <T> The generic type of the target.
     * @return The request body converted to the specified type.
     * @throws IOException If an I/O error occurs while reading the request body.
     */
	public static <T> T readRequestBody(HttpExchange exchange, Class<T> targetType) throws IOException {
        try {
            InputStream requestBody = exchange.getRequestBody();

            // For byte array target type
            if (targetType == byte[].class) {
                return targetType.cast(readBytesFromStream(requestBody));
            }

            // For other types, read as a string and convert
            Scanner scanner = new Scanner(requestBody, StandardCharsets.UTF_8.name()).useDelimiter("\\A");

            if (scanner.hasNext()) {
                String requestString = scanner.next();
                // Use custom logic to convert the requestString to the targetType
                return targetType.cast(requestString);
            } else {
                throw new IOException("Empty request body");
            }
        } catch (IOException e) {
            logger.error("Error reading request body: {}", e.getMessage(), e);
            throw e;
        }
    }

	
	/**
     * Reads bytes from an input stream.
     *
     * @param inputStream The input stream.
     * @return The byte array read from the input stream.
     * @throws IOException If an I/O error occurs.
     */
    private static byte[] readBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int bytesRead;
        byte[] data = new byte[1024];
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }
    
    /**
     * Logs details of the HTTP response, including status code, headers, and optional response body.
     *
     * @param exchange     The HTTP exchange.
     * @param responseCode The HTTP response status code.
     * @param responseBody The optional response body.
     * @param <T>          The type of the response body.
     */
    public static <T> void responseServerLogging(HttpExchange exchange, int responseCode, T responseBody) {
        logger.info("--------------------------------------------------------------");
        logger.info("Sent HTTP response:");
        logger.info("    Status code = {} ({})", responseCode, HttpUtils.getStatusText(responseCode));

        Headers headers = exchange.getResponseHeaders();

        // Log the headers
        headerLogging(headers);

        // Log the response body if it is not null and is a String
        if (responseBody != null && responseBody instanceof String) {
            logger.info(" Response body = {}", responseBody);
        }

        logger.info("--------------------------------------------------------------");
    }

    /**
     * Logs details of the HTTP request, including URI, method, headers, and optional request body.
     *
     * @param exchange   The HTTP exchange.
     * @param requestBody The optional request body.
     * @param <T>        The type of the request body.
     */
    public static <T> void requestServerLogging(HttpExchange exchange, T requestBody) {
        logger.info("--------------------------------------------------------------");
        logger.info("Received HTTP request:");
        logger.info("    {}", exchange.getRequestURI());
        String method = exchange.getRequestMethod();
        logger.info("    " + method);

        Headers headers = exchange.getRequestHeaders();

        // Log the headers
        headerLogging(headers);

        // Log the request body if it is not null and is a String
        if (requestBody != null && requestBody instanceof String) {
            logger.info("  Request body = {}", requestBody);
        }

        logger.info("--------------------------------------------------------------");
    }

    /**
     * Logs the headers of an HTTP request or response.
     *
     * @param headers The headers to log.
     */
    private static void headerLogging(Headers headers) {
        logger.info("    Headers:");

        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            String headerName = header.getKey();
            List<String> headerValues = header.getValue();

            // Join the header values into a comma-separated string
            String headerValueString = String.join(", ", headerValues);

            logger.info("        {} = {}", headerName, headerValueString);
        }
    }

}

class PostHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("POST".equals(exchange.getRequestMethod())) {
			// Read the request body as a string
			//String requestString = DoipHttpServer.readRequestBodyAsString(exchange);
			String requestString = DoipHttpServer.readRequestBody(exchange, String.class);
			DoipHttpServer.requestServerLogging(exchange, requestString);

			// Process the request string
			String response = "Received the following POST request: " + requestString;

			// Set the response headers and body
			DoipHttpServer.sendResponse(exchange, response, "text/plain", 200);
			DoipHttpServer.responseServerLogging(exchange, 200, response);
		
		} else {
			// Method not allowed
			exchange.sendResponseHeaders(405, -1);
		}
		exchange.close();
	}

}

class GetHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if ("GET".equals(exchange.getRequestMethod())) {
			// Create the GET response
			String response = "This is a GET request response.";

			// Set the response headers and body
			DoipHttpServer.sendResponse(exchange, response, "text/plain", 200);
			DoipHttpServer.responseServerLogging(exchange, 200, response);

		} else {
			// Method not allowed
			exchange.sendResponseHeaders(405, -1);
		}
		exchange.close();
	}

}

class ContextHandler {
	private String context;
	private HttpHandler handler;

	public ContextHandler(String context, HttpHandler handler) {
		this.context = context;
		this.handler = handler;
	}

	public String getContext() {
		return context;
	}

	public HttpHandler getHandler() {
		return handler;
	}
}
