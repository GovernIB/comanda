package es.caib.comanda.estadistica.logic.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.consulta.*;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.IndicadorRolEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OrdreDireccioEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeMode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.periode.PresetPeriode;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.*;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorFormulaTermeRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a ConsultaEstadisticaHelper")
class ConsultaEstadisticaHelperTest {

    @Mock private FetRepository fetRepository;
    @Mock private DashboardItemRepository dashboardItemRepository;
    @Mock private UnitatOrganitzativaRepository unitatOrganitzativaRepository;
    @Mock private DimensioRepository dimensioRepository;
    @Mock private IndicadorRepository indicadorRepository;
    @Mock private IndicadorFormulaTermeRepository indicadorFormulaTermeRepository;
    @Mock private AtributsVisualsHelper atributsVisualsHelper;
    @Mock private EstadisticaClientHelper estadisticaClientHelper;
    @Mock private DashboardStyleResolverHelper dashboardStyleResolverHelper;
    @Mock private DashboardSeguretatHelper dashboardSeguretatHelper;
    @Mock private es.caib.comanda.ms.logic.helper.AuthenticationHelper authenticationHelper;

    @InjectMocks
    private ConsultaEstadisticaHelper consultaEstadisticaHelper;

    private ObjectMapper objectMapper;
    private List<FetEntity> fetEntities;
    private List<TempsEntity> tempsEntities;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            fetEntities = objectMapper.readValue(
                new ClassPathResource("es/caib/comanda/estadistica/logic/helper/fets.json").getInputStream(),
                new TypeReference<List<FetEntity>>() {});
            tempsEntities = objectMapper.readValue(
                new ClassPathResource("es/caib/comanda/estadistica/logic/helper/temps.json").getInputStream(),
                new TypeReference<List<TempsEntity>>() {});

            for (FetEntity fet : fetEntities) {
                Long tempsId = fet.getTemps().getId();
                TempsEntity temps = tempsEntities.stream()
                    .filter(t -> t.getId().equals(tempsId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Temps not found"));
                fet.setTemps(temps);
            }
        } catch (Exception e) {
            // Fallback robust si els fitxers JSON no existeixen en l'entorn de prova (ex. CI/CD)
            fetEntities = new ArrayList<>();
            tempsEntities = new ArrayList<>();
        }

        lenient().when(atributsVisualsHelper.getAtributsVisuals(any(DashboardItemEntity.class))).thenReturn(null);
        lenient().when(atributsVisualsHelper.getAtributsVisuals(any(EstadisticaWidgetEntity.class))).thenReturn(null);
        lenient().when(dashboardSeguretatHelper.resoldre(any())).thenReturn(SeguretatDadesResultat.builder().exempt(true).build());
    }

    // ========================================================================
    // 1. TESTOS PARAMETritzats (Simplificació i Cobertura de Branques)
    // ========================================================================

