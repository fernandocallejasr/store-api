package io.github.fernandocallejasr.stickerstore.service.impl;

import io.github.fernandocallejasr.stickerstore.dto.ProductResponse;
import io.github.fernandocallejasr.stickerstore.exception.ProductNotFoundException;
import io.github.fernandocallejasr.stickerstore.repository.ProductRepository;
import io.github.fernandocallejasr.stickerstore.service.ProductService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

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
		log.debug("Fetching all products");
		Timer.Sample sample = Timer.start();
		try {
			List<ProductResponse> products = productRepository.findAll().stream().map(ProductResponse::from).toList();
			log.debug("Fetched {} products", products.size());
			return products;
		} finally {
			sample.stop(productsListTimer);
		}
	}

	@Override
	public ProductResponse getProductById(long productId) {
		log.debug("Looking up product {}", productId);
		return productRepository.findById(productId).map(ProductResponse::from).orElseThrow(() -> {
			productsNotFoundCounter.increment();
			return new ProductNotFoundException(productId);
		});
	}
}
