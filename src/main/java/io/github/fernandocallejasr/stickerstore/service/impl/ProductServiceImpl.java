package io.github.fernandocallejasr.stickerstore.service.impl;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import io.github.fernandocallejasr.stickerstore.exception.ProductNotFoundException;
import io.github.fernandocallejasr.stickerstore.repository.ProductRepository;
import io.github.fernandocallejasr.stickerstore.service.ProductService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final Timer productsListTimer;
	private final Counter productsNotFoundCounter;

	public ProductServiceImpl(ProductRepository productRepository, MeterRegistry meterRegistry) {
		this.productRepository = productRepository;
		this.productsListTimer = Timer.builder("products.list").description("Latency of ProductService.getProducts()")
				.publishPercentileHistogram().register(meterRegistry);
		this.productsNotFoundCounter = Counter.builder("products.notfound")
				.description("Lookups by id that returned no product.").register(meterRegistry);
	}

	@Override
	public List<ProductResponse> getProducts() {
		return productsListTimer.record(() -> productRepository.findAll().stream().map(ProductResponse::from).toList());
	}

	@Override
	public ProductResponse getProductById(long productId) {
		return productRepository.findById(productId).map(ProductResponse::from).orElseThrow(() -> {
			productsNotFoundCounter.increment();
			return new ProductNotFoundException(productId);
		});
	}
}
