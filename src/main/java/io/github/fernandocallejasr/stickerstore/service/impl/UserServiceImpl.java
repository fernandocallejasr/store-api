package io.github.fernandocallejasr.stickerstore.service.impl;

import io.github.fernandocallejasr.stickerstore.dto.UserDto;
import io.github.fernandocallejasr.stickerstore.exception.UserNotFoundException;
import io.github.fernandocallejasr.stickerstore.repository.UserRepository;
import io.github.fernandocallejasr.stickerstore.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDto getProfile(long userId) {
		return userRepository.findById(userId).map(UserDto::from).orElseThrow(() -> new UserNotFoundException(userId));
	}
}
