package io.github.fernandocallejasr.stickerstore;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("dev")
public class ProductApiIntegrationTest {

    @Autowired TestRestTemplate restTemplate;

    @Test
    void getProducts_returnsSeededData() {
        ResponseEntity<ProductResponse[]> response = restTemplate.getForEntity("/api/v1/products", ProductResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(29);
        assertThat(response.getBody()[0].name()).isNotNull();
    }

}
