package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirParamAction;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet.FetObtenirResponse;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.estadistica.logic.mapper.FetMapper;
import es.caib.comanda.estadistica.logic.mapper.TempsMapper;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.TempsRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService.ReportGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a FetServiceImpl")
class FetServiceImplTest {

    @Mock private EstadisticaHelper estadisticaHelper;
    @Mock private ConsultaEstadisticaHelper consultaEstadisticaHelper;
    @Mock private EstadisticaClientHelper estadisticaClientHelper;
    @Mock private TempsRepository tempsRepository;
    @Mock private FetRepository fetRepository;
    @Mock private TempsMapper tempsMapper;
    @Mock private FetMapper fetMapper;

    @InjectMocks
    private FetServiceImpl fetService;

    // ========================================================================
    // 1. TESTOS PER A MÈTODES PÚBLICS DE DELEGACIÓ
    // ========================================================================

    @Test
    @DisplayName("obtenirFets: delega correctament a estadisticaHelper")
    void obtenirFets_quanIdValida_llavorsCridaHelper() {
        // Arrange
        Long entornAppId = 1L;
        EntornApp entornApp = new EntornApp();
        when(estadisticaClientHelper.entornAppFindById(entornAppId)).thenReturn(entornApp);

        // Act
        fetService.obtenirFets(entornAppId);

        // Assert
        verify(estadisticaHelper, times(1)).getEstadisticaInfoDades(entornApp);
    }

    @Test
    @DisplayName("obtenirFets: llança l'excepció quan estadisticaHelper falla")
    void obtenirFets_quanEstadisticaHelperFalla_llancaExcepcio() {
        // Arrange
        Long entornAppId = 1L;
        when(estadisticaClientHelper.entornAppFindById(entornAppId)).thenReturn(new EntornApp());
        doThrow(new RuntimeException("Error de connexió")).when(estadisticaHelper).getEstadisticaInfoDades(any());

        // Act & Assert
        assertThatThrownBy(() -> fetService.obtenirFets(entornAppId))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Error de connexió");
    }

    @Test
    @DisplayName("obtenirFets amb dies: delega correctament a estadisticaHelper")
    void obtenirFetsAmbDies_quanParametresValids_llavorsCridaHelper() {
        // Arrange
        Long entornAppId = 1L;
        int dies = 5;
        EntornApp entornApp = new EntornApp();
        when(estadisticaClientHelper.entornAppFindById(entornAppId)).thenReturn(entornApp);

        // Act
        fetService.obtenirFets(entornAppId, dies);

        // Assert
        verify(estadisticaHelper, times(1)).getEstadisticaInfoDades(entornApp, dies);
    }

    @Test
    @DisplayName("getEstadistiquesPeriode: delega correctament a consultaEstadisticaHelper")
    void getEstadistiquesPeriode_quanParametresValids_llavorsRetornaLlista() {
        // Arrange
        Long entornAppId = 1L;
        LocalDate inici = LocalDate.now().minusDays(7);
        LocalDate fi = LocalDate.now();
        List<Fet> fets = Collections.singletonList(new Fet());
        when(consultaEstadisticaHelper.getEstadistiquesPeriode(entornAppId, inici, fi)).thenReturn(fets);

        // Act
        List<Fet> result = fetService.getEstadistiquesPeriode(entornAppId, inici, fi);

        // Assert
        assertThat(result).isEqualTo(fets);
        verify(consultaEstadisticaHelper, times(1)).getEstadistiquesPeriode(entornAppId, inici, fi);
    }

    @Test
    @DisplayName("getEstadistiquesPeriodeAmbDimensions: delega correctament a consultaEstadisticaHelper")
    void getEstadistiquesPeriodeAmbDimensions_quanParametresValids_llavorsRetornaLlista() {
        // Arrange
        Long entornAppId = 1L;
        LocalDate inici = LocalDate.now().minusDays(7);
        LocalDate fi = LocalDate.now();
        var filtre = Collections.<String, List<String>>emptyMap();
        List<Fet> fets = Collections.singletonList(new Fet());
        when(consultaEstadisticaHelper.getEstadistiquesPeriodeAmbDimensions(entornAppId, inici, fi, filtre)).thenReturn(fets);

        // Act
        List<Fet> result = fetService.getEstadistiquesPeriodeAmbDimensions(entornAppId, inici, fi, filtre);

        // Assert
        assertThat(result).isEqualTo(fets);
        verify(consultaEstadisticaHelper, times(1)).getEstadistiquesPeriodeAmbDimensions(entornAppId, inici, fi, filtre);
    }

    // ========================================================================
    // 2. TESTOS PER A DatesDisponiblesReportGenerator
    // ========================================================================

