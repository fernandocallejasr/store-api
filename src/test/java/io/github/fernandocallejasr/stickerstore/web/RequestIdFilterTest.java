package io.github.fernandocallejasr.stickerstore.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

	private static final String HEADER = "X-Request-Id";
	private static final String MDC_KEY = "requestId";

	@Test
	void generatesRequestId_setsResponseHeader_andClearsMdcAfter() throws Exception {
		RequestIdFilter filter = new RequestIdFilter();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> mdcDuringChain = new AtomicReference<>();
		FilterChain chain = (req, res) -> mdcDuringChain.set(MDC.get(MDC_KEY));

		filter.doFilter(request, response, chain);

		assertThat(mdcDuringChain.get()).isNotBlank();
		assertThat(response.getHeader(HEADER)).isEqualTo(mdcDuringChain.get());
		assertThat(MDC.get(MDC_KEY)).isNull();
	}

	@Test
	void propagatesIncomingRequestId() throws Exception {
		RequestIdFilter filter = new RequestIdFilter();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HEADER, "abc-123");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> mdcDuringChain = new AtomicReference<>();
		FilterChain chain = (req, res) -> mdcDuringChain.set(MDC.get(MDC_KEY));

		filter.doFilter(request, response, chain);

		assertThat(mdcDuringChain.get()).isEqualTo("abc-123");
		assertThat(response.getHeader(HEADER)).isEqualTo("abc-123");
	}

	@Test
	void sanitizesUnsafeIncomingRequestId() throws Exception {
		RequestIdFilter filter = new RequestIdFilter();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HEADER, "abc\r\nINFO evil-injected-line");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> mdcDuringChain = new AtomicReference<>();
		FilterChain chain = (req, res) -> mdcDuringChain.set(MDC.get(MDC_KEY));

		filter.doFilter(request, response, chain);

		assertThat(mdcDuringChain.get()).doesNotContain("\n").doesNotContain("\r").doesNotContain(" ");
		assertThat(mdcDuringChain.get()).matches("[A-Za-z0-9_-]+");
	}
}
