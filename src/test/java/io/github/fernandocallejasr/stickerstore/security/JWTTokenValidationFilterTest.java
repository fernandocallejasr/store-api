package io.github.fernandocallejasr.stickerstore.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.fernandocallejasr.stickerstore.constants.ApplicationConstants;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class JWTTokenValidationFilterTest {

	private static final SecretKey KEY = Keys.hmacShaKeyFor(
			"test-secret-value-that-is-definitely-long-enough".getBytes(StandardCharsets.UTF_8));
	private static final SecretKey OTHER_KEY = Keys.hmacShaKeyFor(
			"a-different-secret-value-also-long-enough-to-sign".getBytes(StandardCharsets.UTF_8));

	private final JWTTokenValidationFilter filter = new JWTTokenValidationFilter(KEY);

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	private JwtBuilder validClaims(long expiresInMillis) {
		Date now = new Date();

		return Jwts.builder().issuer("Sticker Store")
				.subject("user@example.com")
				.claim("userId", 42L)
				.claim("name", "Test User")
				.claim("mobileNumber", "5551230001")
				.claim("roles", "ROLE_USER")
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expiresInMillis));
	}

	private String signedToken(long expiresInMillis) {
		return validClaims(expiresInMillis).signWith(KEY).compact();
	}

	private MockFilterChain invokeWith(String authorizationHeader) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/metrics");
		if (authorizationHeader != null)
			request.addHeader(ApplicationConstants.JWT_HEADER, authorizationHeader);

		MockFilterChain chain = new MockFilterChain();
		filter.doFilter(request, new MockHttpServletResponse(), chain);

		return chain;
	}

	@Test
	void populatesSecurityContextFromValidBearerToken() throws Exception {
		MockFilterChain chain = invokeWith("Bearer " + signedToken(60_000));

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertThat(authentication).isNotNull();
		assertThat(authentication.isAuthenticated()).isTrue();
		assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_USER");
		assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedUser.class);

		AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
		assertThat(principal.userId()).isEqualTo(42L);
		assertThat(principal.email()).isEqualTo("user@example.com");
		assertThat(principal.name()).isEqualTo("Test User");
		assertThat(principal.mobileNumber()).isEqualTo("5551230001");
		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void ignoresNonBearerAuthorizationHeaderAndContinuesChain() throws Exception {
		MockFilterChain chain = invokeWith("Basic dXNlckBleGFtcGxlLmNvbTpwYXNzd29yZA==");

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void leavesContextEmptyForMalformedTokenAndContinuesChain() throws Exception {
		MockFilterChain chain = invokeWith("Bearer not.a.jwt");

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void leavesContextEmptyForExpiredTokenAndContinuesChain() throws Exception {
		MockFilterChain chain = invokeWith("Bearer " + signedToken(-60_000));

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void leavesContextEmptyForTokenSignedWithDifferentKeyAndContinuesChain() throws Exception {
		MockFilterChain chain = invokeWith("Bearer " + validClaims(60_000).signWith(OTHER_KEY).compact());

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void leavesContextEmptyForUnsignedTokenAndContinuesChain() throws Exception {
		String unsignedToken = validClaims(60_000).compact();

		MockFilterChain chain = invokeWith("Bearer " + unsignedToken);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void leavesContextEmptyForValidlySignedTokenMissingUserIdAndContinuesChain() throws Exception {
		// The pre-fix JwtUtil emitted username/email/mobileNumber and no userId. Such a
		// token
		// still verifies against an unchanged secret, so it must be rejected cleanly
		// rather
		// than dereferencing an absent claim.
		Date now = new Date();
		String legacyFormatToken = Jwts.builder().subject("user@example.com")
				.claim("username", "Test User")
				.claim("email", "user@example.com")
				.claim("mobileNumber", "5551230001")
				.issuedAt(now)
				.expiration(new Date(now.getTime() + 60_000))
				.signWith(KEY)
				.compact();

		MockFilterChain chain = invokeWith("Bearer " + legacyFormatToken);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(chain.getRequest()).isNotNull();
	}

	@Test
	void authenticatesWithoutAuthoritiesWhenTokenCarriesNoRoles() throws Exception {
		Date now = new Date();
		String tokenWithoutRoles = Jwts.builder().subject("user@example.com")
				.claim("userId", 42L)
				.claim("name", "Test User")
				.claim("mobileNumber", "5551230001")
				.issuedAt(now)
				.expiration(new Date(now.getTime() + 60_000))
				.signWith(KEY)
				.compact();

		invokeWith("Bearer " + tokenWithoutRoles);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		assertThat(authentication).isNotNull();
		assertThat(authentication.getAuthorities()).isEmpty();
	}

	@Test
	void continuesChainWhenNoAuthorizationHeaderPresent() throws Exception {
		MockFilterChain chain = invokeWith(null);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(chain.getRequest()).isNotNull();
	}
}
