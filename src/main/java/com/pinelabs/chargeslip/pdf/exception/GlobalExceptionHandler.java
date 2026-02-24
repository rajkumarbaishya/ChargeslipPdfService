package com.pinelabs.chargeslip.pdf.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle invalid client input (400)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException e) {
        Throwable root = rootCause(e);

        log.warn(
            "BAD_REQUEST type={}, rootType={}, message={}",
            e.getClass().getSimpleName(),
            root.getClass().getSimpleName(),
            safeMessage(root)
        );

        return build(
                HttpStatus.BAD_REQUEST,
                root.getClass().getSimpleName(),
                safeMessage(root)
        );
    }

    /**
     * Handle missing or invalid request parameters/headers (400)
     */
    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleMissingParams(Exception e) {

        log.warn(
            "BAD_REQUEST type={}, message={}",
            e.getClass().getSimpleName(),
            e.getMessage()
        );

        return build(
                HttpStatus.BAD_REQUEST,
                e.getClass().getSimpleName(),
                e.getMessage()
        );
    }

    /**
     * Handle known server-side runtime errors (500)
     */
    @ExceptionHandler({ChargeSlipException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleInternalServerError(RuntimeException e) {

        Throwable root = rootCause(e);

        log.error(
            "INTERNAL_KNOWN type={}, rootType={}, message={}",
            e.getClass().getSimpleName(),
            root.getClass().getSimpleName(),
            safeMessage(root),
            e
        );

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                root.getClass().getSimpleName(),
                safeMessage(root)
        );
    }

    /**
     * Catch-all for any unhandled exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception e) {

        Throwable root = rootCause(e);

        log.error(
            "INTERNAL_FALLBACK type={}, rootType={}, message={}",
            e.getClass().getSimpleName(),
            root.getClass().getSimpleName(),
            safeMessage(root),
            e
        );

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                root.getClass().getSimpleName(),
                "Unexpected server error"
        );
    }

    /**
     * Extract root cause
     */
    private Throwable rootCause(Throwable t) {

        Throwable r = t;

        while (r.getCause() != null && r.getCause() != r) {
            r = r.getCause();
        }

        return r;
    }

    /**
     * Safe error message
     */
    private String safeMessage(Throwable t) {

        if (t.getMessage() == null || t.getMessage().isBlank()) {
            return t.getClass().getSimpleName();
        }

        return t.getMessage();
    }

    /**
     * Build ApiErrorResponse response
     */
    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String code,
            String message
    ) {

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now().toString(),
                status.value(),
                code,
                message
        );

        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}
