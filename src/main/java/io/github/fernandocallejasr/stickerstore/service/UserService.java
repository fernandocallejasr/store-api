package io.github.fernandocallejasr.stickerstore.service;

import io.github.fernandocallejasr.stickerstore.dto.UserDto;

public interface UserService {
	UserDto getProfile(long userId);
}
