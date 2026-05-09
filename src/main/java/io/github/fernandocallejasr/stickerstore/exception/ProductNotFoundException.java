package io.github.fernandocallejasr.stickerstore.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(long productId) {
        super("Product not found for id: " + productId);
    }
}
