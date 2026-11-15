package com.openclassrooms.etudiant.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitaire direct des handlers (les cas 403 et 500 ne sont déclenchés par aucune route actuelle).
 * Le test est dans le même paquet : les méthodes protected sont accessibles.
 */
public class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();
    private final WebRequest request = new ServletWebRequest(new MockHttpServletRequest("GET", "/api/students"));

    @Test
    public void test_access_denied_returns_403_with_error_details() {
        ResponseEntity<Object> response = handler.handleForbiddenException(new AccessDeniedException("Access denied"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isInstanceOf(ErrorDetails.class);
        ErrorDetails details = (ErrorDetails) response.getBody();
        assertThat(details.getMessage()).isEqualTo("Access denied");
        assertThat(details.getTimestamp()).isNotNull();
    }

    @Test
    public void test_unexpected_exception_returns_500_without_leaking_internal_message() {
        ResponseEntity<Object> response = handler.handleException(new RuntimeException("sensitive internal detail"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Internal Server error");
    }

    @Test
    public void test_bad_credentials_returns_401() {
        ResponseEntity<Object> response = handler.handleBadCredentialsException(new BadCredentialsException("Invalid credentials"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void test_illegal_argument_returns_400() {
        ResponseEntity<Object> response = handler.handleConflict(new IllegalArgumentException("bad input"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_resource_not_found_returns_404_and_email_conflict_returns_409() {
        ResponseEntity<Object> notFound = handler.handleResourceNotFoundException(new ResourceNotFoundException("missing"), request);
        ResponseEntity<Object> conflict = handler.handleEmailAlreadyUsedException(new EmailAlreadyUsedException("used"), request);

        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
