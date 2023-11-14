package doip.simulation.http.helpers;
import com.sun.net.httpserver.HttpExchange;
import com.starcode88.http.HttpUtils;
import com.sun.net.httpserver.Headers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpServerHelper {
	private static final Logger logger = LogManager.getLogger(HttpServerHelper.class);
	
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
	 * Reads the request body of an HTTP exchange and converts it to the specified
	 * type.
	 * 
	 * Usage : String requestString = readRequestBody(exchange, String.class); int
	 * intValue = readRequestBody(exchange, Integer.class); byte[]
	 * byteArrayRequestBody = DoipHttpServer.readRequestBody(exchange,
	 * byte[].class);
	 * 
	 * @param exchange   The HTTP exchange containing the request body.
	 * @param targetType The target type class.
	 * @param <T>        The generic type of the target.
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
	 * Logs details of the HTTP response, including status code, headers, and
	 * optional response body.
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
	 * Logs details of the HTTP request, including URI, method, headers, and
	 * optional request body.
	 *
	 * @param exchange    The HTTP exchange.
	 * @param requestBody The optional request body.
	 * @param <T>         The type of the request body.
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
