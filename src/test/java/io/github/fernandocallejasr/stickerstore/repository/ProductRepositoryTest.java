package io.github.fernandocallejasr.stickerstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fernandocallejasr.stickerstore.config.JpaAuditingConfig;
import io.github.fernandocallejasr.stickerstore.constants.ApplicationConstants;
import io.github.fernandocallejasr.stickerstore.entity.Product;
import io.github.fernandocallejasr.stickerstore.security.AuthenticatedUser;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class ProductRepositoryTest {

	@Autowired
	ProductRepository productRepository;
	@Autowired
	TestEntityManager entityManager;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	private Product newProduct() {
		Product product = new Product();
		product.setName("Test");
		product.setDescription("desc");
		product.setPrice(new BigDecimal("9.99"));
		product.setPopularity(50);
		product.setImageUrl("/stickers/test.png");

		return product;
	}

	@Test
	void saveProductAndFindRoundTrip() {
		Product savedProduct = productRepository.save(newProduct());

		entityManager.flush();
		entityManager.clear();

		Product foundProduct = productRepository.findById(savedProduct.getProductId()).orElseThrow();
		assertThat(foundProduct.getProductId()).isNotNull();
		assertThat(foundProduct.getName()).isEqualTo("Test");
		assertThat(foundProduct.getPrice()).isEqualByComparingTo("9.99");
		assertThat(foundProduct.getPopularity()).isEqualTo(50);
		assertThat(foundProduct.getImageUrl()).isEqualTo("/stickers/test.png");
		assertThat(foundProduct.getCreatedAt()).isNotNull();
		assertThat(foundProduct.getCreatedBy()).isEqualTo("SYSTEM");
		assertThat(foundProduct.getUpdatedAt()).isNull();
		assertThat(foundProduct.getUpdatedBy()).isNull();
	}

	@Test
	void updatingProductStampsModificationAuditFields() {
		Product saved = productRepository.save(newProduct());
		entityManager.flush();
		entityManager.clear();

		Product toUpdate = productRepository.findById(saved.getProductId()).orElseThrow();
		toUpdate.setName("Test Updated");
		productRepository.save(toUpdate);
		entityManager.flush();
		entityManager.clear();

		Product found = productRepository.findById(saved.getProductId()).orElseThrow();
		assertThat(found.getName()).isEqualTo("Test Updated");
		assertThat(found.getUpdatedAt()).isNotNull();
		assertThat(found.getUpdatedBy()).isEqualTo(ApplicationConstants.SYSTEM_AUDITOR);
	}

	@Test
	void savingWhileAuthenticatedStampsTheUserEmail() {
		String longEmail = "product-auditor-with-a-deliberately-long-address@example.com";
		AuthenticatedUser principal = new AuthenticatedUser(1L, "Auditor", longEmail, "5550000100");
		SecurityContextHolder.getContext()
				.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(principal, null,
						AuthorityUtils.createAuthorityList(ApplicationConstants.DEFAULT_ROLE)));

		Product saved = productRepository.save(newProduct());
		entityManager.flush();
		entityManager.clear();

		Product found = productRepository.findById(saved.getProductId()).orElseThrow();
		assertThat(found.getCreatedBy()).isEqualTo(longEmail);
	}

	@Test
	void savingAsAnonymousFallsBackToSystem() {
		SecurityContextHolder.getContext()
				.setAuthentication(new AnonymousAuthenticationToken("key", ApplicationConstants.ANONYMOUS_PRINCIPAL,
						AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

		Product saved = productRepository.save(newProduct());
		entityManager.flush();
		entityManager.clear();

		Product found = productRepository.findById(saved.getProductId()).orElseThrow();
		assertThat(found.getCreatedBy()).isEqualTo(ApplicationConstants.SYSTEM_AUDITOR);
	}
}
