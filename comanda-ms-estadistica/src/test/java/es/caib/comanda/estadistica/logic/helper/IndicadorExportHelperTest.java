package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OverwriteEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.*;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.Conflict;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorFormulaTermeEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.IndicadorFormulaTermeRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a IndicadorExportHelper")
class IndicadorExportHelperTest {

    @Mock private EstadisticaClientHelper estadisticaClientHelper;
    @Mock private DashboardExportMapper dashboardExportMapper;
    @Mock private IndicadorRepository indicadorRepository;
    @Mock private IndicadorFormulaTermeRepository indicadorFormulaTermeRepository;

    @InjectMocks
    private IndicadorExportHelper indicadorExportHelper;

    // ========================================================================
    // collectIndicadorExports
    // ========================================================================

    @Test
    @DisplayName("collectIndicadorExports: retorna llista buida quan el dashboard no té items")
    void collectIndicadorExports_quanSenseItems_retornaBuit() {
        DashboardEntity dashboard = new DashboardEntity();

        List<IndicadorExport> result = indicadorExportHelper.collectIndicadorExports(dashboard);

        assertThat(result).isEmpty();
        verifyNoInteractions(dashboardExportMapper);
    }

    @Test
    @DisplayName("collectIndicadorExports: recull l'indicador d'un widget simple")
    void collectIndicadorExports_quanWidgetSimple_recullIndicador() {
        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setId(1L);
        indicador.setCodi("IND1");
        indicador.setTipus(IndicadorTipus.SIMPLE);

        IndicadorTaulaEntity indicadorInfo = new IndicadorTaulaEntity();
        indicadorInfo.setIndicador(indicador);
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setIndicadorInfo(indicadorInfo);

        DashboardEntity dashboard = dashboardAmbWidget(widget);

        IndicadorExport export = IndicadorExport.builder().codi("IND1").build();
        when(dashboardExportMapper.toIndicadorExport(indicador, estadisticaClientHelper)).thenReturn(export);

        List<IndicadorExport> result = indicadorExportHelper.collectIndicadorExports(dashboard);

        assertThat(result).containsExactly(export);
        verify(indicadorFormulaTermeRepository, never()).findByIndicadorFormulaIdOrderByOrdreAsc(anyLong());
    }

    @Test
    @DisplayName("collectIndicadorExports: recull recursivament els components d'una fórmula i evita duplicats")
    void collectIndicadorExports_quanFormula_recullComponentsSenseDuplicats() {
        IndicadorEntity component = new IndicadorEntity();
        component.setId(2L);
        component.setCodi("COMP");
        component.setTipus(IndicadorTipus.SIMPLE);

        IndicadorEntity formula = new IndicadorEntity();
        formula.setId(1L);
        formula.setCodi("FORM");
        formula.setTipus(IndicadorTipus.FORMULA);

        IndicadorFormulaTermeEntity terme = new IndicadorFormulaTermeEntity();
        terme.setIndicadorComponent(component);
        when(indicadorFormulaTermeRepository.findByIndicadorFormulaIdOrderByOrdreAsc(1L))
                .thenReturn(List.of(terme));

        // El mateix component apareix també directament en un widget gràfic (llista indicadorsInfo).
        IndicadorTaulaEntity componentInfo = new IndicadorTaulaEntity();
        componentInfo.setIndicador(component);
        EstadisticaGraficWidgetEntity graficWidget = new EstadisticaGraficWidgetEntity();
        graficWidget.setIndicadorsInfo(List.of(componentInfo));

        IndicadorTaulaEntity formulaInfo = new IndicadorTaulaEntity();
        formulaInfo.setIndicador(formula);
        EstadisticaTaulaWidgetEntity taulaWidget = new EstadisticaTaulaWidgetEntity();
        taulaWidget.setColumnes(List.of(formulaInfo));

        DashboardEntity dashboard = new DashboardEntity();
        DashboardItemEntity item1 = new DashboardItemEntity();
        item1.setWidget(graficWidget);
        DashboardItemEntity item2 = new DashboardItemEntity();
        item2.setWidget(taulaWidget);
        dashboard.setItems(List.of(item1, item2));

        IndicadorExport componentExport = IndicadorExport.builder().codi("COMP").build();
        IndicadorExport formulaExport = IndicadorExport.builder().codi("FORM").build();
        when(dashboardExportMapper.toIndicadorExport(component, estadisticaClientHelper)).thenReturn(componentExport);
        when(dashboardExportMapper.toIndicadorExport(formula, estadisticaClientHelper)).thenReturn(formulaExport);

        List<IndicadorExport> result = indicadorExportHelper.collectIndicadorExports(dashboard);

        // El component només s'ha de convertir un cop, encara que aparegui dues vegades (directe + fórmula).
        assertThat(result).containsExactlyInAnyOrder(componentExport, formulaExport);
        verify(dashboardExportMapper, times(1)).toIndicadorExport(component, estadisticaClientHelper);
    }

