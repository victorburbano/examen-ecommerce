package com.ecommerce.shared.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.auth.domain.exception.EmailAlreadyTakenException;
import com.ecommerce.auth.domain.exception.InvalidCredentialsException;
import com.ecommerce.auth.domain.exception.InvalidRefreshTokenException;
import com.ecommerce.auth.domain.exception.UserNotFoundException;
import com.ecommerce.catalog.domain.exception.InsufficientStockException;
import com.ecommerce.catalog.domain.exception.ProductNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsDomainExceptionsToStableCodes() {
        assertThat(code(handler.handleEmailAlreadyTaken(new EmailAlreadyTakenException("a@b.com"))))
                .isEqualTo("EMAIL_TAKEN");
        assertThat(handler.handleEmailAlreadyTaken(new EmailAlreadyTakenException("a@b.com")).getStatus())
                .isEqualTo(HttpStatus.CONFLICT.value());

        assertThat(code(handler.handleInvalidCredentials(new InvalidCredentialsException())))
                .isEqualTo("INVALID_CREDENTIALS");
        assertThat(code(handler.handleInvalidRefreshToken(new InvalidRefreshTokenException())))
                .isEqualTo("INVALID_REFRESH_TOKEN");
        assertThat(code(handler.handleUserNotFound(new UserNotFoundException(1L)))).isEqualTo("USER_NOT_FOUND");
        assertThat(handler.handleInvalidCredentials(new InvalidCredentialsException()).getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void mapsCatalogExceptionsToStableCodes() {
        ProblemDetail notFound = handler.handleProductNotFound(new ProductNotFoundException(999L));
        assertThat(notFound.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(code(notFound)).isEqualTo("PRODUCT_NOT_FOUND");

        ProblemDetail stock = handler.handleInsufficientStock(new InsufficientStockException(8L, 2, 0));
        assertThat(stock.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(code(stock)).isEqualTo("INSUFFICIENT_STOCK");
        assertThat(stock.getProperties())
                .containsEntry("productId", 8L)
                .containsEntry("requested", 2)
                .containsEntry("available", 0);
    }

    @Test
    void mapsUnexpectedExceptionsTo500WithoutLeakingDetails() {
        ProblemDetail problem = handler.handleUnexpected(new IllegalStateException("internal detail"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(code(problem)).isEqualTo("INTERNAL_ERROR");
        assertThat(problem.getDetail()).isEqualTo("Unexpected error");
    }

    @Test
    void collectsValidationErrorsIntoTheErrorsProperty() throws Exception {
        ValidationTarget target = new ValidationTarget();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "registerRequest");
        bindingResult.rejectValue("password", "Size", "size must be between 8 and 100");
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter(), bindingResult);

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(
                exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, new ServletWebRequest(new MockHttpServletRequest()));

        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(body).isNotNull();
        assertThat(code(body)).isEqualTo("VALIDATION_ERROR");
        assertThat((List<?>) body.getProperties().get("errors")).hasSize(1);
    }

    @Test
    void mapsAccessDeniedTo403() {
        ProblemDetail problem = handler.handleAccessDenied(new AccessDeniedException("forbidden"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(code(problem)).isEqualTo("FORBIDDEN");
    }

    private static String code(ProblemDetail problem) {
        return (String) problem.getProperties().get("code");
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        return new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class), 0);
    }

    @SuppressWarnings("unused")
    private void dummy(String value) {
        // solo para construir un MethodParameter válido
    }

    /** Bean con setter para que Bean Validation pueda registrar el error de campo. */
    static class ValidationTarget {

        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
