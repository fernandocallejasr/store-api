package io.github.fernandocallejasr.stickerstore.controllers;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import io.github.fernandocallejasr.stickerstore.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ProductService productService;

    private static final String PRODUCTS_PATH = "/api/v1/products";

    @Test
    void getProducts_returns200WithJsonArray() throws Exception {
        ProductResponse productResponse = new ProductResponse(1L,
                "Break",
                "desc",
                new BigDecimal("4.50"),
                40,
                "/stickers/break.png",
                Instant.parse("2026-01-01T00:00:00Z"));

        when(productService.getProducts()).thenReturn(List.of(productResponse));

        mockMvc.perform(get(PRODUCTS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].name").value("Break"))
                .andExpect(jsonPath("$[0].price").value(4.50))
                .andExpect(content().contentType("application/json"));

    }
}