    private DashboardEntity dashboardAmbWidget(es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity widget) {
        DashboardEntity dashboard = new DashboardEntity();
        DashboardItemEntity item = new DashboardItemEntity();
        item.setWidget(widget);
        dashboard.setItems(Collections.singletonList(item));
        return dashboard;
    }

    // ========================================================================
    // importIndicadorsFormula
    // ========================================================================

    @Test
    @DisplayName("importIndicadorsFormula: no fa res quan la llista és null")
    void importIndicadorsFormula_quanNull_noFaRes() {
        indicadorExportHelper.importIndicadorsFormula(null);
        verifyNoInteractions(indicadorRepository, estadisticaClientHelper);
    }

    @Test
    @DisplayName("importIndicadorsFormula: ignora els indicadors de tipus SIMPLE")
    void importIndicadorsFormula_quanSimple_noElCrea() {
        IndicadorExport simple = IndicadorExport.builder().codi("IND1").tipus(IndicadorTipus.SIMPLE).build();

        indicadorExportHelper.importIndicadorsFormula(List.of(simple));

        verifyNoInteractions(indicadorRepository);
    }

    @Test
    @DisplayName("importIndicadorsFormula: no crea l'indicador FORMULA si ja existeix a l'entornApp destí")
    void importIndicadorsFormula_quanJaExisteix_noElTornaACrear() {
        IndicadorExport formula = IndicadorExport.builder()
                .codi("FORM").entornCodi("ENT").appCodi("APP").tipus(IndicadorTipus.FORMULA).build();

        mockEntornApp("ENT", "APP", 10L);
        when(indicadorRepository.findByCodiAndEntornAppId("FORM", 10L)).thenReturn(Optional.of(new IndicadorEntity()));

        indicadorExportHelper.importIndicadorsFormula(List.of(formula));

        verify(indicadorRepository, never()).save(any());
    }

    @Test
    @DisplayName("importIndicadorsFormula: crea l'indicador FORMULA i els seus termes quan no existeix")
    void importIndicadorsFormula_quanNoExisteix_elCreaAmbTermes() {
        IndicadorFormulaTermeExport terme = IndicadorFormulaTermeExport.builder()
                .indicadorComponentCodi("COMP").operador(OperadorFormulaEnum.SUMA).ordre(0).build();
        IndicadorExport formula = IndicadorExport.builder()
                .codi("FORM").nom("Formula").entornCodi("ENT").appCodi("APP")
                .tipus(IndicadorTipus.FORMULA).formula(List.of(terme)).build();

        mockEntornApp("ENT", "APP", 10L);
        when(indicadorRepository.findByCodiAndEntornAppId("FORM", 10L)).thenReturn(Optional.empty());
        IndicadorEntity component = new IndicadorEntity();
        component.setCodi("COMP");
        when(indicadorRepository.findByCodiAndEntornAppId("COMP", 10L)).thenReturn(Optional.of(component));

        indicadorExportHelper.importIndicadorsFormula(List.of(formula));

        org.mockito.ArgumentCaptor<IndicadorEntity> indicadorCaptor = org.mockito.ArgumentCaptor.forClass(IndicadorEntity.class);
        verify(indicadorRepository).save(indicadorCaptor.capture());
        assertThat(indicadorCaptor.getValue().getCodi()).isEqualTo("FORM");
        assertThat(indicadorCaptor.getValue().getTipus()).isEqualTo(IndicadorTipus.FORMULA);
        assertThat(indicadorCaptor.getValue().getEntornAppId()).isEqualTo(10L);

        org.mockito.ArgumentCaptor<IndicadorFormulaTermeEntity> termeCaptor = org.mockito.ArgumentCaptor.forClass(IndicadorFormulaTermeEntity.class);
        verify(indicadorFormulaTermeRepository).save(termeCaptor.capture());
        assertThat(termeCaptor.getValue().getIndicadorComponent()).isSameAs(component);
        assertThat(termeCaptor.getValue().getOperador()).isEqualTo(OperadorFormulaEnum.SUMA);
    }

