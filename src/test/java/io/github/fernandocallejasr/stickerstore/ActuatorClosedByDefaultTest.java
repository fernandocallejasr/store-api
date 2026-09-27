package io.github.fernandocallejasr.stickerstore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "app.cors.allowed-origins=http://localhost:5173")
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
public class ActuatorClosedByDefaultTest {

	@Autowired
	TestRestTemplate restTemplate;
	@Autowired
	Environment environment;

	@Test
	void runsWithoutTheDevProfile() {
		assertThat(environment.getActiveProfiles()).containsExactly("test");
	}

	@Test
	void publicActuatorPathsAreNotServedWithoutDevGrants() {
		assertThat(restTemplate.getForEntity("/actuator/info", String.class).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(restTemplate.getForEntity("/actuator/prometheus", String.class).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(restTemplate.getForEntity("/actuator/health", String.class).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(restTemplate.getForEntity("/actuator/health/liveness", String.class).getStatusCode())
				.isEqualTo(HttpStatus.NOT_FOUND);
	}
}
