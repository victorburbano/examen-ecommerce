package com.ecommerce.auth.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecommerce.auth.domain.Role;
import com.ecommerce.auth.domain.User;
import com.ecommerce.auth.domain.port.AccessTokenIssuer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Comprueba la autorización por rol con un endpoint sonda protegido con {@code @PreAuthorize}:
 * un token USER recibe 403 (ProblemDetail) y un token ADMIN accede. Verifica de paso que el
 * claim {@code roles} se traduce a {@code ROLE_*} y que el decoder HS256 real lo valida.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(RoleBasedAccessIntegrationTest.AdminProbeController.class)
class RoleBasedAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccessTokenIssuer accessTokenIssuer;

    @Test
    void aUserTokenCannotReachAnAdminEndpoint() throws Exception {
        String token = tokenFor(Role.USER);

        mockMvc.perform(get("/api/test/admin-probe").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void anAdminTokenReachesTheAdminEndpoint() throws Exception {
        String token = tokenFor(Role.ADMIN);

        mockMvc.perform(get("/api/test/admin-probe").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String tokenFor(Role role) {
        return accessTokenIssuer.issue(new User(1L, "probe@example.com", "hash", role)).value();
    }

    @RestController
    @RequestMapping("/api/test")
    static class AdminProbeController {

        @GetMapping("/admin-probe")
        @PreAuthorize("hasRole('ADMIN')")
        String adminProbe() {
            return "ok";
        }
    }
}