    @Test
    @DisplayName("importIndicadorsFormula: quan conflicte és EMPRAR_EXISTENT, reutilitza indicador existent i remapeja widgets")
    void importIndicadorsFormula_quanConflicteEmprarExistent_reutilitzaExistentIRemapejaWidgets() {
        IndicadorExport formula = IndicadorExport.builder()
                .codi("FORM_OLD").nom("Formula 1").entornCodi("ENT").appCodi("APP")
                .tipus(IndicadorTipus.FORMULA).build();

        DashboardExport dashboard = new DashboardExport();
        dashboard.setIndicadors(List.of(formula));

        // Widgets que referencien FORM_OLD
        EstadisticaSimpleWidgetExport simpleWidget = new EstadisticaSimpleWidgetExport();
        IndicadorTaulaExport simpleInd = new IndicadorTaulaExport();
        simpleInd.setIndicadorCodi("FORM_OLD");
        simpleWidget.setIndicadorInfo(simpleInd);

        EstadisticaGraficWidgetExport graficWidget = new EstadisticaGraficWidgetExport();
        IndicadorTaulaExport graficInd = new IndicadorTaulaExport();
        graficInd.setIndicadorCodi("FORM_OLD");
        graficWidget.setIndicadorInfo(graficInd);
        IndicadorTaulaExport graficIndList = new IndicadorTaulaExport();
        graficIndList.setIndicadorCodi("FORM_OLD");
        graficWidget.setIndicadorsInfo(List.of(graficIndList));

        EstadisticaTaulaWidgetExport taulaWidget = new EstadisticaTaulaWidgetExport();
        IndicadorTaulaExport colInd = new IndicadorTaulaExport();
        colInd.setIndicadorCodi("FORM_OLD");
        taulaWidget.setColumnes(List.of(colInd));

        DashboardItemExport item1 = new DashboardItemExport();
        item1.setWidget(simpleWidget);
        DashboardItemExport item2 = new DashboardItemExport();
        item2.setWidget(graficWidget);
        DashboardItemExport item3 = new DashboardItemExport();
        item3.setWidget(taulaWidget);
        dashboard.setItems(List.of(item1, item2, item3));

        mockEntornApp("ENT", "APP", 10L);

        IndicadorEntity existent = new IndicadorEntity();
        existent.setId(99L);
        existent.setCodi("FORM_OLD");
        existent.setNom("Formula 1");
        when(indicadorRepository.findByCodiAndEntornAppId("FORM_OLD", 10L)).thenReturn(Optional.of(existent));

        Conflict conflict = new Conflict("Formula 1", IndicadorExport.class.getSimpleName());
        conflict.setCodi("FORM_OLD");
        conflict.setOverwrite(OverwriteEnum.EMPRAR_EXISTENT);

        Map<String, String> remappedCodis = new HashMap<>();
        indicadorExportHelper.importIndicadorsFormula(dashboard, List.of(conflict), remappedCodis);

        assertThat(remappedCodis).containsEntry("FORM_OLD", "FORM_OLD");
        assertThat(simpleWidget.getIndicadorInfo().getIndicadorCodi()).isEqualTo("FORM_OLD");
        assertThat(graficWidget.getIndicadorInfo().getIndicadorCodi()).isEqualTo("FORM_OLD");
        assertThat(graficWidget.getIndicadorsInfo().get(0).getIndicadorCodi()).isEqualTo("FORM_OLD");
        assertThat(taulaWidget.getColumnes().get(0).getIndicadorCodi()).isEqualTo("FORM_OLD");
        verify(indicadorRepository, never()).save(any());
    }

