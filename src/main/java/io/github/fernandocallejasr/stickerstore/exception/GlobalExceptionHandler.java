package io.github.fernandocallejasr.stickerstore.exception;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ProductNotFoundException.class)
	public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
		log.info("Product not found: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.info("Request validation failed: {}", ex.getMessage());
		ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
		return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.info("Request body validation failed: {} field(s) rejected", ex.getBindingResult().getErrorCount());

		ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
		body.setProperty("errors", fieldMessages(ex));

		return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	public ProblemDetail handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
		log.info("Registration rejected: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"An account with that email already exists.");
	}

	@ExceptionHandler(MobileNumberAlreadyRegisteredException.class)
	public ProblemDetail handleMobileNumberAlreadyRegistered(MobileNumberAlreadyRegisteredException ex) {
		log.info("Registration rejected: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"An account with that mobile number already exists.");
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex) {
		log.error("Unexpected error handling request", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
	}

	@ExceptionHandler(UsernameNotFoundException.class)
	public ProblemDetail handleUsernameNotFound(Exception ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ProblemDetail handleBadCredentials(Exception ex) {
		log.warn("Bad Credentials");
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
	}

	private Map<String, List<String>> fieldMessages(MethodArgumentNotValidException ex) {
		return ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(Collectors.groupingBy(FieldError::getField,
						LinkedHashMap::new,
						Collectors.mapping(this::messageOf, Collectors.toList())));
	}

	private String messageOf(FieldError fieldError) {
		return Objects.requireNonNullElse(fieldError.getDefaultMessage(), "Invalid value");
	}
}
