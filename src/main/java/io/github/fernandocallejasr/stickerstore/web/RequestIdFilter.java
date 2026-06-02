package io.github.fernandocallejasr.stickerstore.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

	private static final String REQUEST_ID_HEADER = "X-Request-Id";
	private static final String MDC_KEY = "requestId";
	private static final int MAX_LENGTH = 64;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestId = sanitize(request.getHeader(REQUEST_ID_HEADER));
		if (requestId.isBlank()) {
			requestId = UUID.randomUUID().toString();
		}
		MDC.put(MDC_KEY, requestId);
		response.setHeader(REQUEST_ID_HEADER, requestId);
		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(MDC_KEY);
		}
	}

	private static String sanitize(String value) {
		if (value == null) {
			return "";
		}
		String trimmed = value.length() > MAX_LENGTH ? value.substring(0, MAX_LENGTH) : value;
		return trimmed.replaceAll("[^A-Za-z0-9_-]", "");
	}
}