    @Test
    @DisplayName("DatesDisponiblesReportGenerator: retorna llista mapejada quan hi ha dades")
    void datesDisponiblesReportGenerator_quanHiHaDades_llavorsRetornaLlistaMapejada() {
        // Arrange
        ReportGenerator<FetEntity, Long, Temps> generator = new FetServiceImpl.DatesDisponiblesReportGenerator(tempsRepository, tempsMapper);
        TempsEntity tempsEntity = new TempsEntity();
        Temps tempsMapped = new Temps();

        when(tempsRepository.findByEntornAppId(1L)).thenReturn(Collections.singletonList(tempsEntity));
        when(tempsMapper.toTemps(tempsEntity)).thenReturn(tempsMapped);

        // Act
        List<Temps> result = generator.generateData("CODE", null, 1L);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(tempsMapped);
    }

    @Test
    @DisplayName("DatesDisponiblesReportGenerator: retorna llista buida quan el repositori retorna null")
    void datesDisponiblesReportGenerator_quanRepositoriRetornaNull_llavorsRetornaBuit() {
        // Arrange
        ReportGenerator<FetEntity, Long, Temps> generator = new FetServiceImpl.DatesDisponiblesReportGenerator(tempsRepository, tempsMapper);
        when(tempsRepository.findByEntornAppId(1L)).thenReturn(null);

        // Act
        List<Temps> result = generator.generateData("CODE", null, 1L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("DatesDisponiblesReportGenerator: retorna llista buida i logueja error quan hi ha excepció")
    void datesDisponiblesReportGenerator_quanHiHaExcepcio_llavorsRetornaBuit() {
        // Arrange
        ReportGenerator<FetEntity, Long, Temps> generator = new FetServiceImpl.DatesDisponiblesReportGenerator(tempsRepository, tempsMapper);
        when(tempsRepository.findByEntornAppId(1L)).thenThrow(new RuntimeException("Error de BD"));

        // Act
        List<Temps> result = generator.generateData("CODE", null, 1L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 3. TESTOS PER A DadesDiaReportGenerator
    // ========================================================================

    @Test
    @DisplayName("DadesDiaReportGenerator: retorna llista mapejada quan hi ha dades")
    void dadesDiaReportGenerator_quanHiHaDades_llavorsRetornaLlistaMapejada() {
        // Arrange
        ReportGenerator<FetEntity, FetObtenirParamAction, Fet> generator = new FetServiceImpl.DadesDiaReportGenerator(fetRepository, fetMapper);
        FetEntity fetEntity = new FetEntity();
        Fet fetMapped = new Fet();
        FetObtenirParamAction params = new FetObtenirParamAction();
        params.setEntornAppId(1L);
        params.setDataInici(LocalDate.of(2023, 10, 25));

        when(fetRepository.findByEntornAppIdAndTempsData(1L, params.getDataInici())).thenReturn(Collections.singletonList(fetEntity));
        when(fetMapper.toFet(fetEntity)).thenReturn(fetMapped);

        // Act
        List<Fet> result = generator.generateData("CODE", null, params);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(fetMapped);
    }

    @Test
    @DisplayName("DadesDiaReportGenerator: retorna llista buida quan el repositori retorna null")
    void dadesDiaReportGenerator_quanRepositoriRetornaNull_llavorsRetornaBuit() {
        // Arrange
        ReportGenerator<FetEntity, FetObtenirParamAction, Fet> generator = new FetServiceImpl.DadesDiaReportGenerator(fetRepository, fetMapper);
        FetObtenirParamAction params = new FetObtenirParamAction();
        params.setEntornAppId(1L);
        params.setDataInici(LocalDate.of(2023, 10, 25));

        when(fetRepository.findByEntornAppIdAndTempsData(1L, params.getDataInici())).thenReturn(null);

        // Act
        List<Fet> result = generator.generateData("CODE", null, params);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("DadesDiaReportGenerator: retorna llista buida i logueja error quan hi ha excepció")
    void dadesDiaReportGenerator_quanHiHaExcepcio_llavorsRetornaBuit() {
        // Arrange
        ReportGenerator<FetEntity, FetObtenirParamAction, Fet> generator = new FetServiceImpl.DadesDiaReportGenerator(fetRepository, fetMapper);
        FetObtenirParamAction params = new FetObtenirParamAction();
        params.setEntornAppId(1L);
        params.setDataInici(LocalDate.of(2023, 10, 25));

        when(fetRepository.findByEntornAppIdAndTempsData(anyLong(), any())).thenThrow(new RuntimeException("Error de BD"));

        // Act
        List<Fet> result = generator.generateData("CODE", null, params);

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 4. TESTOS PER A ObtenirPerDataAction
    // ========================================================================

    @Test
    @DisplayName("ObtenirPerDataAction: construeix URL correcta i delega correctament")
    void obtenirPerDataAction_quanEsValid_llavorsConstrueixUrlICridaHelper() throws ActionExecutionException {
        // Arrange
        FetServiceImpl.ObtenirPerDataAction action = new FetServiceImpl.ObtenirPerDataAction(estadisticaClientHelper, estadisticaHelper);

        EntornApp entornApp = new EntornApp();
        entornApp.setEstadisticaUrl("http://test.com/api");
        when(estadisticaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);

        FetObtenirParamAction params = new FetObtenirParamAction();
        params.setEntornAppId(1L);
        params.setDataInici(LocalDate.of(2023, 10, 25));

        FetObtenirResponse expectedResponse = FetObtenirResponse.builder().success(true).build();
        when(estadisticaHelper.getEstadisticaInfoDadesAmbUrl(eq(entornApp), eq("http://test.com/api/of/25-10-2023"), eq(false)))
            .thenReturn(expectedResponse);

        // Act
        FetObtenirResponse result = action.exec("CODE", null, params);

        // Assert
        assertThat(result.getSuccess()).isTrue();
        verify(estadisticaHelper, times(1)).getEstadisticaInfoDadesAmbUrl(eq(entornApp), anyString(), eq(false));
    }

    @Test
    @DisplayName("ObtenirPerDataAction: retorna resposta amb success=false quan hi ha excepció")
    void obtenirPerDataAction_quanHiHaExcepcio_llavorsRetornaError() throws ActionExecutionException {
        // Arrange
        FetServiceImpl.ObtenirPerDataAction action = new FetServiceImpl.ObtenirPerDataAction(estadisticaClientHelper, estadisticaHelper);

        when(estadisticaClientHelper.entornAppFindById(1L)).thenThrow(new RuntimeException("Error de xarxa"));

        FetObtenirParamAction params = new FetObtenirParamAction();
        params.setEntornAppId(1L);
        params.setDataInici(LocalDate.of(2023, 10, 25));

        // Act
        FetObtenirResponse result = action.exec("CODE", null, params);

        // Assert
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Error de xarxa");
    }

    // ========================================================================
    // 5. TESTOS PER A ObtenirPerIntervalAction
    // ========================================================================

    @Test
    @DisplayName("ObtenirPerIntervalAction: construeix URL correcta amb múltiples dies i delega correctament")
    void obtenirPerIntervalAction_quanEsValid_llavorsConstrueixUrlICridaHelper() throws ActionExecutionException {
        // Arrange
        FetServiceImpl.ObtenirPerIntervalAction action = new FetServiceImpl.ObtenirPerIntervalAction(estadisticaClientHelper, estadisticaHelper);

        EntornApp entornApp = new EntornApp();
        entornApp.setEstadisticaUrl("http://test.com/api");
        when(estadisticaClientHelper.entornAppFindById(1L)).thenReturn(entornApp);

        FetObtenirParamAction params = new FetObtenirParamAction();
        params.setEntornAppId(1L);
        params.setDataInici(LocalDate.of(2023, 10, 25));
        params.setDataFi(LocalDate.of(2023, 10, 27));

        FetObtenirResponse expectedResponse = FetObtenirResponse.builder().success(true).build();
        when(estadisticaHelper.getEstadisticaInfoDadesAmbUrl(eq(entornApp), eq("http://test.com/api/from/25-10-2023/to/27-10-2023"), eq(true)))
            .thenReturn(expectedResponse);

        // Act
        FetObtenirResponse result = action.exec("CODE", null, params);

        // Assert
        assertThat(result.getSuccess()).isTrue();
        verify(estadisticaHelper, times(1)).getEstadisticaInfoDadesAmbUrl(eq(entornApp), anyString(), eq(true));
    }

    @Test
    @DisplayName("ObtenirPerIntervalAction: retorna resposta amb success=false quan hi ha excepció")
    void obtenirPerIntervalAction_quanHiHaExcepcio_llavorsRetornaError() throws ActionExecutionException {
        // Arrange
        FetServiceImpl.ObtenirPerIntervalAction action = new FetServiceImpl.ObtenirPerIntervalAction(estadisticaClientHelper, estadisticaHelper);

        when(estadisticaClientHelper.entornAppFindById(1L)).thenThrow(new RuntimeException("Error de xarxa"));

        FetObtenirParamAction params = new FetObtenirParamAction();
        params.setEntornAppId(1L);
        params.setDataInici(LocalDate.of(2023, 10, 25));
        params.setDataFi(LocalDate.of(2023, 10, 27));

        // Act
        FetObtenirResponse result = action.exec("CODE", null, params);

        // Assert
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).contains("Error de xarxa");
    }
}
