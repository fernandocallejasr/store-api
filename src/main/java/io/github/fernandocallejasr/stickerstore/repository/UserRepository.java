package io.github.fernandocallejasr.stickerstore.repository;

import io.github.fernandocallejasr.stickerstore.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByMobileNumber(String mobileNumber);
}
