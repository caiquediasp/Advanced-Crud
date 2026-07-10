package com.caique.AdvancedCrud.shared.exceptions;

import com.caique.AdvancedCrud.shared.errorLog.CriticalErrorEvent;
import com.caique.AdvancedCrud.shared.errorLog.ErrorLogPublisher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorLogPublisher errorLogPublisher;

    public GlobalExceptionHandler(ErrorLogPublisher errorLogPublisher) {
        this.errorLogPublisher = errorLogPublisher;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        pd.setTitle("Resource Not Found");
        return pd;
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ProblemDetail handleRoleNotFoundException(RoleNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        pd.setTitle("Resource Not Found");
        return pd;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage()
        );
        pd.setTitle("Email Already In Use");
        return pd;
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPasswordException(InvalidPasswordException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage()
        );
        pd.setTitle("Invalid Password");
        return pd;
    }

    @ExceptionHandler(SelfModificationException.class)
    public ProblemDetail handleSelfModificationException(SelfModificationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage()
        );
        pd.setTitle("Self Modification not allowed");
        return pd;
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ProblemDetail handleAddressNotFoundException(AddressNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        pd.setTitle("Resource Not Found");
        return pd;
    }

    @ExceptionHandler(CepNotFoundException.class)
    public ProblemDetail handleCepNotFoundException(CepNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("CEP not found");
        return problem;
    }

    @ExceptionHandler(CepServiceUnavailableException.class)
    public ProblemDetail handleCepUnavailableException(CepServiceUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problem.setTitle("Service unavailable");
        return problem;
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ProblemDetail handleTooManyLoginAttemptsException(TooManyLoginAttemptsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS, ex.getMessage()
        );
        pd.setTitle("Too Many Requests");
        return pd;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Invalid email or password");
        problem.setTitle("Authentication failed");
        return problem;
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ProblemDetail handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "You don't have permission to access this resource");
        problem.setTitle("Access denied");
        return problem;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Authentication failed");
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "The request conflicts with the current state of the data"
        );
        pd.setTitle("Data Conflict");
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation Failed"
        );
        pd.setTitle("Validation Error");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        pd.setProperty("errors", errors);

        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Malformed request body"
        );
        pd.setTitle("Malformed Request");
        return pd;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage()
        );
        pd.setTitle("Method Not Allowed");
        return pd;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'"
        );
        pd.setTitle("Invalid Parameter");
        return pd;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "Resource not found"
        );
        pd.setTitle("Resource Not Found");
        return pd;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Authentication Failed"
        );
        pd.setTitle("Authentication Failed");
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);

        publishErrorEvent(ex, request);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An Unexpected Error Occurred"
        );
        pd.setTitle("Internal Server Error");
        return pd;
    }

    private void publishErrorEvent(Exception ex, HttpServletRequest request) {
        try {
            CriticalErrorEvent event = new CriticalErrorEvent(
                    UUID.randomUUID(),
                    ex.getClass().getName(),
                    ex.getMessage(),
                    request.getRequestURI(),
                    resumeStackTrace(ex),
                    Instant.now()
            );

            errorLogPublisher.publish(event);
        } catch (Exception publishException) {
            log.error("Failed to publish critical error event", publishException);
        }
    }

    private String resumeStackTrace(Exception ex) {
        StackTraceElement[] elements = ex.getStackTrace();
        StringBuilder sb = new StringBuilder();
        sb.append(ex.toString()).append("\n");
        int limit = Math.min(elements.length, 10);
        for (int i = 0; i < limit; i++) {
            sb.append("\tat ").append(elements[i]).append("\n");
        }
        return sb.toString();
    }

}