    @Test
    @DisplayName("importIndicadorsFormula: quan conflicte és SOBRESCRIURE, actualitza indicador existent per codi i substitueix termes de la fórmula")
    void importIndicadorsFormula_quanConflicteSobrescriure_actualitzaIndicadorPerCodiISubstitueixTermes() {
        IndicadorFormulaTermeExport terme = IndicadorFormulaTermeExport.builder()
                .indicadorComponentCodi("COMP").operador(OperadorFormulaEnum.SUMA).ordre(0).build();
        IndicadorExport formula = IndicadorExport.builder()
                .codi("FORM_OLD").nom("Formula 1").descripcio("Nova descripcio").entornCodi("ENT").appCodi("APP")
                .tipus(IndicadorTipus.FORMULA).formula(List.of(terme)).build();

        DashboardExport dashboard = new DashboardExport();
        dashboard.setIndicadors(List.of(formula));

        EstadisticaSimpleWidgetExport simpleWidget = new EstadisticaSimpleWidgetExport();
        IndicadorTaulaExport simpleInd = new IndicadorTaulaExport();
        simpleInd.setIndicadorCodi("FORM_OLD");
        simpleWidget.setIndicadorInfo(simpleInd);

        DashboardItemExport item = new DashboardItemExport();
        item.setWidget(simpleWidget);
        dashboard.setItems(List.of(item));

        mockEntornApp("ENT", "APP", 10L);

        IndicadorEntity existent = new IndicadorEntity();
        existent.setId(99L);
        existent.setCodi("FORM_OLD");
        existent.setNom("Formula 1");
        existent.setDescripcio("Vella descripcio");
        when(indicadorRepository.findByCodiAndEntornAppId("FORM_OLD", 10L)).thenReturn(Optional.of(existent));

        IndicadorFormulaTermeEntity oldTerme = new IndicadorFormulaTermeEntity();
        oldTerme.setId(101L);
        when(indicadorFormulaTermeRepository.findByIndicadorFormulaIdOrderByOrdreAsc(99L)).thenReturn(List.of(oldTerme));

        IndicadorEntity component = new IndicadorEntity();
        component.setCodi("COMP");
        when(indicadorRepository.findByCodiAndEntornAppId("COMP", 10L)).thenReturn(Optional.of(component));

        Conflict conflict = new Conflict("Formula 1", IndicadorExport.class.getSimpleName());
        conflict.setCodi("FORM_OLD");
        conflict.setOverwrite(OverwriteEnum.SOBRESCRIURE);

        Map<String, String> remappedCodis = new HashMap<>();
        indicadorExportHelper.importIndicadorsFormula(dashboard, List.of(conflict), remappedCodis);

        assertThat(remappedCodis).containsEntry("FORM_OLD", "FORM_OLD");
        assertThat(simpleWidget.getIndicadorInfo().getIndicadorCodi()).isEqualTo("FORM_OLD");

        verify(indicadorFormulaTermeRepository).deleteAll(List.of(oldTerme));
        verify(indicadorFormulaTermeRepository).save(any(IndicadorFormulaTermeEntity.class));

        verify(indicadorRepository).save(existent);
        assertThat(existent.getDescripcio()).isEqualTo("Nova descripcio");
        assertThat(existent.getCodi()).isEqualTo("FORM_OLD");
    }

    @Test
    @DisplayName("importIndicadorsFormula: quan el nom ha canviat (correcció errata) però el codi és el mateix, SOBRESCRIURE actualitza el nom a la BDD")
    void importIndicadorsFormula_quanNomHaCanviatPeroCodiEsElMateix_sobrescriureActualitzaElNom() {
        IndicadorExport formula = IndicadorExport.builder()
                .codi("IND_COM").nom("Total Comandes").descripcio("Nova descripcio").entornCodi("ENT").appCodi("APP")
                .tipus(IndicadorTipus.FORMULA).build();

        DashboardExport dashboard = new DashboardExport();
        dashboard.setIndicadors(List.of(formula));

        mockEntornApp("ENT", "APP", 10L);

        IndicadorEntity existent = new IndicadorEntity();
        existent.setId(99L);
        existent.setCodi("IND_COM");
        existent.setNom("Total Comades"); // errata prèvia a la BDD
        when(indicadorRepository.findByCodiAndEntornAppId("IND_COM", 10L)).thenReturn(Optional.of(existent));

        Conflict conflict = new Conflict("Total Comandes", IndicadorExport.class.getSimpleName());
        conflict.setCodi("IND_COM");
        conflict.setOverwrite(OverwriteEnum.SOBRESCRIURE);

        indicadorExportHelper.importIndicadorsFormula(dashboard, List.of(conflict), new HashMap<>());

        verify(indicadorRepository).save(existent);
        assertThat(existent.getNom()).isEqualTo("Total Comandes");
    }

    @Test
    @DisplayName("importIndicadorsFormula: quan el nom ha canviat però el codi és el mateix, EMPRAR_EXISTENT manté el nom existent")
    void importIndicadorsFormula_quanNomHaCanviatPeroCodiEsElMateix_emprarExistentManteElNomExistent() {
        IndicadorExport formula = IndicadorExport.builder()
                .codi("IND_COM").nom("Total Comandes").descripcio("Nova descripcio").entornCodi("ENT").appCodi("APP")
                .tipus(IndicadorTipus.FORMULA).build();

        DashboardExport dashboard = new DashboardExport();
        dashboard.setIndicadors(List.of(formula));

        mockEntornApp("ENT", "APP", 10L);

        IndicadorEntity existent = new IndicadorEntity();
        existent.setId(99L);
        existent.setCodi("IND_COM");
        existent.setNom("Total Comades"); // nom amb errata a la BDD
        when(indicadorRepository.findByCodiAndEntornAppId("IND_COM", 10L)).thenReturn(Optional.of(existent));

        Conflict conflict = new Conflict("Total Comandes", IndicadorExport.class.getSimpleName());
        conflict.setCodi("IND_COM");
        conflict.setOverwrite(OverwriteEnum.EMPRAR_EXISTENT);

        indicadorExportHelper.importIndicadorsFormula(dashboard, List.of(conflict), new HashMap<>());

        verify(indicadorRepository, never()).save(any());
        assertThat(existent.getNom()).isEqualTo("Total Comades");
    }

