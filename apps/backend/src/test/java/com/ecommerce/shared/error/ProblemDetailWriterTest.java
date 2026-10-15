package com.ecommerce.shared.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

class ProblemDetailWriterTest {

    @Test
    void writesAnRfc9457ProblemJsonBody() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProblemDetailWriter.write(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required",
                "/api/auth/me");

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/problem+json");
        assertThat(response.getContentAsString())
                .contains("\"status\":401")
                .contains("\"code\":\"UNAUTHORIZED\"")
                .contains("\"instance\":\"/api/auth/me\"")
                .contains("\"title\":\"Unauthorized\"");
    }

    @Test
    void escapesQuotesAndToleratesNullValues() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        ProblemDetailWriter.write(response, HttpStatus.BAD_REQUEST, "CODE", "value with \"quotes\"", null);

        assertThat(response.getContentAsString())
                .contains("value with \\\"quotes\\\"")
                .contains("\"instance\":\"\"");
    }
}
