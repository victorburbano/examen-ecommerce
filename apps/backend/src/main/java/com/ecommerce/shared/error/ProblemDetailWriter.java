package com.ecommerce.shared.error;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Escribe respuestas RFC 9457 ({@code application/problem+json}) desde el filtro de seguridad,
 * donde todavía no interviene la serialización de Spring MVC.
 */
public final class ProblemDetailWriter {

    private static final String TYPE_ABOUT_BLANK = "about:blank";

    private ProblemDetailWriter() {
    }

    public static void write(
            HttpServletResponse response, HttpStatus status, String code, String detail, String instance)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String body = "{\"type\":\"" + TYPE_ABOUT_BLANK + "\""
                + ",\"title\":\"" + escape(status.getReasonPhrase()) + "\""
                + ",\"status\":" + status.value()
                + ",\"detail\":\"" + escape(detail) + "\""
                + ",\"instance\":\"" + escape(instance) + "\""
                + ",\"code\":\"" + escape(code) + "\"}";
        response.getWriter().write(body);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
