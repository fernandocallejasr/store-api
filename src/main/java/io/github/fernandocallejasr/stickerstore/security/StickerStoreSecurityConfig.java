package io.github.fernandocallejasr.stickerstore.security;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class StickerStoreSecurityConfig {

	private static final int MINIMUM_SECRET_LENGTH_BYTES = 32;

	private final List<String> publicPaths;

	@Value("${app.cors.allowed-origins}")
	private List<String> allowedOrigins;

	public StickerStoreSecurityConfig(List<String> publicPaths) {
		this.publicPaths = publicPaths;
	}

	@Bean
	SecretKey jwtSigningKey(@Value("${jwt.secret}") String secret) {
		if (secret == null || secret.isBlank())
			throw new IllegalStateException(
					"jwt.secret is not set. Provide the JWT_SECRET environment variable before starting the application.");

		byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < MINIMUM_SECRET_LENGTH_BYTES)
			throw new IllegalStateException("jwt.secret must be at least " + MINIMUM_SECRET_LENGTH_BYTES
					+ " bytes to sign tokens, but was " + secretBytes.length + ".");

		return Keys.hmacShaKeyFor(secretBytes);
	}

	@Bean
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
			SecretKey jwtSigningKey,
			AuthenticationEntryPoint authenticationEntryPoint) {
		http.cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()));
		http.authorizeHttpRequests(requests -> {
			publicPaths.forEach(path -> requests.requestMatchers(path).permitAll());
			requests.anyRequest().authenticated();
		});
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint));
		// Safe to disable: authentication is bearer-token only, so no ambient cookie
		// can be forged.
		http.csrf(AbstractHttpConfigurer::disable);
		http.addFilterBefore(new JWTTokenValidationFilter(jwtSigningKey), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
		return new ProviderManager(authenticationProvider);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "X-Request-Id"));
		config.setExposedHeaders(List.of("X-Request-Id"));
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}
}
