package io.github.fernandocallejasr.stickerstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fernandocallejasr.stickerstore.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
public class ProductRepositoryTest {

	@Autowired
	ProductRepository productRepository;
	@Autowired
	TestEntityManager entityManager;

	@Test
	void saveProductAndFindRoundTrip() {
		Instant nowInstant = Instant.now();

		Product p = new Product();
		p.setName("Test");
		p.setDescription("desc");
		p.setPrice(new BigDecimal("9.99"));
		p.setPopularity(50);
		p.setImageUrl("/stickers/test.png");
		p.setCreatedAt(nowInstant);
		p.setCreatedBy("test");

		Product savedProduct = productRepository.save(p);

		entityManager.flush();
		entityManager.clear();

		Product foundProduct = productRepository.findById(savedProduct.getProductId()).orElseThrow();
		assertThat(foundProduct.getProductId()).isNotNull();
		assertThat(foundProduct.getName()).isEqualTo("Test");
		assertThat(foundProduct.getPrice()).isEqualByComparingTo("9.99");
		assertThat(foundProduct.getPopularity()).isEqualTo(50);
		assertThat(foundProduct.getImageUrl()).isEqualTo("/stickers/test.png");
		assertThat(foundProduct.getCreatedAt()).isEqualTo(nowInstant);
	}
}
