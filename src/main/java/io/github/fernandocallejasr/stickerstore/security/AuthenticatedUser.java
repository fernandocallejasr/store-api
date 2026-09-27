package io.github.fernandocallejasr.stickerstore.security;

import io.github.fernandocallejasr.stickerstore.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticatedPrincipal;

public record AuthenticatedUser(Long userId,
		String name,
		String email,
		String mobileNumber) implements AuthenticatedPrincipal {

	public static AuthenticatedUser from(User user) {
		return new AuthenticatedUser(user.getUserId(),
				user.getName(),
				user.getEmail(),
				user.getMobileNumber());
	}

	@Override
	@NonNull
	public String getName() {
		return email;
	}
}
