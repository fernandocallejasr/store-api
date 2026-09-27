package io.github.fernandocallejasr.stickerstore.constants;

public class ApplicationConstants {

	private ApplicationConstants() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	public static final String SYSTEM_AUDITOR = "SYSTEM";

	public static final String ANONYMOUS_PRINCIPAL = "anonymousUser";

	public static final String JWT_HEADER = "Authorization";

	public static final String BEARER_PREFIX = "Bearer ";

	public static final String DEFAULT_ROLE = "ROLE_USER";

	public static final String CLAIM_USER_ID = "userId";

	public static final String CLAIM_NAME = "name";

	public static final String CLAIM_MOBILE_NUMBER = "mobileNumber";

	public static final String CLAIM_ROLES = "roles";
}
