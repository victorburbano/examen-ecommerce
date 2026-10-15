package com.ecommerce.auth.api;

import jakarta.validation.constraints.NotBlank;

/** Credenciales de login. */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password) {
}
