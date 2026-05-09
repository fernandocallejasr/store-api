package io.github.fernandocallejasr.stickerstore.service.impl;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import io.github.fernandocallejasr.stickerstore.exception.ProductNotFoundException;
import io.github.fernandocallejasr.stickerstore.repository.ProductRepository;
import io.github.fernandocallejasr.stickerstore.service.ProductService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public List<ProductResponse> getProducts() {
		return productRepository.findAll()
								.stream()
								.map(ProductResponse::from)
								.toList();
	}

	@Override
	public ProductResponse getProductById(long productId) {
		return productRepository.findById(productId)
								.map(ProductResponse::from)
								.orElseThrow(() -> new ProductNotFoundException(productId));
	}
}
