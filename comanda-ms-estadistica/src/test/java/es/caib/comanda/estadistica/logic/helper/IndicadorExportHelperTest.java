package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.IndicadorExport;
import es.caib.comanda.estadistica.logic.intf.model.export.IndicadorFormulaTermeExport;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
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

import java.util.Collections;
import java.util.List;
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
