package io.github.fernandocallejasr.stickerstore.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ProductNotFoundException.class)
	public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
		log.info("Product not found: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ProblemDetail handleValidation(HandlerMethodValidationException ex) {
		log.info("Request validation failed: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
		log.info("No resource found: {}", ex.getMessage());
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleUnexpected(Exception ex) {
		log.error("Unexpected error handling request", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
	}
}
