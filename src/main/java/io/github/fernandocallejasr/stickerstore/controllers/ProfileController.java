package io.github.fernandocallejasr.stickerstore.controllers;

import io.github.fernandocallejasr.stickerstore.dto.UserDto;
import io.github.fernandocallejasr.stickerstore.security.AuthenticatedUser;
import io.github.fernandocallejasr.stickerstore.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

	private final UserService userService;

	public ProfileController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
		return ResponseEntity.ok(userService.getProfile(authenticatedUser.userId()));
	}
}
