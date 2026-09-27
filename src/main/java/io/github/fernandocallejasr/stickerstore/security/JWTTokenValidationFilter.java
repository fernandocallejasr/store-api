package io.github.fernandocallejasr.stickerstore.security;

import io.github.fernandocallejasr.stickerstore.constants.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JWTTokenValidationFilter extends OncePerRequestFilter {

	private final SecretKey secretKey;

	public JWTTokenValidationFilter(SecretKey secretKey) {
		this.secretKey = secretKey;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader(ApplicationConstants.JWT_HEADER);

		if (authHeader != null && authHeader.startsWith(ApplicationConstants.BEARER_PREFIX)) {
			String jwt = authHeader.substring(ApplicationConstants.BEARER_PREFIX.length());

			try {
				Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload();
				SecurityContextHolder.getContext().setAuthentication(authenticationFrom(claims));
			} catch (JwtException | IllegalArgumentException ex) {
				SecurityContextHolder.clearContext();
				logger.debug("Rejected JWT: " + ex.getMessage());
			}
		}

		filterChain.doFilter(request, response);
	}

	private Authentication authenticationFrom(Claims claims) {
		Number userId = claims.get(ApplicationConstants.CLAIM_USER_ID, Number.class);
		if (userId == null)
			throw new MalformedJwtException("Token is missing the userId claim");

		AuthenticatedUser principal = new AuthenticatedUser(
				userId.longValue(),
				claims.get(ApplicationConstants.CLAIM_NAME, String.class),
				claims.getSubject(),
				claims.get(ApplicationConstants.CLAIM_MOBILE_NUMBER, String.class));

		return UsernamePasswordAuthenticationToken.authenticated(principal, null, authoritiesFrom(claims));
	}

	private List<GrantedAuthority> authoritiesFrom(Claims claims) {
		String roles = claims.get(ApplicationConstants.CLAIM_ROLES, String.class);

		return roles == null || roles.isBlank()
				? List.of()
				: AuthorityUtils.commaSeparatedStringToAuthorityList(roles);
	}
}
