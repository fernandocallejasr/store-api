package io.github.fernandocallejasr.stickerstore.repository;

import io.github.fernandocallejasr.stickerstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
