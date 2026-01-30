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

    private String loadComandaAvisYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-management-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaEstadisticaYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-monitoring-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaLogYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-monitoring-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaPermisYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-management-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaSalutYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-monitoring-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaTascaYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-management-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }


    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Salut")
    void containsSalutPaths() throws IOException {
        String yml = loadComandaSalutYaml();
        assertTrue(yml.contains("name: COMANDA → APP / Salut") || yml.contains("name: COMANDA \u2192 APP / Salut"), "Falta el tag de Salut");
        assertTrue(yml.contains("/salut/v1/info"), "Falta el path /v1/salut/info");
        assertTrue(yml.contains("/salut/v1"), "Falta el path /v1/salut");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths d'Estadístiques")
    void containsEstadistiquesPaths() throws IOException {
        String yml = loadComandaEstadisticaYaml();
        assertTrue(yml.contains("name: COMANDA → APP / Estadistiques") || yml.contains("name: COMANDA \u2192 APP / Estadistiques"), "Falta el tag de Estadistiques");
        assertTrue(yml.contains("/estadistiques/v1"), "Falta /v1/estadistiques");
        assertTrue(yml.contains("/estadistiques/v1/info"), "Falta /v1/estadistiques/info");
        assertTrue(yml.contains("/estadistiques/v1/of/{data}"), "Falta /v1/estadistiques/of/{data}");
        assertTrue(yml.contains("/estadistiques/v1/from/{dataInici}/to/{dataFi}"), "Falta /v1/estadistiques/from/{dataInici}/to/{dataFi}");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Logs")
    void containsLogPaths() throws IOException {
        String yml = loadComandaLogYaml();
        assertTrue(yml.contains("name: COMANDA → APP / Logs") || yml.contains("name: COMANDA \u2192 APP / Logs"), "Falta el tag de Logs");
        assertTrue(yml.contains("/logs/v1"), "Falta el path /v1/logs");
        assertTrue(yml.contains("/logs/v1/{nomFitxer}"), "Falta el path /v1/logs/{nomFitxer}");
        assertTrue(yml.contains("/logs/v1/{nomFitxer}/linies/{nLinies}"), "Falta el path /v1/logs/{nomFitxer}/linies/{nLinies}");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Tasques")
    void containsTascaPaths() throws IOException {
        String yml = loadComandaTascaYaml();
        assertTrue(yml.contains("name: APP → COMANDA / Tasques") || yml.contains("name: COMANDA \u2192 APP / Tasques"), "Falta el tag de Tasques");
        assertTrue(yml.contains("/tasques/v1"), "Falta el path /v1/tasques");
        assertTrue(yml.contains("/tasques/v1/multiple"), "Falta el path /v1/tasques/multiple");
        assertTrue(yml.contains("/tasques/v1/{identificador}"), "Falta el path /v1/tasques/{identificador}");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Avisos")
    void containsAvisPaths() throws IOException {
        String yml = loadComandaAvisYaml();
        assertTrue(yml.contains("name: APP → COMANDA / Avisos") || yml.contains("name: COMANDA \u2192 APP / Avisos"), "Falta el tag de Avisos");
        assertTrue(yml.contains("/avisos/v1"), "Falta el path /v1/avisos");
        assertTrue(yml.contains("/avisos/v1/multiple"), "Falta el path /v1/avisos/multiple");
        assertTrue(yml.contains("/avisos/v1/{identificador}"), "Falta el path /v1/avisos/{identificador}");

    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Persmisos")
    void containsPermisPaths() throws IOException {
        String yml = loadComandaPermisYaml();
        assertTrue(yml.contains("name: APP → COMANDA / Permisos") || yml.contains("name: COMANDA \u2192 APP / Permisos"), "Falta el tag de Salut");
        assertTrue(yml.contains("/permisos/v1"), "Falta el path /v1/permisos");
        assertTrue(yml.contains("/permisos/v1/multiple"), "Falta el path /v1/permisos/multiple");
        assertTrue(yml.contains("/permisos/v1/{identificador}"), "Falta el path /v1/permisos/{identificador}");
    }

}
