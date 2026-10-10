package io.github.fernandocallejasr.stickerstore.exception;

public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(long userId) {
		super("User not found for id: " + userId);
	}
}
