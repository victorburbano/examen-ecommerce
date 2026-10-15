package com.ecommerce.auth.api;

import java.util.List;

/** Datos del usuario autenticado (GET /me). */
public record MeResponse(Long userId, String email, List<String> roles) {
}
