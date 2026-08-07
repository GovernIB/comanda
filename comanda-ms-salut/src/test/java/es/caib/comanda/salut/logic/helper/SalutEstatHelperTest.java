package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.salut.logic.intf.model.SalutInformeAgrupacio;
import es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.caib.comanda.salut.logic.helper.SalutInfoHelper.MINUTS_PER_AGRUPACIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a SalutEstatHelper")
class SalutEstatHelperTest {

    @Mock
    private SalutRepository salutRepository;

    @InjectMocks
    private SalutEstatHelper salutEstatHelper;

    // ========================================================================
    // 1. TESTOS PER A computeEstatItemsPerAgrupacio
    // ========================================================================

    @Test
    @DisplayName("computeEstatItemsPerAgrupacio: amb includeAll=true calcula totes les agrupacions")
    void computeEstatItemsPerAgrupacio_quanIncludeAllEsTrue_llavorsCalculaTotes() {
        // Arrange
        Long entornAppId = 1L;
        // Mockejem la crida al repositori per a qualsevol tipus i data per evitar errors
        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(anyLong(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act
        Map<String, List<SalutInformeEstatItem>> result = salutEstatHelper.computeEstatItemsPerAgrupacio(entornAppId, true);

        // Assert
        assertThat(result).hasSize(SalutInformeAgrupacio.values().length);
        assertThat(result).containsKeys("MINUT", "MINUTS_HORA", "HORA", "DIA_SETMANA", "DIA_MES");
        verify(salutRepository, times(SalutInformeAgrupacio.values().length))
            .findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(anyLong(), any(), any());
    }

    @Test
    @DisplayName("computeEstatItemsPerAgrupacio: amb includeAll=false només calcula MINUT")
    void computeEstatItemsPerAgrupacio_quanIncludeAllEsFalse_llavorsCalculaNomesMinut() {
        // Arrange
        Long entornAppId = 1L;
        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(anyLong(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act
        Map<String, List<SalutInformeEstatItem>> result = salutEstatHelper.computeEstatItemsPerAgrupacio(entornAppId, false);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result).containsOnlyKeys("MINUT");
        verify(salutRepository, times(1))
            .findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(anyLong(), any(), any());
    }

    @Test
    @DisplayName("computeEstatItemsPerAgrupacio (sobrecàrrega): delega correctament amb includeAll=true")
    void computeEstatItemsPerAgrupacio_unParametre_llavorsDelegaAmbIncludeAllTrue() {
        // Arrange
        Long entornAppId = 1L;
        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(anyLong(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act
        salutEstatHelper.computeEstatItemsPerAgrupacio(entornAppId);

        // Assert: Verifiquem que es crida el repositori múltiples vegades (totes les agrupacions)
        verify(salutRepository, times(SalutInformeAgrupacio.values().length))
            .findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(anyLong(), any(), any());
    }

    // ========================================================================
    // 2. TESTOS PARAMETritzats PER A mapTipusAgrupacio
    // ========================================================================

    @ParameterizedTest
    @MethodSource("provideMapTipusAgrupacioCases")
    @DisplayName("mapTipusAgrupacio: retorna el TipusRegistreSalut correcte per a cada agrupació")
    void mapTipusAgrupacio_quanEsCrida_llavorsRetornaTipusCorrecte(SalutInformeAgrupacio agrupacio, TipusRegistreSalut expected) {
        // Act
        TipusRegistreSalut result = salutEstatHelper.mapTipusAgrupacio(agrupacio);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> provideMapTipusAgrupacioCases() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.MINUT, TipusRegistreSalut.MINUT),
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.MINUTS_HORA, TipusRegistreSalut.MINUTS),
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.HORA, TipusRegistreSalut.HORA),
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.DIA_SETMANA, TipusRegistreSalut.DIA),
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.DIA_MES, TipusRegistreSalut.DIA)
        );
    }

    // ========================================================================
    // 3. TESTOS PARAMETritzats PER A getTemporalAmountAgrupacio
    // ========================================================================

    @ParameterizedTest
    @MethodSource("provideTemporalAmountCases")
    @DisplayName("getTemporalAmountAgrupacio: retorna la quantitat temporal correcta")
    void getTemporalAmountAgrupacio_quanEsCrida_llavorsRetornaQuantitatCorrecta(SalutInformeAgrupacio agrupacio, Class<?> expectedType, long expectedValue) {
        // Act
        TemporalAmount result = salutEstatHelper.getTemporalAmountAgrupacio(agrupacio);

        // Assert
        assertThat(result).isInstanceOf(expectedType);
        if (result instanceof Duration) {
            assertThat(((Duration) result).toMinutes()).isEqualTo(expectedValue);
        } else if (result instanceof Period) {
            assertThat(((Period) result).getDays()).isEqualTo((int) expectedValue);
        }
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> provideTemporalAmountCases() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.DIA_MES, Period.class, 30L),
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.DIA_SETMANA, Period.class, 7L),
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.HORA, Period.class, 1L),
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.MINUTS_HORA, Duration.class, 60L), // 1 hora = 60 minuts
            org.junit.jupiter.params.provider.Arguments.arguments(SalutInformeAgrupacio.MINUT, Duration.class, 15L) // 15 minuts per defecte
        );
    }

    // ========================================================================
    // 4. TESTOS PER A getDataIniciAjustada
    // ========================================================================

    @Test
    @DisplayName("getDataIniciAjustada: ajusta correctament per a DIA_MES (zeros a hora i minut)")
    void getDataIniciAjustada_quanEsDiaMes_llavorsAjustaHoraIMinut() {
        // Arrange
        LocalDateTime ref = LocalDateTime.of(2023, 10, 25, 14, 35, 45, 123456789);

        // Act
        LocalDateTime result = salutEstatHelper.getDataIniciAjustada(SalutInformeAgrupacio.DIA_MES, ref);

        // Assert
        assertThat(result.getHour()).isEqualTo(0);
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
        assertThat(result.getNano()).isEqualTo(0);
    }

    @Test
    @DisplayName("getDataIniciAjustada: ajusta correctament per a HORA (zeros a minut)")
    void getDataIniciAjustada_quanEsHora_llavorsAjustaMinut() {
        // Arrange
        LocalDateTime ref = LocalDateTime.of(2023, 10, 25, 14, 35, 45);

        // Act
        LocalDateTime result = salutEstatHelper.getDataIniciAjustada(SalutInformeAgrupacio.HORA, ref);

        // Assert
        assertThat(result.getMinute()).isEqualTo(0);
        assertThat(result.getSecond()).isEqualTo(0);
    }

    @Test
    @DisplayName("getDataIniciAjustada: ajusta correctament per a MINUTS_HORA")
    void getDataIniciAjustada_quanEsMinutsHora_llavorsArrodoneixMinut() {
        // Arrange
        LocalDateTime ref = LocalDateTime.of(2023, 10, 25, 10, 16, 30);

        // Act
        LocalDateTime result = salutEstatHelper.getDataIniciAjustada(SalutInformeAgrupacio.MINUTS_HORA, ref);

        // Assert
        assertThat(result.getMinute()).isEqualTo(16 - 16 % MINUTS_PER_AGRUPACIO);
        assertThat(result.getSecond()).isEqualTo(0);
    }

    // ========================================================================
    // 5. TESTOS PER A generarGrupsDates
    // ========================================================================

    @Test
    @DisplayName("generarGrupsDates: genera dates correctament per a HORA")
    void generarGrupsDates_quanEsHora_llavorsIncrementaUnaHora() {
        // Arrange
        LocalDateTime inici = LocalDateTime.of(2023, 10, 25, 10, 0);

        // Act
        List<LocalDateTime> result = salutEstatHelper.generarGrupsDates(inici, SalutInformeAgrupacio.HORA);

        // Assert
        assertThat(result).hasSize(25);
        assertThat(result.get(0)).isEqualTo(inici);
        assertThat(result.get(1)).isEqualTo(inici.plusHours(1));
    }

    @Test
    @DisplayName("generarGrupsDates: genera dates correctament per a MINUT")
    void generarGrupsDates_quanEsMinut_llavorsIncrementaUnMinut() {
        // Arrange
        LocalDateTime inici = LocalDateTime.of(2023, 10, 25, 10, 0);

        // Act
        List<LocalDateTime> result = salutEstatHelper.generarGrupsDates(inici, SalutInformeAgrupacio.MINUT);

        // Assert
        assertThat(result).hasSize(16); // 15 minuts (temporalAmount) + 1 (el propi inici)
        assertThat(result.get(0)).isEqualTo(inici);
        assertThat(result.get(15)).isEqualTo(inici.plusMinutes(15));
    }

    // ========================================================================
    // 6. TESTOS PER A generateEstatList
    // ========================================================================

    @Test
    @DisplayName("generateEstatList: retorna dades de la BD ordenades quan n'hi ha")
    void generateEstatList_quanHiHaDadesBD_llavorsRetornaOrdenades() {
        // Arrange
        Long entornAppId = 1L;
        LocalDateTime dataInici = LocalDateTime.now();

        SalutEntity entity1 = new SalutEntity();
        entity1.setData(dataInici.plusMinutes(30));

        SalutEntity entity2 = new SalutEntity();
        entity2.setData(dataInici.plusMinutes(10)); // Desordenat a la BD

        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
            eq(entornAppId), eq(dataInici), eq(TipusRegistreSalut.MINUT)))
            .thenReturn(List.of(entity1, entity2));

        // Act
        List<SalutInformeEstatItem> result = salutEstatHelper.generateEstatList(dataInici, TipusRegistreSalut.MINUT, entornAppId);

        // Assert
        assertThat(result).isSortedAccordingTo(Comparator.comparing(SalutInformeEstatItem::getData));
        assertThat(result.get(0).getData()).isEqualTo(dataInici);
        assertThat(result.get(1).getData()).isEqualTo(dataInici.plusMinutes(10).withSecond(0));
        assertThat(result.get(2).getData()).isEqualTo(dataInici.plusMinutes(30).withSecond(0));
    }

    @Test
    @DisplayName("generateEstatList: omple amb valors per defecte quan la BD retorna buit")
    void generateEstatList_quanBDBuida_llavorsOmplaAmbValorsPerDefecte() {
        // Arrange
        Long entornAppId = 1L;
        LocalDateTime dataInici = LocalDateTime.now().minusMinutes(30);

        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
            anyLong(), any(), any())).thenReturn(Collections.emptyList());

        // Act
        List<SalutInformeEstatItem> result = salutEstatHelper.generateEstatList(dataInici, TipusRegistreSalut.MINUT, entornAppId);

        // Assert
        assertThat(result).isNotEmpty();
        SalutInformeEstatItem defaultItem = result.stream()
            .filter(item -> item.getUnknownPercent() == 100)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No s'ha trobat cap item amb valors per defecte"));

        assertThat(defaultItem.getUnknownCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("generateEstatList: combina dades de la BD amb dates faltants")
    void generateEstatList_quanFaltenDates_llavorsCombinaIOrdena() {
        // Arrange
        Long entornAppId = 1L;
        LocalDateTime dataInici = LocalDateTime.now().withSecond(0).withNano(0).minusMinutes(2);

        SalutEntity entity = new SalutEntity();
        entity.setData(dataInici.plusMinutes(1)); // Només tenim dades del minut 1

        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
            eq(entornAppId), eq(dataInici), eq(TipusRegistreSalut.MINUT)))
            .thenReturn(List.of(entity));

        // Act
        List<SalutInformeEstatItem> result = salutEstatHelper.generateEstatList(dataInici, TipusRegistreSalut.MINUT, entornAppId);

        // Assert
        assertThat(result).isSortedAccordingTo(Comparator.comparing(SalutInformeEstatItem::getData));
        // Ha de contenir la data de la BD i les dates generades per omplir els buits fins a 'now'
        assertThat(result.stream().map(SalutInformeEstatItem::getData).collect(Collectors.toList()))
            .contains(dataInici.plusMinutes(1));
    }
}
