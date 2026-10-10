package io.github.fernandocallejasr.stickerstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.fernandocallejasr.stickerstore.dto.UserDto;
import io.github.fernandocallejasr.stickerstore.entity.User;
import io.github.fernandocallejasr.stickerstore.exception.UserNotFoundException;
import io.github.fernandocallejasr.stickerstore.repository.UserRepository;
import io.github.fernandocallejasr.stickerstore.service.impl.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

	@Mock
	UserRepository userRepository;

	UserServiceImpl userService;

	@BeforeEach
	void setUp() {
		userService = new UserServiceImpl(userRepository);
	}

	@Test
	void getProfile_returnsMappedDtoWhenFound() {
		User user = new User();
		ReflectionTestUtils.setField(user, "userId", 7L);
		user.setName("Profile User");
		user.setEmail("profile@example.com");
		user.setMobileNumber("5551112222");
		user.setPasswordHash("$2a$10$hash");

		when(userRepository.findById(7L)).thenReturn(Optional.of(user));

		UserDto result = userService.getProfile(7L);

		assertThat(result.getUserId()).isEqualTo(7L);
		assertThat(result.getName()).isEqualTo("Profile User");
		assertThat(result.getEmail()).isEqualTo("profile@example.com");
		assertThat(result.getMobileNumber()).isEqualTo("5551112222");
	}

	@Test
	void getProfile_throwsUserNotFoundExceptionWhenMissing() {
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getProfile(999L)).isInstanceOf(UserNotFoundException.class)
				.hasMessageContaining("999");
	}
}
