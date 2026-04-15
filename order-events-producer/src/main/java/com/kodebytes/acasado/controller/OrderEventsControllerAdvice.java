package com.kodebytes.acasado.controller;

import com.kodebytes.acasado.exception.OrderEventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class OrderEventsControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsControllerAdvice.class);

    /**
     * Handles Bean-validation failures (@Valid / @Validated)
     * constraint violations raised by {@code @Valid} on the request body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .filter(msg -> msg != null && !msg.isBlank())
                .sorted()
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errors));
    }

    /**
     * Handles malformed JSON bodies or unknown enum values
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        String detail = ex.getMostSpecificCause().getMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(List.of("Invalid request body: " + detail)));
    }

    /**
     * Handles Kafka publish failures when the Kafka producer cannot publish the event.
     */
    @ExceptionHandler(OrderEventException.class)
    public ResponseEntity<ErrorResponse> handlePublishException(OrderEventException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(List.of(ex.getMessage())));
    }

    /**
     * Catches-all fallback/exception not handled by a more specific handler above.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(List.of("An unexpected error occurred. Please try again later.")));
    }

    /**
     * Uniform error response returned for every error case.
     */
    public record ErrorResponse(List<String> errors) {}
}


