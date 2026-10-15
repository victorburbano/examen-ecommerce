package com.ecommerce.auth.api;

import com.ecommerce.auth.application.AuthResult;
import com.ecommerce.auth.application.GetCurrentUserService;
import com.ecommerce.auth.application.LoginService;
import com.ecommerce.auth.application.LogoutService;
import com.ecommerce.auth.application.RefreshService;
import com.ecommerce.auth.application.RegisterService;
import com.ecommerce.auth.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de autenticación. Sin lógica de negocio: delega en los casos de uso. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final RefreshService refreshService;
    private final LogoutService logoutService;
    private final GetCurrentUserService getCurrentUserService;
    private final AuthMapper authMapper;
    private final RefreshCookieFactory refreshCookieFactory;

    public AuthController(
            RegisterService registerService,
            LoginService loginService,
            RefreshService refreshService,
            LogoutService logoutService,
            GetCurrentUserService getCurrentUserService,
            AuthMapper authMapper,
            RefreshCookieFactory refreshCookieFactory) {
        this.registerService = registerService;
        this.loginService = loginService;
        this.refreshService = refreshService;
        this.logoutService = logoutService;
        this.getCurrentUserService = getCurrentUserService;
        this.authMapper = authMapper;
        this.refreshCookieFactory = refreshCookieFactory;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = registerService.register(request.email(), request.password());
        return authMapper.toRegisterResponse(user);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = loginService.login(request.email(), request.password());
        return withRefreshCookie(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        return withRefreshCookie(refreshService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = RefreshCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        logoutService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.clear().toString())
                .build();
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        User user = getCurrentUserService.currentUser(Long.valueOf(jwt.getSubject()));
        return authMapper.toMeResponse(user);
    }

    private ResponseEntity<TokenResponse> withRefreshCookie(AuthResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookieFactory.create(result.refreshToken()).toString())
                .body(authMapper.toTokenResponse(result));
    }
}
