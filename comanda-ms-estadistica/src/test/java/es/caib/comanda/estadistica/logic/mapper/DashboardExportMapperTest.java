package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula;
import es.caib.comanda.estadistica.logic.intf.model.export.*;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.*;
import es.caib.comanda.estadistica.persist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardExportMapperTest {

    private DashboardExportMapper mapper;

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @Mock
    private AtributsVisualsHelper atributsVisualsHelper;

    @Mock
    private IndicadorRepository indicadorRepository;

    @Mock
    private DimensioRepository dimensioRepository;

    @Mock
    private DimensioValorRepository dimensioValorRepository;

    private Entorn entorn = new Entorn(10L, "ENTORN001", null);
    private App app = new App();
    private EntornApp entornApp = new EntornApp();
    private DimensioEntity dimensio = new DimensioEntity();

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DashboardExportMapper.class);

        ReflectionTestUtils.setField(app, "id", 20L);
        ReflectionTestUtils.setField(app, "codi", "APP001");
        entornApp.setId(30L);
        dimensio.setCodi("DIM001");

        lenient().when(estadisticaClientHelper.entornById(10L)).thenReturn(entorn);
        lenient().when(estadisticaClientHelper.appFindById(20L)).thenReturn(app);

        lenient().when(estadisticaClientHelper.entornByCodi("ENTORN001")).thenReturn(entorn);
        lenient().when(estadisticaClientHelper.appFindByCodi("APP001")).thenReturn(app);

        lenient().when(estadisticaClientHelper.entornAppFindByAppAndEntorn(20L, 10L)).thenReturn(entornApp);
        lenient().when(dimensioRepository.findByCodiAndEntornAppId("DIM001", 30L)).thenReturn(Optional.of(dimensio));
    }

    // ============================================================================
    // TESTS DE CONVERSIÓN: Entity → Export
    // ============================================================================

    @Test
    void testToDashboardExport_Success() {
        // Given
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setTitol("Dashboard Test");
        entity.setEntornId(entorn.getId());
        entity.setAppId(app.getId());
        entity.setColorFonsClar("#111111");
        entity.setColorFonsFosc("#222222");

        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setNom("Plantilla Test");
        entity.setPlantilla(plantilla);

        // When
        DashboardExport result = mapper.toDashboardExport(entity, estadisticaClientHelper, atributsVisualsHelper);

        // Then
        assertNotNull(result);
        assertEquals("Dashboard Test", result.getTitol());
        assertEquals("ENTORN001", result.getEntornCodi());
        assertEquals("APP001", result.getAppCodi());
        assertEquals("Plantilla Test", result.getPlantilla().getNom());
        assertEquals("#111111", result.getColorFonsClar());
        assertEquals("#222222", result.getColorFonsFosc());

        verify(estadisticaClientHelper).entornById(10L);
        verify(estadisticaClientHelper).appFindById(20L);
    }

    @Test
    void testToDashboardExport_NullEntity() {
        // When
        DashboardExport result = mapper.toDashboardExport((DashboardEntity) null, estadisticaClientHelper, atributsVisualsHelper);

        // Then
        assertNull(result);
    }

    @Test
    void testToDashboardExport_NullEntornId() {
        // Given
        DashboardEntity entity = new DashboardEntity();
        entity.setEntornId(null);

        // When
        DashboardExport result = mapper.toDashboardExport(entity, estadisticaClientHelper, atributsVisualsHelper);

        // Then
        assertNotNull(result);
        assertNull(result.getEntornCodi());
        verify(estadisticaClientHelper, never()).entornById(anyLong());
    }

    @Test
    void testToDashboardExport_EntornNotFound() {
        // Given
        DashboardEntity entity = new DashboardEntity();
        entity.setEntornId(99L);

        when(estadisticaClientHelper.entornById(99L)).thenReturn(null);

        // When
        DashboardExport result = mapper.toDashboardExport(entity, estadisticaClientHelper, atributsVisualsHelper);

        // Then
        assertNotNull(result);
        assertNull(result.getEntornCodi());
    }

    @Test
    void testToDashboardExportList_Success() {
        // Given
        DashboardEntity entity1 = new DashboardEntity();
        entity1.setId(1L);
        entity1.setTitol("Dashboard 1");

        DashboardEntity entity2 = new DashboardEntity();
        entity2.setId(2L);
        entity2.setTitol("Dashboard 2");

        List<DashboardEntity> entities = Arrays.asList(entity1, entity2);

        // When
        List<DashboardExport> result = mapper.toDashboardExport(entities, estadisticaClientHelper, atributsVisualsHelper);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Dashboard 1", result.get(0).getTitol());
        assertEquals("Dashboard 2", result.get(1).getTitol());
    }

    @Test
    void testToDashboardExportList_Null() {
        // When
        List<DashboardExport> result = mapper.toDashboardExport((List<DashboardEntity>) null, estadisticaClientHelper, atributsVisualsHelper);

        // Then
        assertNull(result);
    }

    @Test
    void testToDashboardItemExport_SimpleWidget() {
        // Given
        DashboardItemEntity itemEntity = new DashboardItemEntity();
        itemEntity.setId(1L);
        itemEntity.setEntornId(entorn.getId());

        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setAppId(app.getId());
        itemEntity.setWidget(widget);

        AtributsVisualsSimple atributs = new AtributsVisualsSimple();

        when(atributsVisualsHelper.getAtributsVisuals(widget)).thenReturn(atributs);

        // When
        DashboardItemExport result = mapper.toDashboardItemExport(itemEntity, estadisticaClientHelper, atributsVisualsHelper);

        // Then
        assertNotNull(result);
        assertEquals(entorn.getCodi(), result.getEntornCodi());
        assertEquals(app.getCodi(), result.getAppCodi());
        assertNotNull(result.getWidget());
        assertInstanceOf(EstadisticaSimpleWidgetExport.class, result.getWidget());
        assertEquals(WidgetTipus.SIMPLE, result.getWidget().getTipus());
    }

    @Test
    void testToWidgetExport_GraficWidget() {
        // Given
        EstadisticaGraficWidgetEntity widget = new EstadisticaGraficWidgetEntity();
        widget.setId(1L);

        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("DIM001");
        widget.setDescomposicioDimensio(dimensio);

        AtributsVisualsGrafic atributs = new AtributsVisualsGrafic();
        when(atributsVisualsHelper.getAtributsVisuals(widget)).thenReturn(atributs);

        // When
        EstadisticaWidgetExport result = mapper.toWidgetExport(widget, atributsVisualsHelper);

        // Then
        assertNotNull(result);
        assertInstanceOf(EstadisticaGraficWidgetExport.class, result);
        EstadisticaGraficWidgetExport graficExport = (EstadisticaGraficWidgetExport) result;
        assertEquals(WidgetTipus.GRAFIC, graficExport.getTipus());
        assertEquals(dimensio.getCodi(), graficExport.getDescomposicioDimensioCodi());
        assertEquals(atributs, graficExport.getAtributsVisuals());
    }

    @Test
    void testToWidgetExport_TaulaWidget() {
        // Given
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setId(1L);

        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("DIM002");
        widget.setDimensioAgrupacio(dimensio);

        AtributsVisualsTaula atributs = new AtributsVisualsTaula();
        when(atributsVisualsHelper.getAtributsVisuals(widget)).thenReturn(atributs);

        // When
        EstadisticaWidgetExport result = mapper.toWidgetExport(widget, atributsVisualsHelper);

        // Then
        assertNotNull(result);
        assertInstanceOf(EstadisticaTaulaWidgetExport.class, result);
        EstadisticaTaulaWidgetExport taulaExport = (EstadisticaTaulaWidgetExport) result;
        assertEquals(WidgetTipus.TAULA, taulaExport.getTipus());
        assertEquals(dimensio.getCodi(), taulaExport.getDimensioAgrupacioCodi());
        assertEquals(atributs, taulaExport.getAtributsVisuals());
    }

    @Test
    void testToWidgetExport_Null() {
        // When
        EstadisticaWidgetExport result = mapper.toWidgetExport(null, atributsVisualsHelper);

        // Then
        assertNull(result);
    }

    @Test
    void testToDimensioValorExport_Success() {
        // Given
        DimensioValorEntity entity = new DimensioValorEntity();
        entity.setId(1L);
        entity.setValor("Valor Test");

        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("DIM001");
        entity.setDimensio(dimensio);

        // When
        DimensioValorExport result = mapper.toDimensioValorExport(entity);

        // Then
        assertNotNull(result);
        assertEquals(entity.getValor(), result.getValor());
        assertEquals(dimensio.getCodi(), result.getDimensioCodi());
    }

    @Test
    void testToIndicadorTaulaExport_Success() {
        // Given
        IndicadorTaulaEntity entity = new IndicadorTaulaEntity();
        entity.setId(1L);

        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setCodi("IND001");
        entity.setIndicador(indicador);

        // When
        IndicadorTaulaExport result = mapper.toIndicadorTaulaExport(entity);

        // Then
        assertNotNull(result);
        assertEquals(indicador.getCodi(), result.getIndicadorCodi());
    }

    @Test
    void testToIndicadorExport_Formula() {
        // Given
        IndicadorEntity component = new IndicadorEntity();
        component.setCodi("COMP");

        es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorFormulaTermeEntity terme =
                new es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorFormulaTermeEntity();
        terme.setIndicadorComponent(component);
        terme.setOperador(es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum.SUMA);
        terme.setOrdre(0);

        IndicadorEntity comptadorPerMitjana = new IndicadorEntity();
        comptadorPerMitjana.setCodi("COMPTADOR");

        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setCodi("FORM");
        indicador.setNom("Formula");
        indicador.setEntornAppId(30L);
        indicador.setTipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus.FORMULA);
        indicador.setIndicadorComptadorPerMitjana(comptadorPerMitjana);
        indicador.setFormula(List.of(terme));

        when(estadisticaClientHelper.entornAppFindById(30L)).thenReturn(entornApp);
        entornApp.setEntorn(new es.caib.comanda.client.model.EntornRef(10L, "Entorn"));
        entornApp.setApp(new es.caib.comanda.client.model.AppRef(20L, "App"));

        // When
        IndicadorExport result = mapper.toIndicadorExport(indicador, estadisticaClientHelper);

        // Then
        assertNotNull(result);
        assertEquals("FORM", result.getCodi());
        assertEquals("ENTORN001", result.getEntornCodi());
        assertEquals("APP001", result.getAppCodi());
        assertEquals("COMPTADOR", result.getIndicadorComptadorPerMitjanaCodi());
        assertEquals(1, result.getFormula().size());
        assertEquals("COMP", result.getFormula().get(0).getIndicadorComponentCodi());
    }

    @Test
    void testToIndicadorExport_EntornAppInexistent() {
        // Given
        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setCodi("IND1");
        indicador.setEntornAppId(99L);

        when(estadisticaClientHelper.entornAppFindById(99L)).thenReturn(null);

        // When
        IndicadorExport result = mapper.toIndicadorExport(indicador, estadisticaClientHelper);

        // Then
        assertNotNull(result);
        assertNull(result.getEntornCodi());
        assertNull(result.getAppCodi());
    }

    // ============================================================================
    // TESTS DE CONVERSIÓN INVERSA: Export → Entity
    // ============================================================================

    @Test
    void testToDashboardEntity_Success() {
        // Given
        DashboardExport export = new DashboardExport();
        export.setTitol("Dashboard Test");
        export.setEntornCodi(entorn.getCodi());
        export.setAppCodi(app.getCodi());
        export.setColorFonsClar("#111111");
        export.setColorFonsFosc("#222222");

        PlantillaExport plantillaExport = new PlantillaExport();
        plantillaExport.setNom("Plantilla Test");
        export.setPlantilla(plantillaExport);

        // When
        DashboardEntity result = mapper.toDashboardEntity(export, estadisticaClientHelper, atributsVisualsHelper,
                indicadorRepository, dimensioRepository, dimensioValorRepository);

        // Then
        assertNotNull(result);
        assertEquals(export.getTitol(), result.getTitol());
        assertEquals(entorn.getId(), result.getEntornId());
        assertEquals(app.getId(), result.getAppId());
        assertEquals("Plantilla Test", result.getPlantilla().getNom());
        assertEquals("#111111", result.getColorFonsClar());
        assertEquals("#222222", result.getColorFonsFosc());
    }

    @Test
    void testToDashboardEntity_NullCodi() {
        // Given
        DashboardExport export = new DashboardExport();
        export.setEntornCodi(null);
        export.setAppCodi(null);

        // When
        DashboardEntity result = mapper.toDashboardEntity(export, estadisticaClientHelper, atributsVisualsHelper,
                indicadorRepository, dimensioRepository, dimensioValorRepository);

        // Then
        assertNotNull(result);
        assertNull(result.getEntornId());
        assertNull(result.getAppId());
        verify(estadisticaClientHelper, never()).entornByCodi(anyString());
        verify(estadisticaClientHelper, never()).appFindByCodi(anyString());
    }

    @Test
    void testToDashboardEntity_NotFound() {
        // Given
        DashboardExport export = new DashboardExport();
        export.setEntornCodi("INVALID");
        export.setAppCodi("INVALID");

        when(estadisticaClientHelper.entornByCodi("INVALID")).thenReturn(null);
        when(estadisticaClientHelper.appFindByCodi("INVALID")).thenReturn(null);

        // When
        DashboardEntity result = mapper.toDashboardEntity(export, estadisticaClientHelper, atributsVisualsHelper,
                indicadorRepository, dimensioRepository, dimensioValorRepository);

        // Then
        assertNotNull(result);
        assertNull(result.getEntornId());
        assertNull(result.getAppId());
    }

    @Test
    void testToWidgetEntity_SimpleWidget() {
        // Given
        DashboardItemExport itemExport = new DashboardItemExport();
        itemExport.setEntornCodi(entorn.getCodi());
        itemExport.setAppCodi(app.getCodi());

        EstadisticaSimpleWidgetExport widgetExport = new EstadisticaSimpleWidgetExport();
        AtributsVisualsSimple atributs = new AtributsVisualsSimple();
        widgetExport.setAtributsVisuals(atributs);
        itemExport.setWidget(widgetExport);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributs)).thenReturn("{\"test\":true}");

        // When
        EstadisticaWidgetEntity result = mapper.toWidgetEntity(itemExport, estadisticaClientHelper, atributsVisualsHelper,
                indicadorRepository, dimensioRepository, dimensioValorRepository);

        // Then
        assertNotNull(result);
        assertInstanceOf(EstadisticaSimpleWidgetEntity.class, result);
        assertEquals(app.getId(), result.getAppId());
        assertEquals("{\"test\":true}", result.getAtributsVisualsJson());
    }

    @Test
    void testToWidgetEntity_GraficWidget() {
        // Given
        DashboardItemExport itemExport = new DashboardItemExport();
        itemExport.setEntornCodi(entorn.getCodi());
        itemExport.setAppCodi(app.getCodi());

        EstadisticaGraficWidgetExport widgetExport = new EstadisticaGraficWidgetExport();
        widgetExport.setDescomposicioDimensioCodi(dimensio.getCodi());
        AtributsVisualsGrafic atributs = new AtributsVisualsGrafic();
        widgetExport.setAtributsVisuals(atributs);
        itemExport.setWidget(widgetExport);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributs)).thenReturn("{\"grafic\":true}");

        // When
        EstadisticaWidgetEntity result = mapper.toWidgetEntity(itemExport, estadisticaClientHelper, atributsVisualsHelper,
                indicadorRepository, dimensioRepository, dimensioValorRepository);

        // Then
        assertNotNull(result);
        assertInstanceOf(EstadisticaGraficWidgetEntity.class, result);
        EstadisticaGraficWidgetEntity graficEntity = (EstadisticaGraficWidgetEntity) result;
        assertEquals(app.getId(), graficEntity.getAppId());
        assertEquals(dimensio, graficEntity.getDescomposicioDimensio());
        assertEquals("{\"grafic\":true}", graficEntity.getAtributsVisualsJson());
    }

    @Test
    void testToWidgetEntity_TaulaWidget() {
        // Given
        DashboardItemExport itemExport = new DashboardItemExport();
        itemExport.setEntornCodi(entorn.getCodi());
        itemExport.setAppCodi(app.getCodi());

        EstadisticaTaulaWidgetExport widgetExport = new EstadisticaTaulaWidgetExport();
        widgetExport.setDimensioAgrupacioCodi(dimensio.getCodi());
        AtributsVisualsTaula atributs = new AtributsVisualsTaula();
        widgetExport.setAtributsVisuals(atributs);
        itemExport.setWidget(widgetExport);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributs)).thenReturn("{\"taula\":true}");

        // When
        EstadisticaWidgetEntity result = mapper.toWidgetEntity(itemExport, estadisticaClientHelper, atributsVisualsHelper,
                indicadorRepository, dimensioRepository, dimensioValorRepository);

        // Then
        assertNotNull(result);
        assertInstanceOf(EstadisticaTaulaWidgetEntity.class, result);
        EstadisticaTaulaWidgetEntity taulaEntity = (EstadisticaTaulaWidgetEntity) result;
        assertEquals(app.getId(), taulaEntity.getAppId());
        assertEquals(dimensio, taulaEntity.getDimensioAgrupacio());
        assertEquals("{\"taula\":true}", taulaEntity.getAtributsVisualsJson());
    }

    @Test
    void testToWidgetEntity_Null() {
        // When
        EstadisticaWidgetEntity result = mapper.toWidgetEntity(null, estadisticaClientHelper, atributsVisualsHelper,
                indicadorRepository, dimensioRepository, dimensioValorRepository);

        // Then
        assertNull(result);
    }

    @Test
    void testToDimensioEntity_Success() {
        // When
        DimensioEntity result = mapper.toDimensioEntity(dimensio.getCodi(), entornApp.getId(), dimensioRepository);

        // Then
        assertNotNull(result);
        assertEquals(dimensio, result);
    }

    @Test
    void testToDimensioEntity_NullCodi() {
        // When
        DimensioEntity result = mapper.toDimensioEntity(null, 30L, dimensioRepository);

        // Then
        assertNull(result);
        verify(dimensioRepository, never()).findByCodiAndEntornAppId(anyString(), anyLong());
    }

    @Test
    void testToDimensioEntity_NotFound() {
        // Given
        when(dimensioRepository.findByCodiAndEntornAppId("INVALID", 30L)).thenReturn(Optional.empty());

        // When
        DimensioEntity result = mapper.toDimensioEntity("INVALID", 30L, dimensioRepository);

        // Then
        assertNull(result);
    }

    @Test
    void testToIndicadorEntity_Success() {
        // Given
        IndicadorTaulaExport export = new IndicadorTaulaExport();
        export.setIndicadorCodi("IND001");

        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setCodi("IND001");

        when(indicadorRepository.findByCodiAndEntornAppId("IND001", 30L)).thenReturn(Optional.of(indicador));

        // When
        IndicadorEntity result = mapper.toIndicadorEntity(export, 30L, indicadorRepository);

        // Then
        assertNotNull(result);
        assertEquals(indicador, result);
    }

    @Test
    void testToIndicadorEntity_Null() {
        // When
        IndicadorEntity result = mapper.toIndicadorEntity(null, 30L, indicadorRepository);

        // Then
        assertNull(result);
    }

    @Test
    void testToDimensioValorEntity_Success() {
        // Given
        DimensioValorExport export = new DimensioValorExport();
        export.setDimensioCodi(dimensio.getCodi());
        export.setValor("Valor Test");

        DimensioValorEntity valor = new DimensioValorEntity();
        valor.setValor("Valor Test");

        when(dimensioValorRepository.findByDimensioAndValor(dimensio, export.getValor())).thenReturn(Optional.of(valor));

        // When
        DimensioValorEntity result = mapper.toDimensioValorEntity(export, 30L, dimensioRepository, dimensioValorRepository);

        // Then
        assertNotNull(result);
        assertEquals("Valor Test", result.getValor());
    }

    @Test
    void testToDimensioValorEntity_DimensioNotFound() {
        // Given
        DimensioValorExport export = new DimensioValorExport();
        export.setDimensioCodi("INVALID");
        export.setValor("Valor Test");

        when(dimensioRepository.findByCodiAndEntornAppId("INVALID", 30L)).thenReturn(Optional.empty());

        // When
        DimensioValorEntity result = mapper.toDimensioValorEntity(export, 30L, dimensioRepository, dimensioValorRepository);

        // Then
        assertNull(result);
        verify(dimensioValorRepository, never()).findByDimensioAndValor(any(), anyString());
    }
}
