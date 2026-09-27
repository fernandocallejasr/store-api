package io.github.fernandocallejasr.stickerstore.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class StickerStoreSecurityConfigTest {

	private final StickerStoreSecurityConfig config = new StickerStoreSecurityConfig(List.of());

	@Test
	void rejectsMissingSecret() {
		assertThatThrownBy(() -> config.jwtSigningKey(null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("JWT_SECRET");
	}

	@Test
	void rejectsBlankSecret() {
		assertThatThrownBy(() -> config.jwtSigningKey("   "))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("JWT_SECRET");
	}

	@Test
	void rejectsSecretShorterThanTheSigningMinimum() {
		assertThatThrownBy(() -> config.jwtSigningKey("too-short"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("at least 32 bytes");
	}

	@Test
	void buildsSigningKeyFromSecretOfSufficientLength() {
		assertThat(config.jwtSigningKey("a-secret-value-that-is-long-enough-to-sign")).isNotNull();
	}
}
