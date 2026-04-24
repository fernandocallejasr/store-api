package io.github.fernandocallejasr.stickerstore.service;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import io.github.fernandocallejasr.stickerstore.entity.Product;
import io.github.fernandocallejasr.stickerstore.repository.ProductRepository;
import io.github.fernandocallejasr.stickerstore.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock ProductRepository productRepository;
    @InjectMocks ProductServiceImpl productService;

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
}
