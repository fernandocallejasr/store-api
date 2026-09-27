package io.github.fernandocallejasr.stickerstore.security;

import io.github.fernandocallejasr.stickerstore.constants.ApplicationConstants;
import io.github.fernandocallejasr.stickerstore.entity.User;
import io.github.fernandocallejasr.stickerstore.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class StoreUsernamePwdAuthenticationProvider implements AuthenticationProvider {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public StoreUsernamePwdAuthenticationProvider(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = Objects.requireNonNull(authentication.getCredentials()).toString();

		User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("User details not found for user: " + username));
		AuthenticatedUser authenticatedUser = AuthenticatedUser.from(user);

		if (passwordEncoder.matches(password, user.getPasswordHash())) {
			return UsernamePasswordAuthenticationToken.authenticated(authenticatedUser, null,
					List.of(new SimpleGrantedAuthority(ApplicationConstants.DEFAULT_ROLE)));
		}

		throw new BadCredentialsException("Invalid password");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
