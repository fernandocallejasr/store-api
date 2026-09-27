package io.github.fernandocallejasr.stickerstore.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fernandocallejasr.stickerstore.constants.ApplicationConstants;
import io.github.fernandocallejasr.stickerstore.security.AuthenticatedUser;
import io.github.fernandocallejasr.stickerstore.security.JWTTokenValidationFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtUtilTest {

	private static final SecretKey KEY = Keys.hmacShaKeyFor(
			"test-secret-value-that-is-definitely-long-enough".getBytes(StandardCharsets.UTF_8));

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void generatedTokenRoundTripsThroughValidationFilter() throws Exception {
		AuthenticatedUser user = new AuthenticatedUser(42L, "Test User", "user@example.com", "5551230001");
		Authentication original = UsernamePasswordAuthenticationToken.authenticated(user, null,
				AuthorityUtils.createAuthorityList(ApplicationConstants.DEFAULT_ROLE));

		String token = new JwtUtil(KEY).generateJwtToken(original);

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/metrics");
		request.addHeader(ApplicationConstants.JWT_HEADER, ApplicationConstants.BEARER_PREFIX + token);
		new JWTTokenValidationFilter(KEY).doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertThat(authentication).isNotNull();
		assertThat(authentication.getPrincipal()).isEqualTo(user);
		assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly(ApplicationConstants.DEFAULT_ROLE);

		Claims claims = Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
		assertThat(Duration.between(claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant()))
				.isEqualTo(Duration.ofHours(1));
	}
}
