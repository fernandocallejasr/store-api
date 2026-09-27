package io.github.fernandocallejasr.stickerstore.config;

import io.github.fernandocallejasr.stickerstore.constants.ApplicationConstants;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", modifyOnCreate = false)
public class JpaAuditingConfig {

	@Bean
	public AuditorAware<String> auditorAware() {
		return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
				.filter(Authentication::isAuthenticated)
				.map(Authentication::getName)
				.filter(name -> !ApplicationConstants.ANONYMOUS_PRINCIPAL.equals(name))
				.or(() -> Optional.of(ApplicationConstants.SYSTEM_AUDITOR));
	}
}
