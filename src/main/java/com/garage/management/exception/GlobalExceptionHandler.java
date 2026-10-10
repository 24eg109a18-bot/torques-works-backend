package com.garage.management.exception;

import com.garage.management.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Converts every exception into the same JSON shape (see ApiError). */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest req, Map<String, String> fields) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI(), fields));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException e, HttpServletRequest r) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), r, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> duplicate(DuplicateResourceException e, HttpServletRequest r) {
        return build(HttpStatus.CONFLICT, e.getMessage(), r, null);
    }

    @ExceptionHandler({InvalidAppointmentException.class, BadRequestException.class})
    public ResponseEntity<ApiError> badRequest(RuntimeException e, HttpServletRequest r) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), r, null);
    }

    @ExceptionHandler({UnauthorizedException.class, AccessDeniedException.class})
    public ResponseEntity<ApiError> forbidden(RuntimeException e, HttpServletRequest r) {
        String msg = e instanceof UnauthorizedException ? e.getMessage() : "You do not have permission to perform this action";
        return build(HttpStatus.FORBIDDEN, msg, r, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> badCredentials(BadCredentialsException e, HttpServletRequest r) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", r, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> disabled(DisabledException e, HttpServletRequest r) {
        return build(HttpStatus.FORBIDDEN, "Your account has been blocked. Please contact the garage.", r, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        String first = fields.values().stream().findFirst().orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, first, r, fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> unreadable(Exception e, HttpServletRequest r) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request: check field formats (dates as yyyy-MM-dd, times as HH:mm) and enum values", r, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(DataIntegrityViolationException e, HttpServletRequest r) {
        return build(HttpStatus.CONFLICT, "This record is referenced by other data or duplicates an existing value", r, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> other(Exception e, HttpServletRequest r) {
        log.error("Unexpected error", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on the server", r, null);
    }
}
