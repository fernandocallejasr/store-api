package io.github.fernandocallejasr.stickerstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import io.github.fernandocallejasr.stickerstore.entity.Product;
import io.github.fernandocallejasr.stickerstore.exception.ProductNotFoundException;
import io.github.fernandocallejasr.stickerstore.repository.ProductRepository;
import io.github.fernandocallejasr.stickerstore.service.impl.ProductServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

	@Mock
	ProductRepository productRepository;
	@InjectMocks
	ProductServiceImpl productService;

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
}
