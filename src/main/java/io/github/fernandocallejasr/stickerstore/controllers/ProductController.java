package io.github.fernandocallejasr.stickerstore.controllers;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import io.github.fernandocallejasr.stickerstore.service.ProductService;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public ResponseEntity<List<ProductResponse>> getProducts() {
		return ResponseEntity.ok(productService.getProducts());
	}

	@GetMapping("/{productId}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable @Min(1) long productId) {
		return ResponseEntity.ok(productService.getProductById(productId));
	}

}
