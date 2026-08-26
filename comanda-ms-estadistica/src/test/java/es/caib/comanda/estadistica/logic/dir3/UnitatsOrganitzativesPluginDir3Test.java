package es.caib.comanda.estadistica.logic.dir3;

import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitatsOrganitzativesPluginDir3Test {

    @Mock private UnitatsOrganitzativesRestClient unitatsOrganitzativesRestClient;

    @InjectMocks
    private UnitatsOrganitzativesPluginDir3 unitatsOrganitzativesPlugin;

    private UnidadRest a = new UnidadRest();
    private UnidadRest b = new UnidadRest();
    private UnidadRest c = new UnidadRest();

    @BeforeEach
    void setUp() throws SistemaExternException {
        a.setCodigo("A");

        b.setCodigo("B");
        b.setCodUnidadSuperior("A");
        b.setCodUnidadRaiz("A");

        c.setCodigo("C");
        c.setCodUnidadSuperior("B");
        c.setCodUnidadRaiz("A");

        lenient().when(unitatsOrganitzativesRestClient.isConfigured()).thenReturn(true);
        lenient().when(unitatsOrganitzativesRestClient.findUnidadArrel(eq(null), eq(null), eq(false)))
            .thenReturn(List.of(a, b, c));
    }

    @Test
    void findUnidad() throws SistemaExternException {
        // Given
        when(unitatsOrganitzativesRestClient.obtenerUnidad(eq("C"), eq(null), eq(null), eq(false)))
            .thenReturn(c);

        // When
        UnitatOrganitzativaEntity result = unitatsOrganitzativesPlugin.findUnidad("C");

        // Then
        assertEquals("C", result.getCodi());
        assertEquals("B", result.getCodiConselleria());
    }

    @Test
    void findAll() throws SistemaExternException {
        // Given

        when(unitatsOrganitzativesRestClient.findUnidad(eq("A"), eq(null), eq(null), eq(false)))
            .thenReturn(List.of(a, b, c));

        // When
        List<UnitatOrganitzativaEntity> result = unitatsOrganitzativesPlugin.findAll("A");

        // Then
        assertThat(result).hasSize(3);
    }

//    @Test
    @ParameterizedTest
    @CsvSource({"A,", "B,B", "C,B", "A04026975,A04026972"})
    void getConselleria(String input, String expected) {
        String result = unitatsOrganitzativesPlugin.getConselleria(input);
        assertEquals(expected, result);
    }

    @Test
    void isConfigured_delegaAlRestClient() {
        // Given
        when(unitatsOrganitzativesRestClient.isConfigured()).thenReturn(true);

        // When / Then
        assertThat(unitatsOrganitzativesPlugin.isConfigured()).isTrue();
        verify(unitatsOrganitzativesRestClient).isConfigured();
    }

    @Test
    void getConselleria_quanNoConfigurat_llavorsNoCridaFindUnidadArrelIRetornaValorHardcoded() throws SistemaExternException {
        // Given: substituïm el mock configurat al setUp() per un que indica que Dir3 no està configurat
        org.mockito.Mockito.reset(unitatsOrganitzativesRestClient);
        when(unitatsOrganitzativesRestClient.isConfigured()).thenReturn(false);

        // When
        String result = unitatsOrganitzativesPlugin.getConselleria("A04026975");

        // Then: el valor ve del mapa de correccions històriques (hardcoded), no de Dir3
        assertEquals("A04026972", result);
        verify(unitatsOrganitzativesRestClient, never()).findUnidadArrel(any(), any(), any());
    }

    @Test
    void getConselleria_ambArrel_quanNoConfigurat_llavorsRetornaNullSenseCridarDir3() throws SistemaExternException {
        // Given
        org.mockito.Mockito.reset(unitatsOrganitzativesRestClient);
        when(unitatsOrganitzativesRestClient.isConfigured()).thenReturn(false);
        when(unitatsOrganitzativesRestClient.getCodiArrel()).thenReturn("GOVERN_ARREL");

        // When
        String result = unitatsOrganitzativesPlugin.getConselleria("C", "ALTRA_ARREL");

        // Then
        assertEquals(null, result);
        verify(unitatsOrganitzativesRestClient, never()).findUnidad(any(), any(), any(), any());
    }
}
