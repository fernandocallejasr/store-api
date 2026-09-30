package io.github.fernandocallejasr.stickerstore.dto;

public record LoginResponse(String message,
		UserDto user,
		String jwtToken) {
}
