package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import es.caib.comanda.estadistica.logic.intf.model.widget.EntornResource;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorFormulaTermeEntity;
import es.caib.comanda.estadistica.persist.repository.IndicadorFormulaTermeRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a IndicadorServiceImpl")
class IndicadorServiceImplTest {

    @Mock
    private SpringFilterHelper springFilterHelper;
    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;
    @Mock
    private IndicadorRepository indicadorRepository;
    @Mock
    private IndicadorFormulaTermeRepository indicadorFormulaTermeRepository;
    @Mock
    private I18nUtil i18nUtil;
    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private IndicadorServiceImpl indicadorService;

    private IndicadorServiceImpl.CopiarIndicadorEntornAction copiarIndicadorEntornAction;

    @BeforeEach
    void setUpCopiarEntornAction() {
        // Configuració per evitar NPE en crides estàtiques a I18nUtil (vegeu DashboardImportHelperTest)
        ReflectionTestUtils.setField(I18nUtil.class, "applicationContext", applicationContext);
        lenient().when(applicationContext.getBean(I18nUtil.class)).thenReturn(i18nUtil);
        lenient().when(i18nUtil.getI18nMessage(anyString(), any())).thenAnswer(i -> i.getArgument(0));

        copiarIndicadorEntornAction = indicadorService.new CopiarIndicadorEntornAction();
    }

    @Test
    @DisplayName("namedFilterToSpecification retorna null per a filtres desconeguts")
    void namedFilterToSpecification_quanFiltreDesconegut_retornaNull() {
        assertThat(indicadorService.namedFilterToSpecification("desconegut")).isNull();
    }

    @Test
    @DisplayName("namedFilterToSpecification gestiona el filtre per app correctament")
    void namedFilterToSpecification_quanFiltreApp_retornaSpecification() {
        // Arrange
        String filterName = Indicador.NAMED_FILTER_BY_APP_GROUP_BY_NOM + ":1";
        List<Long> ids = List.of(10L, 20L);
        when(estadisticaClientHelper.getEntornAppsIdByAppId(1L)).thenReturn(ids);

        // Act
        Specification<IndicadorEntity> spec = indicadorService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verify(estadisticaClientHelper).getEntornAppsIdByAppId(1L);
    }

    @Test
    @DisplayName("additionalSpringFilter afegeix filtres d'aplicació si s'indiquen a namedQueries")
    void additionalSpringFilter_quanNamedQueriesAmbApp_afegeixFiltre() {
        // Arrange
        String currentFilter = "codi:'TEST'";
        String[] namedQueries = {"filterByApp:100"};
        
        // Simulem el comportament de springFilterHelper (com que retorna un objecte complex, podem mockejar-lo)
        // Però additionalSpringFilter crida a generate() de Filter.
        // Donat que Filter és una classe externa complexa, ens centrem en que es crida el helper.
        
        // Act
        indicadorService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        verify(springFilterHelper).filterByApp(eq(100L), anyString());
    }

    // ========================================================================
    // CopiarIndicadorEntornAction
    // ========================================================================

    private IndicadorEntity indicadorFormula(Long id, Long entornAppId) {
        IndicadorEntity indicador = new IndicadorEntity();
        ReflectionTestUtils.setField(indicador, "id", id);
        indicador.setCodi("FORM");
        indicador.setNom("Formula");
        indicador.setEntornAppId(entornAppId);
        indicador.setTipus(IndicadorTipus.FORMULA);
        return indicador;
    }

    private Indicador.CopiarIndicadorEntornParams paramsAmbEntorn(Long entornDestiId) {
        Indicador.CopiarIndicadorEntornParams params = new Indicador.CopiarIndicadorEntornParams();
        params.setEntornDesti(ResourceReference.toResourceReference(entornDestiId));
        return params;
    }

