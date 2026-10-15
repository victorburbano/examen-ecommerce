package com.ecommerce.auth.api;

import com.ecommerce.auth.application.AuthResult;
import com.ecommerce.auth.domain.User;
import java.util.List;
import org.springframework.stereotype.Component;

/** Traduce dominio/aplicación a los DTOs de la API (sin lógica de negocio). */
@Component
public class AuthMapper {

    public RegisterResponse toRegisterResponse(User user) {
        return new RegisterResponse(user.id(), user.email(), user.role());
    }

    public TokenResponse toTokenResponse(AuthResult result) {
        return TokenResponse.bearer(result.accessToken());
    }

    public MeResponse toMeResponse(User user) {
        return new MeResponse(user.id(), user.email(), List.of(user.role().name()));
    }
}
