package io.github.fernandocallejasr.stickerstore.util;

import io.github.fernandocallejasr.stickerstore.constants.ApplicationConstants;
import io.github.fernandocallejasr.stickerstore.security.AuthenticatedUser;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

	private static final long TOKEN_VALIDITY_MILLIS = 60 * 60 * 1000L;

	private final SecretKey secretKey;

	public JwtUtil(SecretKey secretKey) {
		this.secretKey = secretKey;
	}

	public String generateJwtToken(Authentication authentication) {
		AuthenticatedUser authenticatedUser = (AuthenticatedUser) Objects.requireNonNull(
				authentication.getPrincipal(), "Authentication has no principal; cannot issue a token.");
		Date issuedAt = new Date();

		return Jwts.builder().issuer("Sticker Store")
				.subject(authenticatedUser.email())
				.claim(ApplicationConstants.CLAIM_USER_ID, authenticatedUser.userId())
				.claim(ApplicationConstants.CLAIM_NAME, authenticatedUser.name())
				.claim(ApplicationConstants.CLAIM_MOBILE_NUMBER, authenticatedUser.mobileNumber())
				.claim(ApplicationConstants.CLAIM_ROLES, authoritiesOf(authentication))
				.issuedAt(issuedAt)
				.expiration(new Date(issuedAt.getTime() + TOKEN_VALIDITY_MILLIS))
				.signWith(secretKey)
				.compact();
	}

	private String authoritiesOf(Authentication authentication) {
		return authentication.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));
	}
}
