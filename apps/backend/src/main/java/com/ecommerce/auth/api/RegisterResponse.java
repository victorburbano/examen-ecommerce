package com.ecommerce.auth.api;

import com.ecommerce.auth.domain.Role;

/** Usuario recién registrado. */
public record RegisterResponse(Long id, String email, Role role) {
}
