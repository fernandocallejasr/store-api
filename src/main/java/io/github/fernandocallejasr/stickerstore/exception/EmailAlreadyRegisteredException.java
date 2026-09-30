package io.github.fernandocallejasr.stickerstore.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
	public EmailAlreadyRegisteredException(String email) {
		super("Email already registered");
	}
}
