package io.github.fernandocallejasr.stickerstore;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("dev")
public class ProductApiIntegrationTest {

	@Autowired
	TestRestTemplate restTemplate;

	private final String PRODUCTS_PATH = "/api/v1/products";
	private final String VITE_LOCAL_ORIGIN = "http://localhost:5173";

	@Test
	void getProducts_returnsSeededData() {
		int NUMBER_OF_PRODUCTS = 19;
		ResponseEntity<ProductResponse[]> response = restTemplate.getForEntity(PRODUCTS_PATH,
				ProductResponse[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(NUMBER_OF_PRODUCTS);
		assertThat(response.getBody()[0].name()).isNotNull();
	}

	@Test
	void corsPreflight_allowsViteOrigin() {
		HttpHeaders headers = new HttpHeaders();
		headers.setOrigin(VITE_LOCAL_ORIGIN);
		headers.set("Access-Control-Request-Method", "GET");

		ResponseEntity<Void> response = restTemplate.exchange(PRODUCTS_PATH, HttpMethod.OPTIONS,
				new HttpEntity<>(headers), Void.class);

		assertThat(response.getHeaders().getAccessControlAllowOrigin()).isEqualTo("http://localhost:5173");
	}

	@Test
	void corsPreflight_allowsAuthorizationHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setOrigin(VITE_LOCAL_ORIGIN);
		headers.setAccessControlRequestMethod(HttpMethod.GET);
		headers.setAccessControlRequestHeaders(List.of(HttpHeaders.AUTHORIZATION));

		ResponseEntity<Void> response = restTemplate.exchange(PRODUCTS_PATH, HttpMethod.OPTIONS,
				new HttpEntity<>(headers), Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getAccessControlAllowOrigin()).isEqualTo("http://localhost:5173");
		assertThat(response.getHeaders().getAccessControlAllowHeaders()).contains(HttpHeaders.AUTHORIZATION);
	}

	@Test
	void corsPreflight_rejectsHeaderOutsideAllowList() {
		HttpHeaders headers = new HttpHeaders();
		headers.setOrigin(VITE_LOCAL_ORIGIN);
		headers.setAccessControlRequestMethod(HttpMethod.GET);
		headers.setAccessControlRequestHeaders(List.of("X-Not-Allowed"));

		ResponseEntity<Void> response = restTemplate.exchange(PRODUCTS_PATH, HttpMethod.OPTIONS,
				new HttpEntity<>(headers), Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(response.getHeaders().getAccessControlAllowOrigin()).isNull();
	}

	@Test
	void corsResponse_exposesRequestIdHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setOrigin(VITE_LOCAL_ORIGIN);

		ResponseEntity<Void> response = restTemplate.exchange(PRODUCTS_PATH, HttpMethod.GET,
				new HttpEntity<>(headers), Void.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getHeaders().getAccessControlExposeHeaders()).contains("X-Request-Id");
		assertThat(response.getHeaders().getFirst("X-Request-Id")).isNotBlank();
	}

}
