package io.github.fernandocallejasr.stickerstore;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fernandocallejasr.stickerstore.constants.ApplicationConstants;
import io.github.fernandocallejasr.stickerstore.security.AuthenticatedUser;
import io.github.fernandocallejasr.stickerstore.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("dev")
public class ActuatorEndpointsTest {

	private static final String METRICS_PATH = "/actuator/metrics";

	@Autowired
	TestRestTemplate restTemplate;
	@Autowired
	JwtUtil jwtUtil;

	@Test
	void healthEndpoint_returnsUp() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("\"status\":\"UP\"");
	}

	@Test
	void healthLivenessProbe_isReachable() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health/liveness", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("\"status\":\"UP\"");
	}

	@Test
	void healthReadinessProbe_isReachable() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health/readiness", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("\"status\":\"UP\"");
	}

	@Test
	void infoEndpoint_exposesBuildInfo() {
		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/info", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("\"build\"").contains("stickerstore");
	}

	@Test
	void prometheusEndpoint_exposesMicrometerAndCustomMetrics() {
		// Touch the products endpoint so the custom timer is recorded at least once.
		restTemplate.getForEntity("/api/v1/products", String.class);

		ResponseEntity<String> response = restTemplate.getForEntity("/actuator/prometheus", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("jvm_memory_used_bytes").contains("products_list_seconds")
				.contains("application=\"stickerstore\"");
	}

	@Test
	void metricsEndpoint_rejectsRequestWithoutToken() {
		ResponseEntity<String> response = restTemplate.getForEntity(METRICS_PATH, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void metricsEndpoint_acceptsValidBearerToken() {
		AuthenticatedUser user = new AuthenticatedUser(1L, "Metrics Reader", "metrics@example.com", "5550000099");
		String token = jwtUtil.generateJwtToken(UsernamePasswordAuthenticationToken.authenticated(user, null,
				AuthorityUtils.createAuthorityList(ApplicationConstants.DEFAULT_ROLE)));

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);

		ResponseEntity<String> response = restTemplate.exchange(METRICS_PATH, HttpMethod.GET,
				new HttpEntity<>(headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("jvm.memory.used");
	}

	@Test
	void sensitiveEndpoints_areNotExposed() {
		// Not in publicPaths, so security rejects these before routing can 404 them.
		assertThat(restTemplate.getForEntity("/actuator/env", String.class).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(restTemplate.getForEntity("/actuator/beans", String.class).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(restTemplate.getForEntity("/actuator/heapdump", String.class).getStatusCode())
				.isEqualTo(HttpStatus.UNAUTHORIZED);
	}
}
