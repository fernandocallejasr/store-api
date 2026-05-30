package io.github.fernandocallejasr.stickerstore.dto;

import io.github.fernandocallejasr.stickerstore.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(Long productId,
		String name,
		String description,
		BigDecimal price,
		Integer popularity,
		String imageUrl,
		Instant createdAt) {

	public static ProductResponse from(Product p) {
		return new ProductResponse(p.getProductId(),
				p.getName(),
				p.getDescription(),
				p.getPrice(),
				p.getPopularity(),
				p.getImageUrl(),
				p.getCreatedAt());
	}
}
