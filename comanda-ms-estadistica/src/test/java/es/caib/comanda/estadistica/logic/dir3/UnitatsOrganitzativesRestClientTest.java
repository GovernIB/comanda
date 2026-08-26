package es.caib.comanda.estadistica.logic.dir3;

import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests per a la guarda de configuració de {@link UnitatsOrganitzativesRestClient}: si el plugin Dir3 no està
 * correctament configurat (URL buida o no vàlida), no s'ha de continuar fent peticions a Dir3.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a UnitatsOrganitzativesRestClient")
class UnitatsOrganitzativesRestClientTest {

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UnitatsOrganitzativesRestClient restClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.invokeMethod(restClient, "init");
    }

    // ========================================================================
    // 1. TESTOS PER A isConfigured
    // ========================================================================

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "no-es-una-url", "/nomes/un/path"})
    @DisplayName("isConfigured: retorna false quan la URL és buida, en blanc o no absoluta")
    void isConfigured_quanUrlBuidaOInvalida_llavorsRetornaFalse(String baseUrl) {
        // Arrange
        ReflectionTestUtils.setField(restClient, "baseUrl", baseUrl);

        // Act & Assert
        assertThat(restClient.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("isConfigured: retorna true quan la URL és absoluta i vàlida")
    void isConfigured_quanUrlValida_llavorsRetornaTrue() {
        // Arrange
        ReflectionTestUtils.setField(restClient, "baseUrl", "https://dir3.example.caib.es");

        // Act & Assert
        assertThat(restClient.isConfigured()).isTrue();
    }

    // ========================================================================
    // 2. TESTOS PER A LA GUARDA A obtenerUnidad / findUnidad
    // ========================================================================

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "no-es-una-url"})
    @DisplayName("obtenerUnidad: llança SistemaExternException sense fer cap petició quan no està configurat")
    void obtenerUnidad_quanNoConfigurat_llavorsLlancaExcepcioSenseCridarRestTemplate(String baseUrl) {
        // Arrange
        ReflectionTestUtils.setField(restClient, "baseUrl", baseUrl);

        // Act & Assert
        assertThatThrownBy(() -> restClient.obtenerUnidad("codi", null, null, false))
            .isInstanceOf(SistemaExternException.class);
        verifyNoInteractions(restTemplate);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "no-es-una-url"})
    @DisplayName("findUnidad: llança SistemaExternException sense fer cap petició quan no està configurat")
    void findUnidad_quanNoConfigurat_llavorsLlancaExcepcioSenseCridarRestTemplate(String baseUrl) {
        // Arrange
        ReflectionTestUtils.setField(restClient, "baseUrl", baseUrl);

        // Act & Assert
        assertThatThrownBy(() -> restClient.findUnidad("codi", null, null, false))
            .isInstanceOf(SistemaExternException.class);
        verifyNoInteractions(restTemplate);
    }

    // ========================================================================
    // 3. TESTOS PER A getCodiArrel
    // ========================================================================

    @Test
    @DisplayName("getCodiArrel: retorna el fallback per defecte quan no hi ha configuració")
    void getCodiArrel_quanNoConfigurat_llavorsRetornaFallback() {
        // Arrange
        ReflectionTestUtils.setField(restClient, "codiArrel", "");

        // Act & Assert
        assertThat(restClient.getCodiArrel()).isEqualTo(UnitatsOrganitzativesRestClient.CODI_ARREL_PER_DEFECTE);
    }
}