    @Test
    @DisplayName("copiarIndicadorEntorn: llança excepció quan l'indicador no és de tipus FORMULA")
    void copiarIndicadorEntorn_quanNoEsFormula_llancaExcepcio() {
        IndicadorEntity simple = new IndicadorEntity();
        simple.setTipus(IndicadorTipus.SIMPLE);

        assertThatThrownBy(() -> copiarIndicadorEntornAction.exec("copiar_indicador_entorn", simple, paramsAmbEntorn(2L)))
                .isInstanceOf(ActionExecutionException.class);
        verifyNoInteractions(indicadorRepository);
    }

    @Test
    @DisplayName("copiarIndicadorEntorn: llança excepció quan no s'indica l'entorn destí")
    void copiarIndicadorEntorn_quanSenseEntornDesti_llancaExcepcio() {
        IndicadorEntity formula = indicadorFormula(1L, 10L);

        assertThatThrownBy(() -> copiarIndicadorEntornAction.exec("copiar_indicador_entorn", formula, null))
                .isInstanceOf(ActionExecutionException.class);
    }

    @Test
    @DisplayName("copiarIndicadorEntorn: llança excepció quan l'entorn destí és el mateix que l'origen")
    void copiarIndicadorEntorn_quanMateixEntorn_llancaExcepcio() {
        IndicadorEntity formula = indicadorFormula(1L, 10L);

        EntornApp origenEntornApp = new EntornApp();
        origenEntornApp.setEntorn(new EntornRef(5L, "Entorn"));
        origenEntornApp.setApp(new AppRef(7L, "App"));
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(origenEntornApp);

        assertThatThrownBy(() -> copiarIndicadorEntornAction.exec("copiar_indicador_entorn", formula, paramsAmbEntorn(5L)))
                .isInstanceOf(ActionExecutionException.class);
    }

    @Test
    @DisplayName("copiarIndicadorEntorn: llança excepció quan l'App no està configurada a l'entorn destí")
    void copiarIndicadorEntorn_quanEntornAppDestiInexistent_llancaExcepcio() {
        IndicadorEntity formula = indicadorFormula(1L, 10L);

        EntornApp origenEntornApp = new EntornApp();
        origenEntornApp.setEntorn(new EntornRef(5L, "Entorn origen"));
        origenEntornApp.setApp(new AppRef(7L, "App"));
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(origenEntornApp);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(7L, 6L)).thenReturn(null);

