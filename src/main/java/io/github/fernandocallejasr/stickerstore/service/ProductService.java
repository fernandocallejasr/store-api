package io.github.fernandocallejasr.stickerstore.service;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import java.util.List;

public interface ProductService {
	List<ProductResponse> getProducts();
	ProductResponse getProductById(long productId);
}
