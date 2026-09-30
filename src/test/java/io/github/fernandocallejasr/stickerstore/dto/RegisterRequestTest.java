package io.github.fernandocallejasr.stickerstore.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class RegisterRequestTest {

	private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

	private final Validator validator = VALIDATOR_FACTORY.getValidator();

	@AfterAll
	static void closeValidatorFactory() {
		VALIDATOR_FACTORY.close();
	}

	private RegisterRequest withName(String name) {
		RegisterRequest request = new RegisterRequest();
		request.setName(name);

		return request;
	}

	@ParameterizedTest
	@ValueSource(strings = {"José García", "O'Brien", "O" + (char) 0x2019 + "Brien", "Anne-Marie", "J. R. R. Tolkien",
			"Zoë Saldaña", "Nguyễn Văn An", "李小龍", "Ana", "Jose" + (char) 0x0301})
	void acceptsValidNames(String name) {
		assertThat(validator.validateProperty(withName(name), "name")).isEmpty();
	}

	static Stream<String> invalidNames() {
		return Stream.of("<svg onload=alert(1)>", "Robert'); DROP TABLE users;--", "John2", "John\nSmith", "John😀",
				"'John", "-Ann", "A", "N".repeat(31));
	}

	@ParameterizedTest
	@MethodSource("invalidNames")
	void rejectsInvalidNames(String name) {
		assertThat(validator.validateProperty(withName(name), "name")).isNotEmpty();
	}

	@Test
	void setNameStripsAndStoresTheNfcForm() {
		RegisterRequest request = withName("  Jose" + (char) 0x0301 + "  ");

		assertThat(request.getName()).isEqualTo("Jos" + (char) 0x00E9).hasSize(4);
	}
}