        assertThatThrownBy(() -> copiarIndicadorEntornAction.exec("copiar_indicador_entorn", formula, paramsAmbEntorn(6L)))
                .isInstanceOf(ActionExecutionException.class);
    }

    @Test
    @DisplayName("copiarIndicadorEntorn: llança excepció quan ja existeix un indicador amb el mateix codi a l'entorn destí")
    void copiarIndicadorEntorn_quanJaExisteix_llancaExcepcio() {
        IndicadorEntity formula = indicadorFormula(1L, 10L);

        EntornApp origenEntornApp = new EntornApp();
        origenEntornApp.setEntorn(new EntornRef(5L, "Entorn origen"));
        origenEntornApp.setApp(new AppRef(7L, "App"));
        EntornApp destiEntornApp = new EntornApp();
        destiEntornApp.setId(20L);
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(origenEntornApp);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(7L, 6L)).thenReturn(destiEntornApp);
        when(indicadorRepository.findByCodiAndEntornAppId("FORM", 20L)).thenReturn(Optional.of(new IndicadorEntity()));

        assertThatThrownBy(() -> copiarIndicadorEntornAction.exec("copiar_indicador_entorn", formula, paramsAmbEntorn(6L)))
                .isInstanceOf(ActionExecutionException.class);
        verify(indicadorRepository, never()).save(any());
    }

    @Test
    @DisplayName("copiarIndicadorEntorn: llança excepció quan un component de la fórmula no existeix a l'entorn destí")
    void copiarIndicadorEntorn_quanComponentInexistent_llancaExcepcio() {
        IndicadorEntity formula = indicadorFormula(1L, 10L);

        EntornApp origenEntornApp = new EntornApp();
        origenEntornApp.setEntorn(new EntornRef(5L, "Entorn origen"));
        origenEntornApp.setApp(new AppRef(7L, "App"));
        EntornApp destiEntornApp = new EntornApp();
        destiEntornApp.setId(20L);
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(origenEntornApp);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(7L, 6L)).thenReturn(destiEntornApp);
        when(indicadorRepository.findByCodiAndEntornAppId("FORM", 20L)).thenReturn(Optional.empty());

        IndicadorEntity component = new IndicadorEntity();
        component.setCodi("COMP");
        IndicadorFormulaTermeEntity terme = new IndicadorFormulaTermeEntity();
        terme.setIndicadorComponent(component);
        terme.setOperador(OperadorFormulaEnum.SUMA);
        terme.setOrdre(0);
        when(indicadorFormulaTermeRepository.findByIndicadorFormulaIdOrderByOrdreAsc(1L)).thenReturn(List.of(terme));
        when(indicadorRepository.findByCodiAndEntornAppId("COMP", 20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> copiarIndicadorEntornAction.exec("copiar_indicador_entorn", formula, paramsAmbEntorn(6L)))
                .isInstanceOf(ActionExecutionException.class);
        verify(indicadorRepository, never()).save(any());
    }

    @Test
    @DisplayName("copiarIndicadorEntorn: copia l'indicador i els seus termes quan tot és correcte")
    void copiarIndicadorEntorn_quanTotCorrecte_copiaIndicadorITermes() {
        IndicadorEntity formula = indicadorFormula(1L, 10L);
        formula.setDescripcio("Descripció");

        EntornApp origenEntornApp = new EntornApp();
        origenEntornApp.setEntorn(new EntornRef(5L, "Entorn origen"));
        origenEntornApp.setApp(new AppRef(7L, "App"));
        EntornApp destiEntornApp = new EntornApp();
        destiEntornApp.setId(20L);
        when(estadisticaClientHelper.entornAppFindById(10L)).thenReturn(origenEntornApp);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(7L, 6L)).thenReturn(destiEntornApp);
        when(indicadorRepository.findByCodiAndEntornAppId("FORM", 20L)).thenReturn(Optional.empty());

        IndicadorEntity componentOrigen = new IndicadorEntity();
        componentOrigen.setCodi("COMP");
        IndicadorFormulaTermeEntity terme = new IndicadorFormulaTermeEntity();
        terme.setIndicadorComponent(componentOrigen);
        terme.setOperador(OperadorFormulaEnum.RESTA);
        terme.setOrdre(0);
        when(indicadorFormulaTermeRepository.findByIndicadorFormulaIdOrderByOrdreAsc(1L)).thenReturn(List.of(terme));

        IndicadorEntity componentDesti = new IndicadorEntity();
        componentDesti.setCodi("COMP");
        when(indicadorRepository.findByCodiAndEntornAppId("COMP", 20L)).thenReturn(Optional.of(componentDesti));

        // Act
        Indicador result = copiarIndicadorEntornAction.exec("copiar_indicador_entorn", formula, paramsAmbEntorn(6L));

        // Assert
        assertThat(result).isNull();

        ArgumentCaptor<IndicadorEntity> indicadorCaptor = ArgumentCaptor.forClass(IndicadorEntity.class);
        verify(indicadorRepository).save(indicadorCaptor.capture());
        IndicadorEntity nou = indicadorCaptor.getValue();
        assertThat(nou.getCodi()).isEqualTo("FORM");
        assertThat(nou.getDescripcio()).isEqualTo("Descripció");
        assertThat(nou.getEntornAppId()).isEqualTo(20L);
        assertThat(nou.getTipus()).isEqualTo(IndicadorTipus.FORMULA);
        assertThat(nou.getCompactable()).isFalse();

        ArgumentCaptor<IndicadorFormulaTermeEntity> termeCaptor = ArgumentCaptor.forClass(IndicadorFormulaTermeEntity.class);
        verify(indicadorFormulaTermeRepository).save(termeCaptor.capture());
        assertThat(termeCaptor.getValue().getIndicadorComponent()).isSameAs(componentDesti);
        assertThat(termeCaptor.getValue().getIndicadorFormula()).isSameAs(nou);
        assertThat(termeCaptor.getValue().getOperador()).isEqualTo(OperadorFormulaEnum.RESTA);
    }
}
