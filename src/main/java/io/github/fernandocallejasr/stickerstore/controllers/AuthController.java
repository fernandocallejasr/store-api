package io.github.fernandocallejasr.stickerstore.controllers;

import io.github.fernandocallejasr.stickerstore.dto.LoginRequest;
import io.github.fernandocallejasr.stickerstore.dto.LoginResponse;
import io.github.fernandocallejasr.stickerstore.dto.RegisterRequest;
import io.github.fernandocallejasr.stickerstore.dto.UserDto;
import io.github.fernandocallejasr.stickerstore.entity.User;
import io.github.fernandocallejasr.stickerstore.exception.EmailAlreadyRegisteredException;
import io.github.fernandocallejasr.stickerstore.exception.MobileNumberAlreadyRegisteredException;
import io.github.fernandocallejasr.stickerstore.repository.UserRepository;
import io.github.fernandocallejasr.stickerstore.security.AuthenticatedUser;
import io.github.fernandocallejasr.stickerstore.util.JwtUtil;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthController(AuthenticationManager authenticationManager,
			JwtUtil jwtUtil,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				loginRequest.username(), loginRequest.password());
		Authentication authentication = authenticationManager.authenticate(authenticationToken);
		String jwtToken = jwtUtil.generateJwtToken(authentication);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new LoginResponse(HttpStatus.OK.getReasonPhrase(),
						getUserDto(authentication),
						jwtToken));
	}

	@PostMapping("/register")
	public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
		if (userRepository.findByEmail(registerRequest.getEmail()).isPresent())
			throw new EmailAlreadyRegisteredException(registerRequest.getEmail());

		if (userRepository.existsByMobileNumber(registerRequest.getMobileNumber()))
			throw new MobileNumberAlreadyRegisteredException(registerRequest.getMobileNumber());

		User user = createUserEntityFrom(registerRequest);
		userRepository.save(user);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body("Registration successful");
	}

	private User createUserEntityFrom(RegisterRequest registerRequest) {
		User user = new User();
		user.setName(registerRequest.getName());
		user.setEmail(registerRequest.getEmail());
		user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
		user.setMobileNumber(registerRequest.getMobileNumber());

		return user;
	}

	private UserDto getUserDto(Authentication authentication) {
		AuthenticatedUser authenticatedUser = (AuthenticatedUser) Objects.requireNonNull(authentication.getPrincipal());

		UserDto userDto = new UserDto();
		userDto.setUserId(authenticatedUser.userId());
		userDto.setName(authenticatedUser.name());
		userDto.setEmail(authenticatedUser.email());
		userDto.setMobileNumber(authenticatedUser.mobileNumber());

		return userDto;
	}
}
