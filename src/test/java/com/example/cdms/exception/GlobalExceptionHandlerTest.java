package com.example.cdms.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.example.cdms.dto.ErrorResponse;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidation_shouldReturnBadRequestWithFieldErrors() {
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "target");
        result.addError(new FieldError("target", "sku", "sku is required"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, result);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().error());
        assertEquals(400, response.getBody().status());
        assertEquals(true, response.getBody().message().contains("sku"));
    }

    @Test
    void handleConstraintViolation_shouldReturnBadRequest() {
        jakarta.validation.ConstraintViolationException ex = mockConstraintViolationException();

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Constraint violation", response.getBody().error());
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("bad arg");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid argument", response.getBody().error());
        assertEquals("bad arg", response.getBody().message());
    }

    @Test
    void handleRuntime_shouldReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("something broke");

        ResponseEntity<ErrorResponse> response = handler.handleRuntime(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().error());
        assertEquals("something broke", response.getBody().message());
    }

    private jakarta.validation.ConstraintViolationException mockConstraintViolationException() {
        return new jakarta.validation.ConstraintViolationException(
                "violation", java.util.Collections.emptySet());
    }
}
