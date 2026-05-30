package io.github.fernandocallejasr.stickerstore.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import io.github.fernandocallejasr.stickerstore.exception.GlobalExceptionHandler;
import io.github.fernandocallejasr.stickerstore.exception.ProductNotFoundException;
import io.github.fernandocallejasr.stickerstore.service.ProductService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
public class ProductControllerTest {

	@Autowired
	MockMvc mockMvc;
	@MockitoBean
	ProductService productService;

	private static final String PRODUCTS_PATH = "/api/v1/products";

	@Test
	void getProducts_returns200WithJsonArray() throws Exception {
		ProductResponse productResponse = new ProductResponse(1L, "Break", "desc", new BigDecimal("4.50"), 40,
				"/stickers/break.png", Instant.parse("2026-01-01T00:00:00Z"));

		when(productService.getProducts()).thenReturn(List.of(productResponse));

		mockMvc.perform(get(PRODUCTS_PATH)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].productId").value(1)).andExpect(jsonPath("$[0].name").value("Break"))
				.andExpect(jsonPath("$[0].price").value(4.50)).andExpect(content().contentType("application/json"));

	}

	@Test
	void getProductById_returns200WithJson() throws Exception {
		ProductResponse productResponse = new ProductResponse(1L, "Break", "desc", new BigDecimal("4.50"), 40,
				"/stickers/break.png", Instant.parse("2026-01-01T00:00:00Z"));

		when(productService.getProductById(1L)).thenReturn(productResponse);

		mockMvc.perform(get(PRODUCTS_PATH + "/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.productId").value(1)).andExpect(jsonPath("$.description").value("desc"))
				.andExpect(jsonPath("$.name").value("Break")).andExpect(jsonPath("$.price").value(4.50))
				.andExpect(jsonPath("$.imageUrl").value("/stickers/break.png"))
				.andExpect(content().contentType("application/json"));
	}

	@Test
	void getProductById_returns400WhenIdNotPositive() throws Exception {
		mockMvc.perform(get(PRODUCTS_PATH + "/0")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("Request validation failed"));
	}

	@Test
	void getProductById_returns404WhenNotFound() throws Exception {
		when(productService.getProductById(999L)).thenThrow(new ProductNotFoundException(999L));

		mockMvc.perform(get(PRODUCTS_PATH + "/999")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.detail").value("Product not found for id: 999"));
	}
}
