package io.github.fernandocallejasr.stickerstore.dto;

import io.github.fernandocallejasr.stickerstore.util.Emails;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "Username is required") String username,
		@NotBlank(message = "Password is required") String password) {

	public LoginRequest {
		username = Emails.normalize(username);
	}
}
