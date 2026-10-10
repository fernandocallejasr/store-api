package io.github.fernandocallejasr.stickerstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

import io.github.fernandocallejasr.stickerstore.entity.User;
import io.github.fernandocallejasr.stickerstore.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("dev")
public class AuthFlowIntegrationTest {

	private static final String REGISTER_PATH = "/api/v1/auth/register";
	private static final String LOGIN_PATH = "/api/v1/auth/login";
	private static final String PROFILE_PATH = "/api/v1/profile";
	private static final String REGISTERED_PASSWORD = "password123";

	@Autowired
	TestRestTemplate restTemplate;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	UserRepository userRepository;
	@Autowired
	PasswordEncoder passwordEncoder;

	@Test
	void registerCreatesAccount() {
		ResponseEntity<String> response = postJson(REGISTER_PATH,
				registrationJson("New User", "create@example.com", "5550000001", "password123"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).contains("Registration successful");
	}

	@Test
	void registerStoresBcryptHashNotRawPassword() {
		register("hashed@example.com", "5550000010");

		User user = userRepository.findByEmail("hashed@example.com").orElseThrow();

		assertThat(user.getPasswordHash()).isNotEqualTo(REGISTERED_PASSWORD);
		assertThat(user.getPasswordHash()).startsWith("$2");
		assertThat(passwordEncoder.matches(REGISTERED_PASSWORD, user.getPasswordHash())).isTrue();
	}

	@Test
	void registerIgnoresUnknownFieldsAndCannotSetInternalOnes() {
		String email = "massassign@example.com";
		String json = """
				{"name":"Registered User","email":"%s","mobileNumber":"5550000015","password":"%s",
				"userId":999,"passwordHash":"not-a-hash","createdBy":"attacker","role":"ADMIN"}
				""".formatted(email, REGISTERED_PASSWORD);

		ResponseEntity<String> response = postJson(REGISTER_PATH, json);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		User user = userRepository.findByEmail(email).orElseThrow();
		assertThat(user.getUserId()).isNotEqualTo(999L);
		assertThat(user.getPasswordHash()).isNotEqualTo("not-a-hash");
		assertThat(user.getPasswordHash()).startsWith("$2");
		assertThat(passwordEncoder.matches(REGISTERED_PASSWORD, user.getPasswordHash())).isTrue();
		assertThat(user.getCreatedBy()).isEqualTo("SYSTEM");
	}

	@Test
	void registerRejectsDuplicateEmailWithConflict() {
		String duplicateEmail = "duplicate@example.com";

		register(duplicateEmail, "5550000002");

		ResponseEntity<String> response = postJson(REGISTER_PATH,
				registrationJson("Other User", duplicateEmail, "5550000003", "password123"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).contains("email");
	}

	@Test
	void registerNormalizesEmailCaseAndSurroundingWhitespace() {
		register("  Mixed.Case@Example.COM  ", "5550000011");

		ResponseEntity<String> response = postJson(LOGIN_PATH,
				loginJson("mixed.case@example.com", REGISTERED_PASSWORD));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(body).extractingByKey("user", MAP).containsEntry("email", "mixed.case@example.com");
	}

	@Test
	void registerRejectsEmailDifferingOnlyInCaseWithConflict() {
		register("dup-case@example.com", "5550000012");

		ResponseEntity<String> response = postJson(REGISTER_PATH,
				registrationJson("Other User", "DUP-Case@Example.com", "5550000013", "password123"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).contains("email");
	}

	@Test
	void registerRejectsDuplicateMobileNumberWithConflict() {
		String duplicateMobileNumber = "5550000004";

		register("mobileowner@example.com", duplicateMobileNumber);

		ResponseEntity<String> response = postJson(REGISTER_PATH,
				registrationJson("Other User", "othermobile@example.com", duplicateMobileNumber, "password123"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).contains("mobile number");
	}

	@Test
	void registerRejectsInvalidPayload() {
		ResponseEntity<String> response = postJson(REGISTER_PATH,
				registrationJson("No", "not-an-email", "12", "short"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void registerRejectsTooLongNameWithSizeLimitsInMessage() {
		String thirtyOneCharacterName = "N".repeat(31);

		ResponseEntity<String> response = postJson(REGISTER_PATH,
				registrationJson(thirtyOneCharacterName, "longname@example.com", "5550000009", "password123"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		Map<String, Object> problem = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(problem).extractingByKey("errors", MAP)
				.extractingByKey("name", LIST)
				.containsExactly("The length of the name should be between 2 and 30 characters");
	}

	@Test
	void registerRejectsMarkupInNameWithAllowListMessage() {
		ResponseEntity<String> response = postJson(REGISTER_PATH,
				registrationJson("<svg onload=alert(1)>", "svgname@example.com", "5550000017", "password123"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		Map<String, Object> problem = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(problem).extractingByKey("errors", MAP)
				.extractingByKey("name", LIST)
				.containsExactly("Name can only contain letters, spaces, apostrophes, hyphens and periods");
	}

	@Test
	void registerRejectsOverlongEmailWithSizeMessage() {
		String hundredAndOneCharacterEmail = "a".repeat(64) + "@" + "b".repeat(24) + ".example.com";
		assertThat(hundredAndOneCharacterEmail).hasSize(101);

		ResponseEntity<String> response = postJson(REGISTER_PATH,
				registrationJson("Registered User", hundredAndOneCharacterEmail, "5550000019", "password123"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		Map<String, Object> problem = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(problem).extractingByKey("errors", MAP)
				.extractingByKey("email", LIST)
				.containsExactly("Email must be at most 100 characters");
	}

	@Test
	void registerStripsSurroundingWhitespaceFromName() {
		String email = "validname@example.com";

		ResponseEntity<String> registration = postJson(REGISTER_PATH,
				registrationJson("  Valid Name  ", email, "5550000018", REGISTERED_PASSWORD));

		assertThat(registration.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(userRepository.findByEmail(email).orElseThrow().getName()).isEqualTo("Valid Name");

		ResponseEntity<String> login = postJson(LOGIN_PATH, loginJson(email, REGISTERED_PASSWORD));

		assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);

		Map<String, Object> body = objectMapper.readValue(login.getBody(), new TypeReference<>() {
		});
		assertThat(body).extractingByKey("user", MAP).containsEntry("name", "Valid Name");
	}

	@Test
	void registerStripsSurroundingWhitespaceFromMobileNumber() {
		register("trimmedmobile@example.com", " 5551234567 ");

		assertThat(userRepository.findByEmail("trimmedmobile@example.com").orElseThrow().getMobileNumber())
				.isEqualTo("5551234567");
	}

	@Test
	void loginReturnsTokenAndPopulatedUser() {
		register("login@example.com", "5550000005");

		ResponseEntity<String> response = postJson(LOGIN_PATH, loginJson("login@example.com", REGISTERED_PASSWORD));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(body).extractingByKey("jwtToken", STRING).isNotBlank();
		assertThat(body).extractingByKey("user", MAP)
				.containsEntry("email", "login@example.com")
				.containsEntry("name", "Registered User")
				.containsEntry("mobileNumber", "5550000005")
				.extractingByKey("userId")
				.isNotNull();
	}

	@Test
	void loginAcceptsEmailInDifferentCase() {
		register("case-login@example.com", "5550000014");

		ResponseEntity<String> response = postJson(LOGIN_PATH,
				loginJson("CASE-LOGIN@EXAMPLE.COM", REGISTERED_PASSWORD));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void loginIgnoresUnknownFields() {
		register("rememberme@example.com", "5550000016");

		ResponseEntity<String> response = postJson(LOGIN_PATH, """
				{"username":"rememberme@example.com","password":"%s","rememberMe":true}
				""".formatted(REGISTERED_PASSWORD));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void loginRejectsWrongPasswordWithUnauthorized() {
		register("wrongpass@example.com", "5550000006");

		ResponseEntity<String> response = postJson(LOGIN_PATH, loginJson("wrongpass@example.com", "notmypassword"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).contains("Incorrect username or password");
	}

	@Test
	void loginRejectsUnknownEmailWithTheSameResponseAsAWrongPassword() {
		register("enumeration@example.com", "5550000008");

		ResponseEntity<String> wrongPassword = postJson(LOGIN_PATH,
				loginJson("enumeration@example.com", "notmypassword"));
		ResponseEntity<String> unknownEmail = postJson(LOGIN_PATH, loginJson("nobody@example.com", "password123"));

		assertThat(wrongPassword.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(unknownEmail.getStatusCode()).isEqualTo(wrongPassword.getStatusCode());
		assertThat(unknownEmail.getBody()).contains("Incorrect username or password");
		assertThat(unknownEmail.getBody()).isEqualTo(wrongPassword.getBody());
	}

	@Test
	void loginRejectsPasswordLongerThanBcryptLimitLikeAWrongPassword() {
		register("longpassword@example.com", "5550000020");

		ResponseEntity<String> wrongPassword = postJson(LOGIN_PATH,
				loginJson("longpassword@example.com", "notmypassword"));
		ResponseEntity<String> overlongPassword = postJson(LOGIN_PATH,
				loginJson("longpassword@example.com", "a".repeat(100)));

		assertThat(overlongPassword.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(overlongPassword.getStatusCode()).isEqualTo(wrongPassword.getStatusCode());
		assertThat(overlongPassword.getBody()).isEqualTo(wrongPassword.getBody());
	}

	@Test
	void loginRejectsMissingPasswordWithBadRequest() {
		ResponseEntity<String> response = postJson(LOGIN_PATH, "{\"username\":\"someone@example.com\"}");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void loginRejectsMissingUsernameWithBadRequest() {
		ResponseEntity<String> response = postJson(LOGIN_PATH, "{\"password\":\"password123\"}");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		Map<String, Object> problem = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(problem).extractingByKey("errors", MAP)
				.extractingByKey("username", LIST)
				.containsExactly("Username is required");
	}

	@Test
	void protectedEndpointRejectsRequestWithoutToken() {
		ResponseEntity<String> response = getWithAuthorization(PROFILE_PATH, null);

		assertAuthenticationRequiredProblem(response);
	}

	@Test
	void protectedEndpointAcceptsValidBearerToken() {
		String token = tokenFor("bearer@example.com", "5550000007");

		ResponseEntity<String> response = getWithAuthorization(PROFILE_PATH, "Bearer " + token);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(body).containsEntry("email", "bearer@example.com")
				.containsEntry("name", "Registered User")
				.containsEntry("mobileNumber", "5550000007");

		Long registeredUserId = userRepository.findByEmail("bearer@example.com").orElseThrow().getUserId();
		assertThat(((Number) body.get("userId")).longValue()).isEqualTo(registeredUserId);
	}

	@Test
	void profileReflectsNameChangedAfterLogin() {
		String email = "profile-rename@example.com";
		String token = tokenFor(email, "5550000021");

		User user = userRepository.findByEmail(email).orElseThrow();
		user.setName("Renamed User");
		userRepository.save(user);

		ResponseEntity<String> response = getWithAuthorization(PROFILE_PATH, "Bearer " + token);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		Map<String, Object> body = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(body).containsEntry("name", "Renamed User");
	}

	@Test
	void profileReturnsNotFoundWhenUserDeletedAfterLogin() {
		String email = "profile-deleted@example.com";
		String token = tokenFor(email, "5550000022");

		userRepository.delete(userRepository.findByEmail(email).orElseThrow());

		ResponseEntity<String> response = getWithAuthorization(PROFILE_PATH, "Bearer " + token);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getHeaders().getContentType())
				.matches(MediaType.APPLICATION_PROBLEM_JSON::equalsTypeAndSubtype);

		Map<String, Object> problem = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(problem).containsEntry("status", 404).containsEntry("title", "Not Found");
	}

	@Test
	void protectedEndpointRejectsMalformedToken() {
		ResponseEntity<String> response = getWithAuthorization(PROFILE_PATH, "Bearer not.a.jwt");

		assertAuthenticationRequiredProblem(response);
	}

	@Test
	void protectedEndpointIgnoresNonBearerAuthorizationHeader() {
		ResponseEntity<String> response = getWithAuthorization(PROFILE_PATH,
				"Basic Ym9ndXM6Y3JlZGVudGlhbHM=");

		assertAuthenticationRequiredProblem(response);
	}

	private ResponseEntity<String> postJson(String path, String json) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return restTemplate.postForEntity(path, new HttpEntity<>(json, headers), String.class);
	}

	private ResponseEntity<String> getWithAuthorization(String path, String authorizationHeader) {
		HttpHeaders headers = new HttpHeaders();
		if (authorizationHeader != null)
			headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);

		return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
	}

	private String registrationJson(String name, String email, String mobileNumber, String password) {
		return """
				{"name":"%s","email":"%s","mobileNumber":"%s","password":"%s"}
				""".formatted(name, email, mobileNumber, password);
	}

	private String loginJson(String email, String password) {
		return """
				{"username":"%s","password":"%s"}
				""".formatted(email, password);
	}

	private void register(String email, String mobileNumber) {
		ResponseEntity<String> response = postJson(REGISTER_PATH,
				registrationJson("Registered User", email, mobileNumber, REGISTERED_PASSWORD));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	private String tokenFor(String email, String mobileNumber) {
		register(email, mobileNumber);
		ResponseEntity<String> login = postJson(LOGIN_PATH, loginJson(email, REGISTERED_PASSWORD));
		String body = login.getBody();

		Assertions.assertNotNull(body);
		return body.replaceAll("(?s).*\"jwtToken\"\\s*:\\s*\"([^\"]+)\".*", "$1");
	}

	private void assertAuthenticationRequiredProblem(ResponseEntity<String> response) {
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		assertThat(response.getHeaders().getContentType())
				.isEqualTo(new MediaType(MediaType.APPLICATION_PROBLEM_JSON, StandardCharsets.UTF_8));

		Map<String, Object> problem = objectMapper.readValue(response.getBody(), new TypeReference<>() {
		});
		assertThat(problem).containsEntry("status", 401)
				.containsEntry("title", "Unauthorized")
				.containsEntry("detail", "Authentication required");
	}
}
