package io.github.fernandocallejasr.stickerstore.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EmailsTest {

	private final Locale originalLocale = Locale.getDefault();

	@AfterEach
	void restoreDefaultLocale() {
		Locale.setDefault(originalLocale);
	}

	@Test
	void lowercasesAndTrimsSurroundingWhitespace() {
		assertThat(Emails.normalize("  Mixed.Case@Example.COM \t")).isEqualTo("mixed.case@example.com");
	}

	@Test
	void returnsNullForNull() {
		assertThat(Emails.normalize(null)).isNull();
	}

	@Test
	void lowercasesIndependentlyOfTheDefaultLocale() {
		Locale.setDefault(Locale.of("tr"));
		assertThat("ADMIN".toLowerCase()).isEqualTo("adm" + (char) 0x0131 + "n");

		assertThat(Emails.normalize("ADMIN@EXAMPLE.COM")).isEqualTo("admin@example.com");
	}
}
