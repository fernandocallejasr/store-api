package io.github.fernandocallejasr.stickerstore.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.fernandocallejasr.stickerstore.entity.User;
import io.github.fernandocallejasr.stickerstore.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class StoreUsernamePwdAuthenticationProviderTest {

	@Mock
	UserRepository userRepository;

	final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	StoreUsernamePwdAuthenticationProvider provider;

	@BeforeEach
	void setUp() {
		provider = new StoreUsernamePwdAuthenticationProvider(userRepository, passwordEncoder);
	}

	private User storedUser(String email, String rawPassword) {
		User user = new User();
		user.setName("Test User");
		user.setEmail(email);
		user.setMobileNumber("5551230001");
		user.setPasswordHash(passwordEncoder.encode(rawPassword));

		return user;
	}

	@Test
	void returnsAuthenticatedTokenCarryingDefaultRole() {
		when(userRepository.findByEmail("user@example.com"))
				.thenReturn(Optional.of(storedUser("user@example.com", "password123")));

		Authentication result = provider.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated("user@example.com", "password123"));

		Assertions.assertNotNull(result);
		assertThat(result.isAuthenticated()).isTrue();
		assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_USER");
		assertThat(result.getCredentials()).isNull();
		assertThat(result.getPrincipal()).isInstanceOf(AuthenticatedUser.class);

		AuthenticatedUser principal = (AuthenticatedUser) result.getPrincipal();
		Assertions.assertNotNull(principal);
		assertThat(principal.email()).isEqualTo("user@example.com");
		assertThat(principal.name()).isEqualTo("Test User");
		assertThat(principal.mobileNumber()).isEqualTo("5551230001");
	}

	@Test
	void rejectsWrongPasswordWithBadCredentials() {
		when(userRepository.findByEmail("user@example.com"))
				.thenReturn(Optional.of(storedUser("user@example.com", "password123")));

		assertThatThrownBy(() -> provider.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated("user@example.com", "wrongpassword")))
				.isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void rejectsUnknownEmail() {
		when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> provider.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated("nobody@example.com", "password123")))
				.isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	void supportsUsernamePasswordAuthenticationTokens() {
		assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
	}

	@Test
	void doesNotSupportOtherAuthenticationTokenTypes() {
		assertThat(provider.supports(AnonymousAuthenticationToken.class)).isFalse();
	}
}
