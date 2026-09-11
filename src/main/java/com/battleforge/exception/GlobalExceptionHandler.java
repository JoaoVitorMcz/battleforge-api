package com.battleforge.exception;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Single source of error responses, all shaped as RFC 7807 ProblemDetail. Extending
 * ResponseEntityExceptionHandler means Spring MVC's own failures (unreadable body,
 * unknown route, wrong method) come out in the same shape as ours.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROBLEM_BASE = "https://battleforge.dev/problems/";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<ValidationFailure> failures = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ValidationFailure(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validation-failed",
                "Validation failed", "One or more fields are invalid.");
        problem.setProperty("errors", failures);
        return ResponseEntity.badRequest().body(problem);
    }

    /**
     * Spring reports an unmatched route as a missing static resource. Rewritten so an API
     * client sees an unknown endpoint, not the servlet resource handler that happened to fail.
     */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "endpoint-not-found",
                "Endpoint not found", "No endpoint %s %s.".formatted(ex.getHttpMethod(), ex.getResourcePath()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationFailure> failures = ex.getConstraintViolations().stream()
                .map(violation -> new ValidationFailure(
                        violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validation-failed",
                "Validation failed", "One or more values are invalid.");
        problem.setProperty("errors", failures);
        return problem;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "resource-not-found", "Resource not found", ex.getMessage());
    }

    @ExceptionHandler(InvalidBattleActionException.class)
    public ProblemDetail handleInvalidBattleAction(InvalidBattleActionException ex) {
        return problem(HttpStatus.CONFLICT, "invalid-battle-action", "Invalid battle action", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        // The client gets an opaque message and a traceId; the details stay in the logs.
        String traceId = UUID.randomUUID().toString();
        log.error("Unhandled exception [traceId={}]", traceId, ex);

        ProblemDetail problem = problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error",
                "Internal server error", "Unexpected error. Quote the traceId when reporting it.");
        problem.setProperty("traceId", traceId);
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String slug, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEM_BASE + slug));
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
