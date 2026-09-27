package io.github.fernandocallejasr.stickerstore.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PublicPathConfig {

	@Bean
	public List<String> publicPaths() {
		return List.of(
				"/api/v1/products/**",
				"/api/v1/auth/**",
				// only registered in dev; every other profile inherits
				// management.endpoints.access.default=none.
				"/actuator/health",
				"/actuator/health/**",
				"/actuator/info",
				"/actuator/prometheus");
	}
}