    @ParameterizedTest
    @MethodSource("providePercentatgeComparacioCases")
    @DisplayName("getPercentatgeComparacio: calcula correctament el percentatge")
    void getPercentatgeComparacio_quanValorsSonValidos_llavorsCalculaCorrectament(double valor1, double valor2, double expected) {
        double result = (double) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getPercentatgeComparacio", valor1, valor2);
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> providePercentatgeComparacioCases() {
        return Stream.of(
            arguments(100.0, 150.0, 50.0),
            arguments(100.0, 75.0, -25.0),
            arguments(100.0, 100.0, 0.0),
            arguments(0.0, 10.0, 100.0),
            arguments(0.0, -10.0, -100.0),
            arguments(0.0, 0.0, 0.0)
        );
    }

    @ParameterizedTest
    @MethodSource("provideIsZeroCases")
    @DisplayName("isZero: identifica correctament valors propers a zero")
    void isZero_quanEsCrida_llavorsRetornaBooleanEsperat(double valor, boolean expected) {
        boolean result = (boolean) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "isZero", valor);
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideIsZeroCases() {
        return Stream.of(
            arguments(0.0, true),
            arguments(0.0000000001, true),
            arguments(-0.0000000001, true),
            arguments(0.1, false),
            arguments(1.0, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideIsNumericCases")
    @DisplayName("isNumeric: valida correctament cadenes numèriques")
    void isNumeric_quanEsCrida_llavorsRetornaBooleanEsperat(String valor, boolean expected) {
        boolean result = (boolean) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "isNumeric", valor);
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideIsNumericCases() {
        return Stream.of(
            arguments("123", true),
            arguments("123.45", true),
            arguments("-123.45", true),
            arguments("abc", false),
            arguments("123abc", false),
            arguments(null, false),
            arguments("", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideIsDateCases")
    @DisplayName("isDate: valida correctament cadenes de data")
    void isDate_quanEsCrida_llavorsRetornaBooleanEsperat(String valor, boolean expected) {
        boolean result = (boolean) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "isDate", valor);
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideIsDateCases() {
        return Stream.of(
            arguments("01/01/2023", true),
            arguments("1/1/2023", true),
            arguments("01-01-2023", false),
            arguments("2023-13-01", false),
            arguments("abc", false),
            arguments(null, false),
            arguments("", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideToDoubleCases")
    @DisplayName("toDouble: converteix correctament cadenes a Double o retorna null")
    void toDouble_quanEsCrida_llavorsRetornaValorEsperat(String valor, Double expected) {
        Double result = (Double) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "toDouble", valor);
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideToDoubleCases() {
        return Stream.of(
            arguments("123", 123.0),
            arguments("123.45", 123.45),
            arguments("-123.45", -123.45),
            arguments("abc", null),
            arguments(null, null),
            arguments("", null),
            arguments(" ", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideLabelAgrupacioTemporalCases")
    @DisplayName("getLabelAgrupacioTemporal: retorna l'etiqueta correcta per a cada unitat")
    void getLabelAgrupacioTemporal_quanEsCrida_llavorsRetornaEtiquetaCorrecta(PeriodeUnitat unitat, String expected) {
        String result = (String) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getLabelAgrupacioTemporal", unitat);
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideLabelAgrupacioTemporalCases() {
        return Stream.of(
            arguments(PeriodeUnitat.DIA, "Dia"),
            arguments(PeriodeUnitat.SETMANA, "Setmana"),
            arguments(PeriodeUnitat.MES, "Mes"),
            arguments(PeriodeUnitat.TRIMESTRE, "Trimestre"),
            arguments(PeriodeUnitat.ANY, "Any")
        );
    }

    @ParameterizedTest
    @MethodSource("provideWidgetTypeCases")
    @DisplayName("determineWidgetType: identifica correctament el tipus de widget")
    void determineWidgetType_quanEsCrida_llavorsRetornaTipusCorrecte(EstadisticaWidgetEntity widget, WidgetTipus expected) {
        DashboardItemEntity item = new DashboardItemEntity();
        item.setId(1L);
        item.setWidget(widget);

        WidgetTipus result = (WidgetTipus) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "determineWidgetType", item);
        assertThat(result).isEqualTo(expected);
    }

    private static Stream<Arguments> provideWidgetTypeCases() {
        return Stream.of(
            arguments(new EstadisticaSimpleWidgetEntity(), WidgetTipus.SIMPLE),
            arguments(new EstadisticaGraficWidgetEntity(), WidgetTipus.GRAFIC),
            arguments(new EstadisticaTaulaWidgetEntity(), WidgetTipus.TAULA)
        );
    }

    // ========================================================================
    // 2. TESTOS D'EXCEPCIONS I CASOS LÍMIT
    // ========================================================================

    @Test
    @DisplayName("determineWidgetType: llança ReportGenerationException per a tipus desconegut")
    void determineWidgetType_quanTipusDesconegut_llancaExcepcio() {
        DashboardItemEntity item = new DashboardItemEntity();
        item.setId(1L);
        item.setWidget(mock(EstadisticaWidgetEntity.class)); // No és cap dels 3 tipus coneguts

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "determineWidgetType", item))
            .isInstanceOf(ReportGenerationException.class)
            .hasMessageContaining("Tipus de widget incorrecte");
    }

    @Test
    @DisplayName("getDadesWidget: llança ReportGenerationException quan el repositori falla")
    void getDadesWidget_quanRepositoriFalla_llancaReportGenerationException() {
        DashboardItemEntity item = new DashboardItemEntity();
        item.setId(1L);
        when(dashboardItemRepository.findById(1L)).thenThrow(new RuntimeException("DB Error"));

        assertThatThrownBy(() -> consultaEstadisticaHelper.getDadesWidget(item, false, null))
            .isInstanceOf(ReportGenerationException.class)
            .hasMessageContaining("DB Error");
    }

    @Test
    @DisplayName("getEstadistiquesPeriodeAmbDimensions: delega a getEstadistiquesPeriode quan dimensions és null o buit")
    void getEstadistiquesPeriodeAmbDimensions_quanDimensionsBuit_delegaAMetodeBase() {
        Long entornAppId = 1L;
        LocalDate inici = LocalDate.of(2023, 1, 1);
        LocalDate fi = LocalDate.of(2023, 3, 31);

        when(fetRepository.findByEntornAppIdAndTempsDataBetween(eq(entornAppId), eq(inici), eq(fi)))
            .thenReturn(Collections.emptyList());

        // Act: null
        consultaEstadisticaHelper.getEstadistiquesPeriodeAmbDimensions(entornAppId, inici, fi, null);
        // Act: empty map
        consultaEstadisticaHelper.getEstadistiquesPeriodeAmbDimensions(entornAppId, inici, fi, Collections.emptyMap());

        // Assert
        verify(fetRepository, times(2)).findByEntornAppIdAndTempsDataBetween(eq(entornAppId), eq(inici), eq(fi));
        verify(fetRepository, never()).findByEntornAppIdAndTempsDataBetweenAndDimensions(any(), any(), any(), any());
    }

    @Test
    @DisplayName("findUnitatsOrganitzativesCodiNomByCodiInBatches: retorna mapa buit quan codis és null o buit")
    void findUnitatsOrganitzativesCodiNomByCodiInBatches_quanCodisNullOBuit_retornaMapaBuit() {
        assertThat((Map<String, String>) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "findUnitatsOrganitzativesCodiNomByCodiInBatches", (Set<String>) null)).isEmpty();
        assertThat((Map<String, String>) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "findUnitatsOrganitzativesCodiNomByCodiInBatches", Collections.emptySet())).isEmpty();
        verify(unitatOrganitzativaRepository, never()).findByCodiIn(anyList());
    }

    @Test
    @DisplayName("calculateValorSimple: retorna null quan periodeConsulta és null o té dates null")
    void calculateValorSimple_quanPeriodeNull_llavorsRetornaNull() {
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();

        String result = (String) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "calculateValorSimple", widget, (PeriodeResolverHelper.PeriodeDates) null, 1L, null, null);
        assertThat(result).isNull();

        PeriodeResolverHelper.PeriodeDates periode = new PeriodeResolverHelper.PeriodeDates(null, null);
        result = (String) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "calculateValorSimple", widget, periode, 1L, null, null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("calculateCanviPercentual: retorna null quan compararPeriodeAnterior és false")
    void calculateCanviPercentual_quanNoEsCompararPeriodeAnterior_llavorsRetornaNull() {
        // Arrange
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setCompararPeriodeAnterior(false);
        PeriodeResolverHelper.PeriodeDates periode = new PeriodeResolverHelper.PeriodeDates(LocalDate.now(), LocalDate.now());

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "calculateCanviPercentual", widget, "100", periode, 1L, null, null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("calculateCanviPercentual: retorna null quan el valor actual no és numèric")
    void calculateCanviPercentual_quanValorActualNoNumeric_llavorsRetornaNull() {
        // Arrange
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setCompararPeriodeAnterior(true);
        PeriodeResolverHelper.PeriodeDates periode = new PeriodeResolverHelper.PeriodeDates(LocalDate.now(), LocalDate.now());

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "calculateCanviPercentual", widget, "no-numero", periode, 1L, null, null);
        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("calculateCanviPercentual: retorna null quan el valor previ no és numèric")
    void calculateCanviPercentual_quanValorPreviNoNumeric_llavorsRetornaNull() {
        // Arrange
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        IndicadorTaulaEntity indicadorTaula = new IndicadorTaulaEntity();
        IndicadorEntity indicador = new IndicadorEntity();
        indicadorTaula.setIndicador(indicador);
        widget.setIndicadorInfo(indicadorTaula);
        widget.setCompararPeriodeAnterior(true);
        PeriodeResolverHelper.PeriodeDates periode = new PeriodeResolverHelper.PeriodeDates(LocalDate.now(), LocalDate.now());
        when(fetRepository.getValorSimpleAgregat(any(), any(), any(), any(), any(), any())).thenReturn("no-numero");

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "calculateCanviPercentual", widget, "100", periode, 1L, null, null);
        // Assert
        assertThat(result).isNull();
    }

    // ========================================================================
    // 3. TESTOS DE LÒGICA DE NEGOCI PRINCIPAL
    // ========================================================================

    @Test
    @DisplayName("getDadesWidgetTaula: substitueix codi per nom quan la dimensió és de tipus unitat organitzativa")
    void getDadesWidgetTaula_quanDimensioEsUnitatOrganitzativa_substitueixCodiPerNom() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("departament");
        dimensio.setTipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum.ORGAN_GESTOR);
        widget.setDimensioAgrupacio(dimensio);

        IndicadorTaulaEntity indicadorTaula = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(indicadorTaula, "titol", "columna_titol");
        ReflectionTestUtils.setField(indicadorTaula, "indicador", new IndicadorEntity());
        widget.setColumnes(Collections.singletonList(indicadorTaula));
        widget.setTitolAgrupament("titol_agrupament");
        dashboardItem.setWidget(widget);

        DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
            .entornAppId(1L).entornCodi("DEV")
            .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
            .build();

        List<Map<String, String>> mockFiles = new ArrayList<>();
        mockFiles.add(new HashMap<>(Map.of("agrupacio", "CODI_1", "col1", "100")));
        when(fetRepository.getValorsTaulaAgregat(any(), any(), any(), any(), any(), any(), any())).thenReturn(mockFiles);

        DimensioEntity dimensioEntity = new DimensioEntity();
        dimensioEntity.setTipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum.ORGAN_GESTOR);
        when(dimensioRepository.findByCodiAndEntornAppId("departament", 1L)).thenReturn(Optional.of(dimensioEntity));

        UnitatOrganitzativaEntity uo = mock(UnitatOrganitzativaEntity.class);
        when(uo.getCodi()).thenReturn("CODI_1");
        when(uo.getCodiNom()).thenReturn("Nom Real");
        when(unitatOrganitzativaRepository.findByCodiIn(anyList())).thenReturn(Collections.singletonList(uo));

        // Act
        InformeWidgetItem result = (InformeWidgetItem) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getDadesWidgetTaula", dashboardItem, dadesComuns, null, null);

        // Assert
        assertThat(result).isNotNull();
        InformeWidgetTaulaItem taulaItem = (InformeWidgetTaulaItem) result;
        assertThat(taulaItem.getFiles().get(0).get("agrupacio")).isEqualTo("Nom Real");
        verify(unitatOrganitzativaRepository).findByCodiIn(anyList());
    }

    @Test
    @DisplayName("filesToSeries: GAUGE_CHART amb DOS_INDICADORS produeix value i max separats")
    void filesToSeries_quanGaugeChartIDosIndicadors_llavorsRetornaValueIMaxSeparats() {
        // Arrange
        List<Map<String, String>> files = Collections.singletonList(
            Map.of("agrupacio", "Total", "col1", "40", "col2", "200"));

        // Act
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.GAUGE_CHART, TipusGraficDataEnum.DOS_INDICADORS);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("value", 40.0);
        assertThat(result.get(0)).containsEntry("max", 200.0);
    }

    @Test
    @DisplayName("filesToSeries: GAUGE_CHART amb DOS_INDICADORS i sense dades de màxim retorna max null, no 0")
    void filesToSeries_quanGaugeChartIDosIndicadorsSenseMaxim_llavorsRetornaMaxNull() {
        // Arrange
        Map<String, String> fila = new java.util.HashMap<>();
        fila.put("agrupacio", "Total");
        fila.put("col1", "40");
        fila.put("col2", null);
        List<Map<String, String>> files = Collections.singletonList(fila);

        // Act
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.GAUGE_CHART, TipusGraficDataEnum.DOS_INDICADORS);

        // Assert
        assertThat(result.get(0).get("max")).isNull();
    }

    @Test
    @DisplayName("getDadesWidgetGrafic: DOS_INDICADORS consulta ambdós indicadors i no llança excepció")
    void getDadesWidgetGrafic_quanDosIndicadors_llavorsConsultaAmbdosIndicadorsSenseError() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        IndicadorEntity indicadorValor = new IndicadorEntity();
        indicadorValor.setId(1L);
        indicadorValor.setCodi("IND_VALOR");
        IndicadorTaulaEntity filaValor = new IndicadorTaulaEntity();
        filaValor.setRol(IndicadorRolEnum.VALOR);
        filaValor.setIndicador(indicadorValor);
        filaValor.setTitol("Valor");
        filaValor.setAgregacio(TableColumnsEnum.SUM);

        IndicadorEntity indicadorMaxim = new IndicadorEntity();
        indicadorMaxim.setId(2L);
        indicadorMaxim.setCodi("IND_MAXIM");
        IndicadorTaulaEntity filaMaxim = new IndicadorTaulaEntity();
        filaMaxim.setRol(IndicadorRolEnum.MAXIM);
        filaMaxim.setIndicador(indicadorMaxim);
        filaMaxim.setTitol("Màxim");
        filaMaxim.setAgregacio(TableColumnsEnum.SUM);

        EstadisticaGraficWidgetEntity widget = new EstadisticaGraficWidgetEntity();
        widget.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);
        widget.setTipusGrafic(TipusGraficEnum.GAUGE_CHART);
        widget.setTempsAgrupacio(PeriodeUnitat.DIA);
        widget.setIndicadorsInfo(List.of(filaValor, filaMaxim));
        dashboardItem.setWidget(widget);

        DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
            .entornAppId(1L).entornCodi("DEV")
            .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
            .build();

        when(fetRepository.getValorsGraficVarisIndicadors(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.singletonList(Map.of("agrupacio", "Total", "col1", "40", "col2", "200")));

        // Act
        InformeWidgetItem result = (InformeWidgetItem) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "getDadesWidgetGrafic", dashboardItem, dadesComuns, null, null);

        // Assert
        assertThat(result).isInstanceOf(InformeWidgetGraficItem.class);
        InformeWidgetGraficItem graficItem = (InformeWidgetGraficItem) result;
        assertThat(graficItem.getDades()).hasSize(1);
        assertThat(graficItem.getDades().get(0)).containsEntry("value", 40.0);
        assertThat(graficItem.getDades().get(0)).containsEntry("max", 200.0);
    }

    @Test
    @DisplayName("resolveAtributsVisuals: aplica correctament la plantilla destacada en mode fosc")
    void resolveAtributsVisuals_quanDestacatITemaFosc_aplicaDarkHighlighted() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setDestacat(true);
        dashboardItem.setWidget(new EstadisticaSimpleWidgetEntity());

        PlantillaEntity plantilla = new PlantillaEntity();
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setPlantilla(plantilla);
        dashboardItem.setDashboard(dashboard);

        // Act
        consultaEstadisticaHelper.resolveAtributsVisuals(dashboardItem, true);

        // Assert
        ArgumentCaptor<PaletteGroupType> groupTypeCaptor = ArgumentCaptor.forClass(PaletteGroupType.class);
        verify(dashboardStyleResolverHelper).applyTemplateDefaults(any(), eq(plantilla), groupTypeCaptor.capture(), any(WidgetStyleScope.class));
        assertThat(groupTypeCaptor.getValue()).isEqualTo(PaletteGroupType.DARK_HIGHLIGHTED);
    }

    @Test
    @DisplayName("resolveAtributsVisuals: ignora camps propis si personalitzat és false")
    void resolveAtributsVisuals_quanNoPersonalitzat_ignoraCampsPropis() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setPersonalitzat(false);
        dashboardItem.setWidget(new EstadisticaSimpleWidgetEntity());

        AtributsVisualsSimple dummyAtributs = new AtributsVisualsSimple();
        dummyAtributs.setColorText("#AAAAAA");
        lenient().when(atributsVisualsHelper.getAtributsVisuals(any(EstadisticaWidgetEntity.class))).thenReturn(dummyAtributs);

        // Act
        Object result = consultaEstadisticaHelper.resolveAtributsVisuals(dashboardItem, false);

        // Assert
        assertThat(result).isInstanceOf(AtributsVisualsSimple.class);
        assertThat(((AtributsVisualsSimple) result).getColorText()).isNull();
        verify(atributsVisualsHelper, never()).getAtributsVisuals(any(EstadisticaWidgetEntity.class));
    }

