package io.github.fernandocallejasr.stickerstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fernandocallejasr.stickerstore.config.JpaAuditingConfig;
import io.github.fernandocallejasr.stickerstore.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class UserRepositoryTest {

	@Autowired
	UserRepository userRepository;
	@Autowired
	TestEntityManager entityManager;

	private User newUser(String email, String mobileNumber) {
		User user = new User();
		user.setName("UserName");
		user.setEmail(email);
		user.setMobileNumber(mobileNumber);
		user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuv");

		return user;
	}

	@Test
	void savingUserStampsCreationAuditFieldsAndLeavesUpdateFieldsNull() {
		User saved = userRepository.save(newUser("user@example.com", "5551230001"));

		entityManager.flush();
		entityManager.clear();

		User found = userRepository.findById(saved.getUserId()).orElseThrow();
		assertThat(found.getUserId()).isNotNull();
		assertThat(found.getName()).isEqualTo("UserName");
		assertThat(found.getEmail()).isEqualTo("user@example.com");
		assertThat(found.getMobileNumber()).isEqualTo("5551230001");
		assertThat(found.getPasswordHash()).isEqualTo("$2a$10$abcdefghijklmnopqrstuv");
		assertThat(found.getCreatedAt()).isNotNull();
		assertThat(found.getCreatedBy()).isEqualTo("SYSTEM");
		assertThat(found.getUpdatedAt()).isNull();
		assertThat(found.getUpdatedBy()).isNull();
	}

	@Test
	void updatingUserStampsModificationAuditFields() {
		User saved = userRepository.save(newUser("update@example.com", "5551230002"));
		entityManager.flush();
		entityManager.clear();

		User toUpdate = userRepository.findById(saved.getUserId()).orElseThrow();
		toUpdate.setName("UserName Updated");
		userRepository.save(toUpdate);
		entityManager.flush();
		entityManager.clear();

		User found = userRepository.findById(saved.getUserId()).orElseThrow();
		assertThat(found.getName()).isEqualTo("UserName Updated");
		assertThat(found.getUpdatedAt()).isNotNull();
		assertThat(found.getUpdatedBy()).isEqualTo("SYSTEM");
	}

	@Test
	void findByEmailReturnsMatchingUser() {
		userRepository.save(newUser("lookup@example.com", "5551230003"));
		entityManager.flush();
		entityManager.clear();

		Optional<User> found = userRepository.findByEmail("lookup@example.com");

		assertThat(found).isPresent();
		assertThat(found.get().getMobileNumber()).isEqualTo("5551230003");
	}

	@Test
	void findByEmailReturnsEmptyWhenEmailIsUnknown() {
		assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
	}

	@Test
	void existsByMobileNumberDistinguishesRegisteredFromUnknownNumbers() {
		userRepository.save(newUser("mobile@example.com", "5551230004"));
		entityManager.flush();
		entityManager.clear();

		assertThat(userRepository.existsByMobileNumber("5551230004")).isTrue();
		assertThat(userRepository.existsByMobileNumber("5559999999")).isFalse();
	}
}
