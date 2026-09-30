package io.github.fernandocallejasr.stickerstore.exception;

public class MobileNumberAlreadyRegisteredException extends RuntimeException {
	public MobileNumberAlreadyRegisteredException(String mobileNumber) {
		super("Mobile number already registered");
	}
}
