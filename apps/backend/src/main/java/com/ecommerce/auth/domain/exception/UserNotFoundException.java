package com.ecommerce.auth.domain.exception;

/** El usuario del access token ya no existe (se traduce a 401 USER_NOT_FOUND). */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("User not found: " + userId);
    }
}
