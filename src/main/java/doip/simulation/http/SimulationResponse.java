package doip.simulation.http;

public class SimulationResponse {
	private int statusCode;
	private String jsonResponse;

	public SimulationResponse(int statusCode, String jsonResponse) {
		this.statusCode = statusCode;
		this.jsonResponse = jsonResponse;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getJsonResponse() {
		return jsonResponse;
	}

}
