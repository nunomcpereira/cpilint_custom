package com.nmp.cpilint.impl;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.json.JSONArray;
import org.json.JSONObject;

import dk.mwittrock.cpilint.artifacts.IflowArtifact;
import dk.mwittrock.cpilint.artifacts.IflowArtifactTag;
import dk.mwittrock.cpilint.consumers.IssueConsumer;
import dk.mwittrock.cpilint.rules.RuleBase;

final class DesignGuidelinesRule extends RuleBase {

	private static final int POLL_MAX_ATTEMPTS = 24;
	private static final long POLL_INTERVAL_MS = 5000L;

	private static final String ENV_BASE_URL    = "CPILINT_DG_BASE_URL";
	private static final String ENV_AUTH_URL    = "CPILINT_DG_AUTH_URL";
	private static final String ENV_CLIENT_ID   = "CPILINT_DG_CLIENT_ID";
	private static final String ENV_CLIENT_SECRET = "CPILINT_DG_CLIENT_SECRET";

	private final String baseUrl;
	private final String authUrl;
	private final String clientId;
	private final String clientSecret;
	private HttpClient httpClient;
	private String oauthToken;
	private String csrfToken;

	DesignGuidelinesRule() {
		this.baseUrl      = requireEnv(ENV_BASE_URL);
		this.authUrl      = requireEnv(ENV_AUTH_URL);
		this.clientId     = requireEnv(ENV_CLIENT_ID);
		this.clientSecret = requireEnv(ENV_CLIENT_SECRET);
	}

	private static String requireEnv(String name) {
		String value = System.getenv(name);
		if (value == null || value.isBlank()) {
			throw new RuntimeException("Required environment variable not set: " + name);
		}
		return value;
	}

	@Override
	public void startTesting(IssueConsumer consumer) {
		super.startTesting(consumer);
		httpClient = HttpClient.newBuilder()
			.cookieHandler(new CookieManager())
			.connectTimeout(Duration.ofSeconds(30))
			.build();
		oauthToken = fetchOAuthToken();
		csrfToken = fetchCsrfToken();
	}

	@Override
	public void inspect(IflowArtifact iflow) {
		IflowArtifactTag tag = iflow.getTag();
		String iflowId = tag.getId();
		try {
			String executionId = triggerExecution(iflowId);
			waitForCompletion(iflowId, executionId);
			reportViolations(iflowId, executionId, tag);
		} catch (Exception e) {
			System.err.println("[DesignGuidelinesRule] Error processing iflow " + iflowId + ": " + e.getMessage());
		}
	}

	private String fetchOAuthToken() {
		String clientId = this.clientId;
		String clientSecret = this.clientSecret;
		String authUrl = this.authUrl;

		String body = "grant_type=client_credentials"
			+ "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
			+ "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8);

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(authUrl))
			.header("Content-Type", "application/x-www-form-urlencoded")
			.header("Accept", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(body))
			.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				throw new RuntimeException("OAuth token request failed with status "
					+ response.statusCode() + ": " + response.body());
			}
			return new JSONObject(response.body()).getString("access_token");
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Failed to fetch OAuth token", e);
		}
	}

	private String fetchCsrfToken() {
		String baseUrl = this.baseUrl;

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(baseUrl))
			.header("Authorization", "Bearer " + oauthToken)
			.header("X-CSRF-Token", "Fetch")
			.method("HEAD", HttpRequest.BodyPublishers.noBody())
			.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			String token = response.headers().firstValue("x-csrf-token").orElse(null);
			if (token == null || token.isBlank()) {
				throw new RuntimeException("CSRF token not found in response headers (status " + response.statusCode() + ")");
			}
			return token;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Failed to fetch CSRF token", e);
		}
	}

	private String triggerExecution(String iflowId) throws IOException, InterruptedException {
		String baseUrl = this.baseUrl;
		String url = baseUrl + "/ExecuteIntegrationDesigntimeArtifactsGuidelines"
			+ "?Id='" + URLEncoder.encode(iflowId, StandardCharsets.UTF_8) + "'"
			+ "&Version='active'";

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("Authorization", "Bearer " + oauthToken)
			.header("X-CSRF-Token", csrfToken)
			.header("Accept", "application/json")
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new RuntimeException("ExecuteIntegrationDesigntimeArtifactsGuidelines failed with status "
				+ response.statusCode() + ": " + response.body());
		}
		// The endpoint returns the execution ID as a plain string (not JSON)
		return response.body().trim();
	}

	private void waitForCompletion(String iflowId, String executionId) throws IOException, InterruptedException {
		String baseUrl = this.baseUrl;
		String url = baseUrl + "/IntegrationDesigntimeArtifacts(Id='" + iflowId + "',Version='active')"
			+ "/DesignGuidelineExecutionResults('" + executionId + "')?$format=json";

		for (int attempt = 0; attempt < POLL_MAX_ATTEMPTS; attempt++) {
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Authorization", "Bearer " + oauthToken)
				.header("Accept", "application/json")
				.GET()
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				String status = new JSONObject(response.body()).getJSONObject("d").optString("ExecutionStatus", "");
				if (!status.isEmpty() && !"RUNNING".equalsIgnoreCase(status) && !"PENDING".equalsIgnoreCase(status)) {
					return;
				}
			}
			Thread.sleep(POLL_INTERVAL_MS);
		}
		throw new RuntimeException("Design guidelines execution timed out for execution ID: " + executionId);
	}

	private void reportViolations(String iflowId, String executionId, IflowArtifactTag tag) throws IOException, InterruptedException {
		String baseUrl = this.baseUrl;
		String url = baseUrl + "/IntegrationDesigntimeArtifacts(Id='" + iflowId + "',Version='active')"
			+ "/DesignGuidelineExecutionResults('" + executionId + "')"
			+ "?$expand=DesignGuidelines&$format=json";

		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create(url))
			.header("Authorization", "Bearer " + oauthToken)
			.header("Accept", "application/json")
			.GET()
			.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new RuntimeException("Failed to fetch execution results (status " + response.statusCode() + "): " + response.body());
		}
		JSONObject d = new JSONObject(response.body()).getJSONObject("d");
		JSONObject details = d.optJSONObject("DesignGuidelines");
		if (details == null) {
			return;
		}
		JSONArray results = details.optJSONArray("results");
		if (results == null) {
			return;
		}

		for (int i = 0; i < results.length(); i++) {
			JSONObject detail = results.getJSONObject(i);
			String compliance = detail.optString("Compliance", "");
			if ("Non-Compliant".equalsIgnoreCase(compliance)) {
				String guidelineName = detail.optString("GuidelineName", "Unknown guideline");
				String severity = detail.optString("Severity", "");
				String expectedKpi = detail.optString("ExpectedKPI", "");
				String actualKpi = detail.optString("ActualKPI", "");
				String message = String.format("[%s] %s | Expected: %s | Actual: %s",
					severity, guidelineName, expectedKpi, actualKpi);
				consumer.consume(new DesignGuidelinesIssue(tag, message));
			}
		}
	}

}
