package io.github.fernandocallejasr.stickerstore.dto;

import io.github.fernandocallejasr.stickerstore.entity.User;
import io.github.fernandocallejasr.stickerstore.security.AuthenticatedUser;

public class UserDto {
	private Long userId;
	private String name;
	private String email;
	private String mobileNumber;

	public static UserDto from(User user) {
		return of(user.getUserId(), user.getName(), user.getEmail(), user.getMobileNumber());
	}

	public static UserDto from(AuthenticatedUser authenticatedUser) {
		return of(authenticatedUser.userId(), authenticatedUser.name(), authenticatedUser.email(),
				authenticatedUser.mobileNumber());
	}

	private static UserDto of(Long userId, String name, String email, String mobileNumber) {
		UserDto userDto = new UserDto();
		userDto.setUserId(userId);
		userDto.setName(name);
		userDto.setEmail(email);
		userDto.setMobileNumber(mobileNumber);

		return userDto;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
}
