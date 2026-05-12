package io.github.fernandocallejasr.stickerstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import io.github.fernandocallejasr.stickerstore.entity.Product;
import io.github.fernandocallejasr.stickerstore.exception.ProductNotFoundException;
import io.github.fernandocallejasr.stickerstore.repository.ProductRepository;
import io.github.fernandocallejasr.stickerstore.service.impl.ProductServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

	@Mock
	ProductRepository productRepository;

	MeterRegistry meterRegistry;
	ProductServiceImpl productService;

	@BeforeEach
	void setUp() {
		meterRegistry = new SimpleMeterRegistry();
		productService = new ProductServiceImpl(productRepository, meterRegistry);
	}

	@Test
	void getProducts_mapsEntitiesToResponses() {
		Instant testInstant = Instant.now();

		Product product = new Product();
		product.setName("Break");
		product.setDescription("break desc");
		product.setPrice(new BigDecimal("13.13"));
		product.setPopularity(10);
		product.setImageUrl("/stickers/break.png");
		product.setCreatedAt(testInstant);
		product.setCreatedBy("test-creator");

		when(productRepository.findAll()).thenReturn(List.of(product));

		List<ProductResponse> result = productService.getProducts();

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().name()).isEqualTo("Break");
		assertThat(result.getFirst().description()).isEqualTo("break desc");
		assertThat(result.getFirst().price()).isEqualByComparingTo("13.13");
		assertThat(result.getFirst().popularity()).isEqualTo(10);
		assertThat(result.getFirst().imageUrl()).isEqualTo("/stickers/break.png");
		assertThat(result.getFirst().createdAt()).isEqualTo(testInstant);
	}

	@Test
	void getProductById_returnsResponseWhenFound() {
		Instant testInstant = Instant.now();

		Product product = new Product();
		product.setName("Break");
		product.setDescription("break desc");
		product.setPrice(new BigDecimal("13.13"));
		product.setPopularity(10);
		product.setImageUrl("/stickers/break.png");
		product.setCreatedAt(testInstant);
		product.setCreatedBy("test-creator");

		when(productRepository.findById(1L)).thenReturn(Optional.of(product));

		ProductResponse result = productService.getProductById(1L);

		assertThat(result.name()).isEqualTo("Break");
		assertThat(result.description()).isEqualTo("break desc");
		assertThat(result.price()).isEqualByComparingTo("13.13");
		assertThat(result.popularity()).isEqualTo(10);
		assertThat(result.imageUrl()).isEqualTo("/stickers/break.png");
		assertThat(result.createdAt()).isEqualTo(testInstant);
	}

	@Test
	void getProductById_throwsProductNotFoundExceptionWhenMissing() {
		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.getProductById(999L)).isInstanceOf(ProductNotFoundException.class)
				.hasMessageContaining("999");
	}

	@Test
	void getProducts_recordsListTimer() {
		when(productRepository.findAll()).thenReturn(List.of());

		productService.getProducts();
		productService.getProducts();

		assertThat(Objects.requireNonNull(meterRegistry.find("products.list").timer()).count()).isEqualTo(2L);
	}

	@Test
	void getProductById_incrementsNotFoundCounterWhenMissing() {
		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.getProductById(999L)).isInstanceOf(ProductNotFoundException.class);

		assertThat(Objects.requireNonNull(meterRegistry.find("products.notfound").counter()).count()).isEqualTo(1.0);
	}

	@Test
	void getProductById_doesNotIncrementNotFoundCounterWhenFound() {
		when(productRepository.findById(1L)).thenReturn(Optional.of(new Product()));

		productService.getProductById(1L);

		assertThat(meterRegistry.find("products.notfound").counter().count()).isEqualTo(0.0);
	}
}
