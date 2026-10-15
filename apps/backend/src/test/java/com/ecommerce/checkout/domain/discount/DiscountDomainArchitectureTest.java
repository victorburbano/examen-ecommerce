package com.ecommerce.checkout.domain.discount;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Test de arquitectura: el motor de descuentos es **dominio puro**. Ningún archivo del paquete
 * puede importar Spring, JPA, validación, Jackson ni JDBC.
 */
class DiscountDomainArchitectureTest {

    private static final Path DISCOUNT_PACKAGE =
            Path.of("src", "main", "java", "com", "ecommerce", "checkout", "domain", "discount");

    private static final List<String> FORBIDDEN_IMPORTS = List.of(
            "import org.springframework",
            "import jakarta.persistence",
            "import jakarta.validation",
            "import com.fasterxml.jackson",
            "import tools.jackson",
            "import java.sql");

    @Test
    void theDiscountPackageDoesNotDependOnFrameworks() {
        assertThat(DISCOUNT_PACKAGE).as("ruta del paquete de dominio").exists();

        List<Path> sources = sources();

        assertThat(sources).as("archivos de dominio encontrados").isNotEmpty();
        for (Path source : sources) {
            String content = read(source);
            for (String forbidden : FORBIDDEN_IMPORTS) {
                assertThat(content)
                        .as("%s no debe contener '%s'", source, forbidden)
                        .doesNotContain(forbidden);
            }
        }
    }

    private static List<Path> sources() {
        try (Stream<Path> paths = Files.walk(DISCOUNT_PACKAGE)) {
            return paths.filter(path -> path.toString().endsWith(".java")).toList();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String read(Path source) {
        try {
            return Files.readString(source);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