    @Test
    @DisplayName("importIndicadorsFormula: associa l'indicador comptador per mitjana si està definit a l'exportació")
    void importIndicadorsFormula_quanTeIndicadorComptadorPerMitjana_mapejaComptador() {
        IndicadorExport formula = IndicadorExport.builder()
                .codi("FORM").nom("Formula").entornCodi("ENT").appCodi("APP")
                .tipus(IndicadorTipus.FORMULA).indicadorComptadorPerMitjanaCodi("COMPTADOR").build();

        mockEntornApp("ENT", "APP", 10L);
        when(indicadorRepository.findByCodiAndEntornAppId("FORM", 10L)).thenReturn(Optional.empty());

        IndicadorEntity comptador = new IndicadorEntity();
        comptador.setCodi("COMPTADOR");
        when(indicadorRepository.findByCodiAndEntornAppId("COMPTADOR", 10L)).thenReturn(Optional.of(comptador));

        indicadorExportHelper.importIndicadorsFormula(List.of(formula));

        org.mockito.ArgumentCaptor<IndicadorEntity> captor = org.mockito.ArgumentCaptor.forClass(IndicadorEntity.class);
        verify(indicadorRepository).save(captor.capture());
        assertThat(captor.getValue().getIndicadorComptadorPerMitjana()).isSameAs(comptador);
    }

    @Test
    @DisplayName("importIndicadorsFormula: quan hi ha conflictes amb el mateix codi en diferents entorns, resol segons entornAppId")
    void importIndicadorsFormula_quanMateixCodiDiferentsEntorns_resolConflicteSegonsEntornAppId() {
        IndicadorExport formula = IndicadorExport.builder()
                .codi("IND_COM").nom("Total Comandes").descripcio("Nova descripcio").entornCodi("ENT").appCodi("APP")
                .tipus(IndicadorTipus.FORMULA).build();

        DashboardExport dashboard = new DashboardExport();
        dashboard.setIndicadors(List.of(formula));

        mockEntornApp("ENT", "APP", 10L);

        IndicadorEntity existent = new IndicadorEntity();
        existent.setId(99L);
        existent.setCodi("IND_COM");
        existent.setNom("Total Comades");
        when(indicadorRepository.findByCodiAndEntornAppId("IND_COM", 10L)).thenReturn(Optional.of(existent));

        // Conflicte per a un altre entorn (entornAppId = 20) amb SOBRESCRIURE
        Conflict conflictAltreEntorn = new Conflict("Total Comandes", IndicadorExport.class.getSimpleName());
        conflictAltreEntorn.setCodi("IND_COM");
        conflictAltreEntorn.setEntornAppId(20L);
        conflictAltreEntorn.setOverwrite(OverwriteEnum.SOBRESCRIURE);

        // Conflicte per al nostre entorn (entornAppId = 10) amb EMPRAR_EXISTENT
        Conflict conflictNostreEntorn = new Conflict("Total Comandes", IndicadorExport.class.getSimpleName());
        conflictNostreEntorn.setCodi("IND_COM");
        conflictNostreEntorn.setEntornAppId(10L);
        conflictNostreEntorn.setOverwrite(OverwriteEnum.EMPRAR_EXISTENT);

        indicadorExportHelper.importIndicadorsFormula(dashboard, List.of(conflictAltreEntorn, conflictNostreEntorn), new HashMap<>());

        // Hauria d'haver aplicat EMPRAR_EXISTENT (no save) i no SOBRESCRIURE de l'altre entorn
        verify(indicadorRepository, never()).save(any());
        assertThat(existent.getNom()).isEqualTo("Total Comades");
    }

    private void mockEntornApp(String entornCodi, String appCodi, Long entornAppId) {
        Entorn entorn = new Entorn();
        org.springframework.test.util.ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        org.springframework.test.util.ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp();
        entornApp.setId(entornAppId);
        when(estadisticaClientHelper.entornByCodi(entornCodi)).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi(appCodi)).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 1L)).thenReturn(entornApp);
    }

}
