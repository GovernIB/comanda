package es.caib.comanda.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests que validen que el YAML OpenAPI generat existeix i conté
 * els paths i tags principals per a Salut i Estadístiques.
 */
class OpenApiContractSmokeTest {

    private String loadComandaAppYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-app-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadAppComandaYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-app-comanda-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté els tags principals")
    void containsMainTags() throws IOException {
        String yml = loadComandaAppYaml();
        assertTrue(yml.contains("name: COMANDA → APP / Salut") || yml.contains("name: COMANDA \u2192 APP / Salut"),
                "Falta el tag de Salut");
        assertTrue(yml.contains("name: COMANDA → APP / Estadístiques") || yml.contains("name: COMANDA \u2192 APP / Estadístiques"),
                "Falta el tag d'Estadístiques");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Salut")
    void containsSalutPaths() throws IOException {
        String yml = loadComandaAppYaml();
        assertTrue(yml.contains("/api/v1/appInfo"), "Falta el path /api/v1/appInfo");
        assertTrue(yml.contains("/api/v1/salut"), "Falta el path /api/v1/salut");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths d'Estadístiques")
    void containsEstadistiquesPaths() throws IOException {
        String yml = loadComandaAppYaml();
        assertTrue(yml.contains("/api/v1/estadistiques/info"), "Falta /api/v1/estadistiques/info");
        assertTrue(yml.contains("/api/v1/estadistiques"), "Falta /api/v1/estadistiques");
        assertTrue(yml.contains("/api/v1/estadistiques/of/{data}"), "Falta /api/v1/estadistiques/of/{data}");
        assertTrue(yml.contains("/api/v1/estadistiques/from/{dataInici}/to/{dataFi}"), "Falta /api/v1/estadistiques/from/{dataInici}/to/{dataFi}");
    }

    @Test
    @DisplayName("APP → COMANDA - YAML generat: conté els tags principals")
    void appComandaContainsMainTags() throws IOException {
        String yml = loadAppComandaYaml();
        assertTrue(yml.contains("name: APP → COMANDA / JMS") || yml.contains("name: APP \u2192 COMANDA / JMS"),
                "Falta el tag de JMS");
    }

    @Test
    @DisplayName("APP → COMANDA - YAML generat: conté paths de Jms")
    void appComandaContainsSalutPaths() throws IOException {
        String yml = loadAppComandaYaml();
        assertTrue(yml.contains("/api/v1/jms"), "Falta el path /");
        assertTrue(yml.contains("/api/v1/jms/avisos"), "Falta el path /api/v1/jms/avisos");
        assertTrue(yml.contains("/api/v1/jms/tasques"), "Falta el path /api/v1/jms/tasques");
        assertTrue(yml.contains("/api/v1/jms/permisos"), "Falta el path /api/v1/jms/permisos");
    }
}