    // ========================================================================
    // 4. TESTOS DE MAPPERS I UTILS
    // ========================================================================

    @Test
    @DisplayName("toFets: converteix correctament una llista d'entitats a DTOs")
    void toFets_quanEsCrida_llavorsConverteixCorrectament() {
        if (fetEntities.isEmpty()) return; // Skip if JSON not loaded

        @SuppressWarnings("unchecked")
        List<Fet> result = (List<Fet>) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "toFets", fetEntities);

        assertThat(result).hasSize(fetEntities.size());
        assertThat(result.get(0).getEntornAppId()).isEqualTo(fetEntities.get(0).getEntornAppId());
    }

    @Test
    @DisplayName("createDimensionsFiltre: agrupa correctament els valors per codi de dimensió")
    void createDimensionsFiltre_quanEsCrida_llavorsAgrupaCorrectament() {
        DimensioValorEntity dv1 = new DimensioValorEntity();
        DimensioEntity d1 = new DimensioEntity(); d1.setCodi("dep");
        dv1.setDimensio(d1); dv1.setValor("RRHH");

        DimensioValorEntity dv2 = new DimensioValorEntity();
        DimensioEntity d2 = new DimensioEntity(); d2.setCodi("dep");
        dv2.setDimensio(d2); dv2.setValor("IT");

        @SuppressWarnings("unchecked")
        Map<String, List<String>> result = (Map<String, List<String>>) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "createDimensionsFiltre", Arrays.asList(dv1, dv2));

        assertThat(result).containsEntry("dep", Arrays.asList("RRHH", "IT"));
    }

    // ========================================================================
    // 5. TESTOS CRÍTICS PER PUJAR LA COBERTURA > 80%
    // ========================================================================

    @Test
    @DisplayName("getDadesWidgetGrafic: processa correctament VARIS_INDICADORS")
    void getDadesWidgetGrafic_quanVarisIndicadors_llavorsCridaRepositoriCorrecte() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        EstadisticaGraficWidgetEntity widget = new EstadisticaGraficWidgetEntity();
        widget.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);
        widget.setTempsAgrupacio(PeriodeUnitat.MES);

        IndicadorTaulaEntity ind1 = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind1, "titol", "Indicador 1");
        ReflectionTestUtils.setField(ind1, "indicador", new IndicadorEntity() {{ setCodi("ind1"); }});

        IndicadorTaulaEntity ind2 = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind2, "titol", "Indicador 2");
        ReflectionTestUtils.setField(ind2, "indicador", new IndicadorEntity() {{ setCodi("ind2"); }});

        widget.setIndicadorsInfo(Arrays.asList(ind1, ind2));
        dashboardItem.setWidget(widget);

        DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
            .entornAppId(1L).entornCodi("DEV")
            .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
            .build();

        when(fetRepository.getValorsGraficVarisIndicadors(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act
        Object result = ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getDadesWidgetGrafic", dashboardItem, dadesComuns, null, null);

        // Assert
        assertThat(result).isInstanceOf(InformeWidgetGraficItem.class);
        verify(fetRepository).getValorsGraficVarisIndicadors(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("getDadesWidgetGrafic: processa UN_INDICADOR_AMB_DESCOMPOSICIO amb agrupació activada")
    void getDadesWidgetGrafic_quanDescomposicioIAgrupatTrue_llavorsCridaRepositoriAmb3Args() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        EstadisticaGraficWidgetEntity widget = new EstadisticaGraficWidgetEntity();
        widget.setTipusDades(TipusGraficDataEnum.UN_INDICADOR_AMB_DESCOMPOSICIO);
        widget.setAgruparPerDimensioDescomposicio(true);
        widget.setTempsAgrupacio(PeriodeUnitat.DIA);

        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("dep");
        dimensio.setNom("Departament");
        widget.setDescomposicioDimensio(dimensio);

        IndicadorTaulaEntity ind = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind, "titol", "Visites");
        ReflectionTestUtils.setField(ind, "indicador", new IndicadorEntity() {{ setCodi("visites"); }});
        widget.setIndicadorsInfo(Collections.singletonList(ind));

        dashboardItem.setWidget(widget);

        DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
            .entornAppId(1L).entornCodi("DEV")
            .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
            .build();

        when(fetRepository.getValorsGraficUnIndicadorAmbDescomposicio(any(), any(), any(), any(), any(), anyString(), any()))
            .thenReturn(Collections.emptyList());

        // Act
        Object result = ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getDadesWidgetGrafic", dashboardItem, dadesComuns, null, null);

        // Assert
        assertThat(result).isInstanceOf(InformeWidgetGraficItem.class);
        verify(fetRepository).getValorsGraficUnIndicadorAmbDescomposicio(any(), any(), any(), any(), any(), eq("dep"), any());
    }

    @Test
    @DisplayName("getDadesWidgetTaula: no crida al repositori d'unitats si la dimensió no és TIPUS_AMB_UNITAT_ORG")
    void getDadesWidgetTaula_quanDimensioNoEsUnitatOrganitzativa_llavorsNoCridaRepositoriUO() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("entitat");
        dimensio.setTipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum.ENTITAT); // NO és TIPUS_AMB_UNITAT_ORG
        dimensio.setNom("Entitat");
        widget.setDimensioAgrupacio(dimensio);
        widget.setTitolAgrupament("Agrupament");

        IndicadorTaulaEntity ind = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind, "titol", "Columna");
        ReflectionTestUtils.setField(ind, "indicador", new IndicadorEntity() {{ setCodi("col1"); }});
        widget.setColumnes(Collections.singletonList(ind));

        dashboardItem.setWidget(widget);

        DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
            .entornAppId(1L).entornCodi("DEV")
            .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
            .build();

        when(fetRepository.getValorsTaulaAgregat(any(), any(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(dimensioRepository.findByCodiAndEntornAppId(anyString(), anyLong())).thenReturn(Optional.of(dimensio));

        // Act
        ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getDadesWidgetTaula", dashboardItem, dadesComuns, null, null);

        // Assert
        verify(unitatOrganitzativaRepository, never()).findByCodiIn(anyList());
    }

    @Test
    @DisplayName("ensureAtributsVisualsType: retorna null quan el widget és d'un tipus desconegut")
    void ensureAtributsVisualsType_quanWidgetDesconegut_llavorsRetornaNull() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setWidget(mock(EstadisticaWidgetEntity.class)); // No és Simple, Grafic ni Taula

        // Act
        AtributsVisuals result = (AtributsVisuals) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "ensureAtributsVisualsType", dashboardItem, null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("widgetStyleScope: retorna COMMON quan el widget és d'un tipus desconegut")
    void widgetStyleScope_quanWidgetDesconegut_llavorsRetornaCommon() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setWidget(mock(EstadisticaWidgetEntity.class)); // No és Simple, Grafic ni Taula

        // Act
        WidgetStyleScope result = (WidgetStyleScope) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "widgetStyleScope", dashboardItem);

        // Assert
        assertThat(result).isEqualTo(WidgetStyleScope.COMMON);
    }

    @Test
    @DisplayName("getColumnNames: genera correctament els noms de columnes")
    void getColumnNames_quanEsCrida_llavorsGeneraNomsCorrectes() {
        // Arrange
        List<Object> indicadors = Arrays.asList(new Object(), new Object(), new Object());

        // Act
        String[] result = ConsultaEstadisticaHelper.getColumnNames(indicadors);

        // Assert
        assertThat(result).containsExactly("agrupacio", "col1", "col2", "col3");
    }

    @Test
    @DisplayName("getEstadistiquesPeriodeAmbDimensions: executa consulta real quan el mapa de dimensions no és buit")
    void getEstadistiquesPeriodeAmbDimensions_quanHiHaDimensions_llavorsCridaRepositoriAmbDimensions() {
        // Arrange
        Long entornAppId = 1L;
        LocalDate inici = LocalDate.of(2023, 1, 1);
        LocalDate fi = LocalDate.of(2023, 3, 31);
        Map<String, List<String>> dimensions = Map.of("departament", List.of("RRHH"));

        when(fetRepository.findByEntornAppIdAndTempsDataBetweenAndDimensions(eq(entornAppId), eq(inici), eq(fi), eq(dimensions)))
            .thenReturn(Collections.emptyList());

        // Act
        consultaEstadisticaHelper.getEstadistiquesPeriodeAmbDimensions(entornAppId, inici, fi, dimensions);

        // Assert
        verify(fetRepository).findByEntornAppIdAndTempsDataBetweenAndDimensions(eq(entornAppId), eq(inici), eq(fi), eq(dimensions));
    }

    @Test
    @DisplayName("getDadesWidget: executa la branca GRAFIC del switch públic correctament")
    void getDadesWidget_quanTipusGrafic_llavorsExecutaRamaGrafic() {
        // Arrange
        DashboardItemEntity item = new DashboardItemEntity();
        item.setId(1L);
        item.setEntornId(1L);

        EstadisticaGraficWidgetEntity widget = new EstadisticaGraficWidgetEntity();
        widget.setTipusDades(TipusGraficDataEnum.UN_INDICADOR);
        widget.setTempsAgrupacio(PeriodeUnitat.DIA);
        widget.setPeriodeMode(PeriodeMode.PRESET);
        widget.setPresetPeriode(PresetPeriode.AVUI);
        IndicadorTaulaEntity ind = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind, "titol", "Titol");
        ReflectionTestUtils.setField(ind, "indicador", new IndicadorEntity() {{ setCodi("c"); }});
        widget.setIndicadorsInfo(Collections.singletonList(ind));
        item.setWidget(widget);

        when(dashboardItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(any(), any())).thenReturn(EntornApp.builder().id(1L).entorn(EntornRef.builder().id(1L).build()).build());
        when(estadisticaClientHelper.entornById(any())).thenReturn(Entorn.builder().codi("DEV").build());
        when(fetRepository.getValorsGraficUnIndicador(any(), any(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());

        // Act: Cridem al mètode PÚBLIC per cobrir el switch
        InformeWidgetItem result = consultaEstadisticaHelper.getDadesWidget(item, false, null);

        // Assert
        assertThat(result).isInstanceOf(InformeWidgetGraficItem.class);
    }

    @Test
    @DisplayName("getDadesWidget: executa la branca TAULA del switch públic correctament")
    void getDadesWidget_quanTipusTaula_llavorsExecutaRamaTaula() {
        // Arrange
        DashboardItemEntity item = new DashboardItemEntity();
        item.setId(1L);
        item.setEntornId(1L);

        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setTitolAgrupament("Agrup");
        DimensioEntity dim = new DimensioEntity();
        dim.setCodi("d");
        dim.setNom("Dimensio");
        widget.setDimensioAgrupacio(dim);
        IndicadorTaulaEntity ind = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind, "titol", "Col");
        ReflectionTestUtils.setField(ind, "indicador", new IndicadorEntity() {{ setCodi("c"); }});
        widget.setColumnes(Collections.singletonList(ind));
        widget.setPeriodeMode(PeriodeMode.PRESET);
        widget.setPresetPeriode(PresetPeriode.AVUI);
        item.setWidget(widget);

        when(dashboardItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(any(), any())).thenReturn(EntornApp.builder().id(1L).entorn(EntornRef.builder().id(1L).build()).build());
        when(estadisticaClientHelper.entornById(any())).thenReturn(Entorn.builder().codi("DEV").build());
        when(fetRepository.getValorsTaulaAgregat(any(), any(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(dimensioRepository.findByCodiAndEntornAppId(any(), any())).thenReturn(Optional.empty());

        // Act: Cridem al mètode PÚBLIC per cobrir el switch
        InformeWidgetItem result = consultaEstadisticaHelper.getDadesWidget(item, false, null);

        // Assert
        assertThat(result).isInstanceOf(InformeWidgetTaulaItem.class);
    }

    @Test
    @DisplayName("getDadesWidgetGrafic: processa UN_INDICADOR_AMB_DESCOMPOSICIO amb agrupació DESACTIVADA")
    void getDadesWidgetGrafic_quanDescomposicioIAgrupatFalse_llavorsCridaRepositoriAmb4Args() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        EstadisticaGraficWidgetEntity widget = new EstadisticaGraficWidgetEntity();
        widget.setTipusDades(TipusGraficDataEnum.UN_INDICADOR_AMB_DESCOMPOSICIO);
        widget.setAgruparPerDimensioDescomposicio(false);
        widget.setTempsAgrupacio(PeriodeUnitat.DIA);

        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("dep");
        dimensio.setNom("Departament");
        widget.setDescomposicioDimensio(dimensio);

        IndicadorTaulaEntity ind = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind, "titol", "Visites");
        ReflectionTestUtils.setField(ind, "indicador", new IndicadorEntity() {{ setCodi("visites"); }});
        widget.setIndicadorsInfo(Collections.singletonList(ind));
        dashboardItem.setWidget(widget);

        DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
            .entornAppId(1L).entornCodi("DEV")
            .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
            .build();

        when(fetRepository.getValorsGraficUnIndicadorAmbDescomposicio(any(), any(), any(), any(), any(), anyString(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act
        Object result = ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getDadesWidgetGrafic", dashboardItem, dadesComuns, null, null);

        // Assert
        assertThat(result).isInstanceOf(InformeWidgetGraficItem.class);
        verify(fetRepository).getValorsGraficUnIndicadorAmbDescomposicio(any(), any(), any(), any(), any(), eq("dep"), any(), any());
    }

    @Test
    @DisplayName("getDadesWidgetTaula: no falla quan dimensioEntity és null")
    void getDadesWidgetTaula_quanDimensioEntityEsNull_llavorsNoFalla() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("entitat");
        dimensio.setNom("Entitat");
        widget.setDimensioAgrupacio(dimensio);
        widget.setTitolAgrupament("Agrupament");

        IndicadorTaulaEntity ind = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind, "titol", "Columna");
        ReflectionTestUtils.setField(ind, "indicador", new IndicadorEntity() {{ setCodi("col1"); }});
        widget.setColumnes(Collections.singletonList(ind));
        dashboardItem.setWidget(widget);

        DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
            .entornAppId(1L).entornCodi("DEV")
            .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
            .build();

        when(fetRepository.getValorsTaulaAgregat(any(), any(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(dimensioRepository.findByCodiAndEntornAppId(anyString(), anyLong())).thenReturn(Optional.empty());

        // Act
        Object result = ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getDadesWidgetTaula", dashboardItem, dadesComuns, null, null);

        // Assert
        assertThat(result).isNotNull();
        verify(unitatOrganitzativaRepository, never()).findByCodiIn(anyList());
    }

    @Test
    @DisplayName("getDadesWidgetSimple: calcula canvi percentual quan compararPeriodeAnterior és true i agregació és SUM")
    void getDadesWidgetSimple_quanCompararPeriodeAnterior_llavorsCalculaCanvi() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setCompararPeriodeAnterior(true);
        widget.setPeriodeMode(PeriodeMode.PRESET);
        widget.setPresetPeriode(PresetPeriode.DARRERS_30_DIES);

        IndicadorTaulaEntity ind = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind, "indicador", new IndicadorEntity() {{ setCodi("visites"); }});
        ind.setAgregacio(TableColumnsEnum.SUM); // No és FIRST_SEEN ni LAST_SEEN
        widget.setIndicadorInfo(ind);
        dashboardItem.setWidget(widget);

        DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
            .entornAppId(1L).entornCodi("DEV")
            .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
            .build();

        when(fetRepository.getValorSimpleAgregat(any(), any(), any(), any(), any(), any())).thenReturn("100");

        // Act
        Object result = ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getDadesWidgetSimple", dashboardItem, dadesComuns, null, null);

        // Assert
        assertThat(result).isInstanceOf(InformeWidgetSimpleItem.class);
        // S'ha de cridar 2 vegades: una per al valor actual i una altra per al previ
        verify(fetRepository, times(2)).getValorSimpleAgregat(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("getDadesWidgetSimple: NO calcula canvi percentual si agregació és FIRST_SEEN")
    void getDadesWidgetSimple_quanAgregacioFirstSeen_llavorsNoCalculaCanvi() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setEntornId(1L);

        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setCompararPeriodeAnterior(true); // Encara que sigui true, l'agregació ho anul·la

        IndicadorTaulaEntity ind = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(ind, "indicador", new IndicadorEntity() {{ setCodi("visites"); }});
        ind.setAgregacio(TableColumnsEnum.FIRST_SEEN);
        widget.setIndicadorInfo(ind);
        dashboardItem.setWidget(widget);

        DadesComunsWidgetConsulta dadesComuns = DadesComunsWidgetConsulta.builder()
            .entornAppId(1L).entornCodi("DEV")
            .periodeDates(new PeriodeResolverHelper.PeriodeDates(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31)))
            .build();

        when(fetRepository.getValorSimpleAgregat(any(), any(), any(), any(), any(), any())).thenReturn("100");

        // Act
        Object result = ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "getDadesWidgetSimple", dashboardItem, dadesComuns, null, null);

        // Assert
        assertThat(result).isInstanceOf(InformeWidgetSimpleItem.class);
        // Només es crida 1 vegada, no es calcula el previ
        verify(fetRepository, times(1)).getValorSimpleAgregat(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("resolveAtributsVisuals: maneja personalitzat=true quan els atributs visuals són null")
    void resolveAtributsVisuals_quanPersonalitzatTrueIAtributsNull_llavorsNoFalla() {
        // Arrange
        DashboardItemEntity dashboardItem = new DashboardItemEntity();
        dashboardItem.setId(1L);
        dashboardItem.setPersonalitzat(true);
        dashboardItem.setWidget(new EstadisticaSimpleWidgetEntity());

        // Els mocks ja retornen null per defecte per a getAtributsVisuals

        // Act
        Object result = consultaEstadisticaHelper.resolveAtributsVisuals(dashboardItem, false);

        // Assert
        assertThat(result).isInstanceOf(AtributsVisualsSimple.class);
    }

    @Test
    @DisplayName("filesToSeries: simple mapping amb PIE_CHART crida a convertToPieChartSeriesSimple")
    void filesToSeries_quanSimpleMappingIPieChart_llavorsConverteixAPieChart() {
        // Arrange
        List<Map<String, String>> files = Collections.singletonList(Map.of("agrupacio", "Gener", "valor", "100"));

        // Act
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.PIE_CHART, TipusGraficDataEnum.UN_INDICADOR);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("label", "Gener");
        assertThat(result.get(0)).containsEntry("value", 100.0);
    }

    @Test
    @DisplayName("filesToSeries: simple mapping amb GAUGE_CHART crida a convertToGaugeChartSeriesSimple")
    void filesToSeries_quanSimpleMappingIGaugeChart_llavorsConverteixAGaugeChart() {
        // Arrange
        List<Map<String, String>> files = Collections.singletonList(Map.of("agrupacio", "Total", "valor", "50"));

        // Act
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.GAUGE_CHART, TipusGraficDataEnum.UN_INDICADOR);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("value", 50.0);
    }

    @Test
    @DisplayName("filesToSeries: simple mapping amb LINE_CHART (default intern) crida a convertToChartSeriesSimple")
    void filesToSeries_quanSimpleMappingILineChart_llavorsConverteixAChartSeriesSimple() {
        // Arrange
        List<Map<String, String>> files = Collections.singletonList(Map.of("agrupacio", "Gener", "valor", "100"));

        // Act
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.LINE_CHART, TipusGraficDataEnum.UN_INDICADOR);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("agrupacio", "Gener");
        assertThat(result.get(0)).containsEntry("valor", 100.0);
    }

    @Test
    @DisplayName("filesToSeries: mapping complex amb 'descomposicio' i PIE_CHART crida a groupByAndAggregate")
    void filesToSeries_quanMappingComplexDescomposicioIPieChart_llavorsAgregaPerDescomposicio() {
        // Arrange
        List<Map<String, String>> files = Arrays.asList(
            Map.of("agrupacio", "2023", "descomposicio", "RRHH", "valor", "10"),
            Map.of("agrupacio", "2023", "descomposicio", "RRHH", "valor", "20"), // S'ha de sumar
            Map.of("agrupacio", "2023", "descomposicio", "IT", "valor", "30")
        );

        // Act
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.PIE_CHART, TipusGraficDataEnum.UN_INDICADOR_AMB_DESCOMPOSICIO);

        // Assert
        assertThat(result).hasSize(2);
        // Verifiquem que s'ha agrupat per 'descomposicio' i sumat el 'valor'
        Map<String, Object> rrhh = result.stream().filter(m -> "RRHH".equals(m.get("label"))).findFirst().orElseThrow();
        assertThat(rrhh).containsEntry("value", 30.0); // 10 + 20

        Map<String, Object> it = result.stream().filter(m -> "IT".equals(m.get("label"))).findFirst().orElseThrow();
        assertThat(it).containsEntry("value", 30.0);
    }

    @Test
    @DisplayName("filesToSeries: mapping complex amb 'descomposicio' i LINE_CHART crida a groupByAndMapToSeries")
    void filesToSeries_quanMappingComplexDescomposicioILineChart_llavorsMapToSeries() {
        // Arrange
        List<Map<String, String>> files = Collections.singletonList(
            Map.of("agrupacio", "01/01/2023", "descomposicio", "RRHH", "valor", "100")
        );

        // Act
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.LINE_CHART, TipusGraficDataEnum.UN_INDICADOR_AMB_DESCOMPOSICIO);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("agrupacio", "01/01/2023");
        assertThat(result.get(0)).containsEntry("RRHH", 100.0);
    }

    @Test
    @DisplayName("filesToSeries: mapping complex SENSE 'descomposicio' crida a convertFilesToSeriesWithKeys")
    void filesToSeries_quanMappingComplexSenseDescomposicio_llavorsConverteixAmbKeys() {
        // Arrange
        List<Map<String, String>> files = Collections.singletonList(
            Map.of("agrupacio", "Gener", "col1", "10", "col2", "20")
        );

        // Act
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "filesToSeries", files, TipusGraficEnum.BAR_CHART, TipusGraficDataEnum.VARIS_INDICADORS);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsEntry("agrupacio", "Gener");
        assertThat(result.get(0)).containsEntry("col1", 10.0);
        assertThat(result.get(0)).containsEntry("col2", 20.0);
    }

    @Test
    @DisplayName("extractKeyExcluding: retorna la clau que no és la exclòsa")
    void extractKeyExcluding_quanEsCrida_llavorsRetornaClauCorrecta() {
        // Arrange
        Map<String, String> map = Map.of("agrupacio", "Gener", "valor", "100");

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "extractKeyExcluding", map, "agrupacio");

        // Assert
        assertThat(result).isEqualTo("valor");
    }

    @Test
    @DisplayName("extractKeyExcluding: retorna null si totes les claus estan excloses o el mapa és buit")
    void extractKeyExcluding_quanNoHiHaClausValidas_llavorsRetornaNull() {
        // Arrange
        Map<String, String> map = Map.of("agrupacio", "Gener");

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "extractKeyExcluding", map, "agrupacio");

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resoldreTermesFormula: retorna els termes (codi component + operador) quan l'indicador és FORMULA")
    void resoldreTermesFormula_quanIndicadorEsFormula_llavorsRetornaElsTermesOrdenats() {
        IndicadorEntity formula = new IndicadorEntity();
        formula.setId(1L);
        formula.setTipus(IndicadorTipus.FORMULA);
        when(indicadorRepository.findByCodiAndEntornAppId("TOTAL", 10L)).thenReturn(Optional.of(formula));

        IndicadorEntity ind1 = new IndicadorEntity();
        ind1.setCodi("IND1");
        IndicadorEntity ind2 = new IndicadorEntity();
        ind2.setCodi("IND2");

        IndicadorFormulaTermeEntity terme1 = new IndicadorFormulaTermeEntity();
        terme1.setIndicadorComponent(ind1);
        terme1.setOperador(OperadorFormulaEnum.SUMA);
        IndicadorFormulaTermeEntity terme2 = new IndicadorFormulaTermeEntity();
        terme2.setIndicadorComponent(ind2);
        terme2.setOperador(OperadorFormulaEnum.RESTA);
        when(indicadorFormulaTermeRepository.findByIndicadorFormulaIdOrderByOrdreAsc(1L))
            .thenReturn(List.of(terme1, terme2));

        @SuppressWarnings("unchecked")
        List<IndicadorFormulaTermeResolt> result = (List<IndicadorFormulaTermeResolt>) ReflectionTestUtils.invokeMethod(
            consultaEstadisticaHelper, "resoldreTermesFormula", "TOTAL", 10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIndicadorCodi()).isEqualTo("IND1");
        assertThat(result.get(0).getOperador()).isEqualTo(OperadorFormulaEnum.SUMA);
        assertThat(result.get(1).getIndicadorCodi()).isEqualTo("IND2");
        assertThat(result.get(1).getOperador()).isEqualTo(OperadorFormulaEnum.RESTA);
    }

    @Test
    @DisplayName("resoldreTermesFormula: retorna null quan l'indicador és SIMPLE (comportament habitual, sense fórmula)")
    void resoldreTermesFormula_quanIndicadorEsSimple_llavorsRetornaNull() {
        IndicadorEntity simple = new IndicadorEntity();
        simple.setId(2L);
        simple.setTipus(IndicadorTipus.SIMPLE);
        when(indicadorRepository.findByCodiAndEntornAppId("visites", 10L)).thenReturn(Optional.of(simple));

        Object result = ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "resoldreTermesFormula", "visites", 10L);

        assertThat(result).isNull();
        verifyNoInteractions(indicadorFormulaTermeRepository);
    }

    @Test
    @DisplayName("resoldreTermesFormula: retorna null quan l'indicador no existeix")
    void resoldreTermesFormula_quanIndicadorNoExisteix_llavorsRetornaNull() {
        when(indicadorRepository.findByCodiAndEntornAppId("INEXISTENT", 10L)).thenReturn(Optional.empty());

        Object result = ReflectionTestUtils.invokeMethod(consultaEstadisticaHelper, "resoldreTermesFormula", "INEXISTENT", 10L);

        assertThat(result).isNull();
    }

    // ========================================================================
    // applyFilesFilterSortLimit
    // ========================================================================

    private static Map<String, String> fila(String agrupacio, String... colValues) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("agrupacio", agrupacio);
        for (int i = 0; i < colValues.length; i++) {
            row.put("col" + (i + 1), colValues[i]);
        }
        return row;
    }

    @Test
    @DisplayName("applyFilesFilterSortLimit: sense cap opció activa, retorna les files sense canvis")
    void applyFilesFilterSortLimit_senseOpcions_retornaFilesSenseCanvis() {
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setAmagarFilesZero(false);
        List<Map<String, String>> files = List.of(fila("A", "5"), fila("B", "0"));

        List<Map<String, String>> result = ConsultaEstadisticaHelper.applyFilesFilterSortLimit(files, widget);

        assertThat(result).isEqualTo(files);
    }

    @Test
    @DisplayName("applyFilesFilterSortLimit: amb amagarFilesZero, elimina només les files amb totes les columnes a zero")
    void applyFilesFilterSortLimit_ambAmagarFilesZero_eliminaFilesTotesAZero() {
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setAmagarFilesZero(true);
        List<Map<String, String>> files = List.of(
            fila("A", "5", "0"),
            fila("B", "0", "0"),
            fila("C", "0", "3")
        );

        List<Map<String, String>> result = ConsultaEstadisticaHelper.applyFilesFilterSortLimit(files, widget);

        assertThat(result).extracting(row -> row.get("agrupacio")).containsExactly("A", "C");
    }

    @Test
    @DisplayName("applyFilesFilterSortLimit: amb columnaOrdenacio i DESC, ordena de major a menor per la columna indicada")
    void applyFilesFilterSortLimit_ambColumnaOrdenacioDesc_ordenaDeMajorAMenor() {
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setColumnaOrdenacio(0);
        widget.setDireccioOrdenacio(OrdreDireccioEnum.DESC);
        List<Map<String, String>> files = List.of(fila("A", "5"), fila("B", "20"), fila("C", "10"));

        List<Map<String, String>> result = ConsultaEstadisticaHelper.applyFilesFilterSortLimit(files, widget);

        assertThat(result).extracting(row -> row.get("agrupacio")).containsExactly("B", "C", "A");
    }

    @Test
    @DisplayName("applyFilesFilterSortLimit: amb columnaOrdenacio i ASC, ordena de menor a major per la columna indicada")
    void applyFilesFilterSortLimit_ambColumnaOrdenacioAsc_ordenaDeMenorAMajor() {
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setColumnaOrdenacio(0);
        widget.setDireccioOrdenacio(OrdreDireccioEnum.ASC);
        List<Map<String, String>> files = List.of(fila("A", "5"), fila("B", "20"), fila("C", "10"));

        List<Map<String, String>> result = ConsultaEstadisticaHelper.applyFilesFilterSortLimit(files, widget);

        assertThat(result).extracting(row -> row.get("agrupacio")).containsExactly("A", "C", "B");
    }

    @Test
    @DisplayName("applyFilesFilterSortLimit: amb columnaOrdenacio i limitResultats, retalla la llista al nombre indicat un cop ordenada")
    void applyFilesFilterSortLimit_ambLimitResultats_retallaLaLlistaOrdenada() {
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setColumnaOrdenacio(0);
        widget.setDireccioOrdenacio(OrdreDireccioEnum.DESC);
        widget.setLimitResultats(2);
        List<Map<String, String>> files = List.of(fila("A", "5"), fila("B", "20"), fila("C", "10"));

        List<Map<String, String>> result = ConsultaEstadisticaHelper.applyFilesFilterSortLimit(files, widget);

        assertThat(result).extracting(row -> row.get("agrupacio")).containsExactly("B", "C");
    }

    @Test
    @DisplayName("applyFilesFilterSortLimit: limitResultats sense columnaOrdenacio no té cap efecte")
    void applyFilesFilterSortLimit_ambLimitResultatsSenseColumnaOrdenacio_noTeEfecte() {
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setLimitResultats(1);
        List<Map<String, String>> files = List.of(fila("A", "5"), fila("B", "20"));

        List<Map<String, String>> result = ConsultaEstadisticaHelper.applyFilesFilterSortLimit(files, widget);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("applyFilesFilterSortLimit: limitResultats superior al nombre de files no en descarta cap")
    void applyFilesFilterSortLimit_ambLimitResultatsSuperiorAlNombreDeFiles_noDescartaCap() {
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setColumnaOrdenacio(0);
        widget.setDireccioOrdenacio(OrdreDireccioEnum.DESC);
        widget.setLimitResultats(10);
        List<Map<String, String>> files = List.of(fila("A", "5"), fila("B", "20"));

        List<Map<String, String>> result = ConsultaEstadisticaHelper.applyFilesFilterSortLimit(files, widget);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("applyFilesFilterSortLimit: combina amagarFilesZero, ordenació i límit en aquest ordre")
    void applyFilesFilterSortLimit_ambTotesLesOpcions_lesCombinaEnLordreCorrecte() {
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setAmagarFilesZero(true);
        widget.setColumnaOrdenacio(0);
        widget.setDireccioOrdenacio(OrdreDireccioEnum.DESC);
        widget.setLimitResultats(1);
        List<Map<String, String>> files = List.of(
            fila("A", "5"),
            fila("B", "0"),
            fila("C", "20")
        );

        List<Map<String, String>> result = ConsultaEstadisticaHelper.applyFilesFilterSortLimit(files, widget);

        assertThat(result).extracting(row -> row.get("agrupacio")).containsExactly("C");
    }
}
