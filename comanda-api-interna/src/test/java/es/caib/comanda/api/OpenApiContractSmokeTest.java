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
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-avis-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaEstadisticaYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-estadistica-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaLogYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-log-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaPermisYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-permis-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaSalutYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-salut-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }

    private String loadComandaTascaYaml() throws IOException {
        Path yaml = Path.of("src", "main", "resources", "openapi", "openapi-comanda-tasca-v1.yml");
        assertTrue(Files.exists(yaml), "No s'ha generat el fitxer OpenAPI: " + yaml.toAbsolutePath());
        return Files.readString(yaml, StandardCharsets.UTF_8);
    }


    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Salut")
    void containsSalutPaths() throws IOException {
        String yml = loadComandaSalutYaml();
        assertTrue(yml.contains("name: COMANDA → APP / Salut") || yml.contains("name: COMANDA \u2192 APP / Salut"), "Falta el tag de Salut");
        assertTrue(yml.contains("/v1/salut/info"), "Falta el path /v1/salut/info");
        assertTrue(yml.contains("/v1/salut"), "Falta el path /v1/salut");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths d'Estadístiques")
    void containsEstadistiquesPaths() throws IOException {
        String yml = loadComandaEstadisticaYaml();
        assertTrue(yml.contains("name: COMANDA → APP / Estadístiques") || yml.contains("name: COMANDA \u2192 APP / Estadístiques"), "Falta el tag de Estadistiques");
        assertTrue(yml.contains("/v1/estadistiques"), "Falta /v1/estadistiques");
        assertTrue(yml.contains("/v1/estadistiques/info"), "Falta /v1/estadistiques/info");
        assertTrue(yml.contains("/v1/estadistiques/of/{data}"), "Falta /v1/estadistiques/of/{data}");
        assertTrue(yml.contains("/v1/estadistiques/from/{dataInici}/to/{dataFi}"), "Falta /v1/estadistiques/from/{dataInici}/to/{dataFi}");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Logs")
    void containsLogPaths() throws IOException {
        String yml = loadComandaLogYaml();
        assertTrue(yml.contains("name: COMANDA → APP / Logs") || yml.contains("name: COMANDA \u2192 APP / Logs"), "Falta el tag de Logs");
        assertTrue(yml.contains("/v1/logs"), "Falta el path /v1/logs");
        assertTrue(yml.contains("/v1/logs/{nomFitxer}"), "Falta el path /v1/logs/{nomFitxer}");
        assertTrue(yml.contains("/v1/logs/{nomFitxer}/linies/{nLinies}"), "Falta el path /v1/logs/{nomFitxer}/linies/{nLinies}");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Tasques")
    void containsTascaPaths() throws IOException {
        String yml = loadComandaTascaYaml();
        assertTrue(yml.contains("name: APP → COMANDA / Tasques") || yml.contains("name: COMANDA \u2192 APP / Tasques"), "Falta el tag de Tasques");
        assertTrue(yml.contains("/v1/tasques"), "Falta el path /v1/tasques");
        assertTrue(yml.contains("/v1/tasques/multiple"), "Falta el path /v1/tasques/multiple");
        assertTrue(yml.contains("/v1/tasques/{identificador}"), "Falta el path /v1/tasques/{identificador}");
    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Avisos")
    void containsAvisPaths() throws IOException {
        String yml = loadComandaAvisYaml();
        assertTrue(yml.contains("name: APP → COMANDA / Avisos") || yml.contains("name: COMANDA \u2192 APP / Avisos"), "Falta el tag de Avisos");
        assertTrue(yml.contains("/v1/avisos"), "Falta el path /v1/avisos");
        assertTrue(yml.contains("/v1/avisos/multiple"), "Falta el path /v1/avisos/multiple");
        assertTrue(yml.contains("/v1/avisos/{identificador}"), "Falta el path /v1/avisos/{identificador}");

    }

    @Test
    @DisplayName("COMANDA → APP - YAML generat: conté paths de Persmisos")
    void containsPermisPaths() throws IOException {
        String yml = loadComandaPermisYaml();
        assertTrue(yml.contains("name: APP → COMANDA / Permisos") || yml.contains("name: COMANDA \u2192 APP / Permisos"), "Falta el tag de Salut");
        assertTrue(yml.contains("/v1/permisos"), "Falta el path /v1/permisos");
        assertTrue(yml.contains("/v1/permisos/multiple"), "Falta el path /v1/permisos/multiple");
        assertTrue(yml.contains("/v1/permisos/{identificador}"), "Falta el path /v1/permisos/{identificador}");
    }

}
