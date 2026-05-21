package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.PaletaHelper;
import es.caib.comanda.estadistica.logic.intf.model.paleta.*;
import es.caib.comanda.estadistica.persist.entity.paleta.*;
import es.caib.comanda.estadistica.persist.repository.DashboardTemplatePaletteGroupRepository;
import es.caib.comanda.estadistica.persist.repository.PaletaRepository;
import es.caib.comanda.estadistica.persist.repository.WidgetStylePropertyRepository;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a PlantillaServiceImpl")
class PaletaServiceImplTest {

    @Mock private PaletaRepository paletaRepository;
    @Mock private PaletaHelper paletaHelper;
    @Mock private DashboardTemplatePaletteGroupRepository paletteGroupRepository;
    @Mock private WidgetStylePropertyRepository stylePropertyRepository;

    @InjectMocks
    private PlantillaServiceImpl plantillaService;

    @Captor
    private ArgumentCaptor<PaletaEntity> paletaEntityCaptor;

    private Plantilla plantillaResource;
    private PlantillaEntity plantillaEntity;
    private Map<String, AnswerRequiredException.AnswerValue> answers;

    @BeforeEach
    void setUp() {
        plantillaResource = new Plantilla();
        plantillaResource.setNom("Plantilla Test");

        plantillaEntity = new PlantillaEntity();
        plantillaEntity.setId(2L);
        plantillaEntity.setNom("Plantilla Test");

        answers = new HashMap<>();
    }

    @Test
    @DisplayName("beforeCreateSave: crida syncTemplate i no llança excepció")
    void beforeCreateSave_quanTotCorrecte_noLlancaExcepcio() {
        // Arrange
        Paleta paletteResource = crearPaletaResource("910001", "Test Widget");
        plantillaResource.setPaletes(Collections.singletonList(paletteResource));
        PaletaEntity paletteEntity = crearPaletaEntity(910001L, "Test Widget");

        when(paletaRepository.findById(910001L)).thenReturn(Optional.of(paletteEntity));
        when(paletaRepository.saveAndFlush(paletteEntity)).thenReturn(paletteEntity);
        doNothing().when(paletaHelper).syncColors(any(), any());

        // Act & Assert
        assertThatCode(() ->
                plantillaService.beforeCreateSave(plantillaEntity, plantillaResource, answers)
        ).doesNotThrowAnyException();

        verify(paletaHelper).syncColors(eq(paletteEntity), eq(paletteResource));
    }

    @Test
    @DisplayName("beforeCreateSave: captura excepció i la converteix a ResourceNotCreatedException")
    void beforeCreateSave_quanSyncTemplateLlancaExcepcio_converteixExcepcio() {
        // Arrange
        doThrow(new RuntimeException("Error de sincronització"))
                .when(paletaHelper).syncColors(any(), any());

        // Act & Assert
        assertThatThrownBy(() ->
                plantillaService.beforeCreateSave(plantillaEntity, plantillaResource, answers)
        ).isInstanceOf(ResourceNotCreatedException.class)
                .hasMessageContaining("Error de sincronització");
    }

    @Test
    @DisplayName("beforeUpdateSave: crida syncTemplate i no llança excepció")
    void beforeUpdateSave_quanTotCorrecte_noLlancaExcepcio() {
        // Arrange
        Paleta paletteResource = crearPaletaResource("910001", "Test Widget");
        plantillaResource.setPaletes(Collections.singletonList(paletteResource));
        PaletaEntity paletteEntity = crearPaletaEntity(910001L, "Test Widget");

        when(paletaRepository.findById(910001L)).thenReturn(Optional.of(paletteEntity));
        when(paletaRepository.saveAndFlush(paletteEntity)).thenReturn(paletteEntity);
        doNothing().when(paletaHelper).syncColors(any(), any());

        // Act & Assert
        assertThatCode(() ->
                plantillaService.beforeUpdateSave(plantillaEntity, plantillaResource, answers)
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("beforeUpdateSave: captura excepció i la converteix a ResourceNotUpdatedException")
    void beforeUpdateSave_quanSyncTemplateLlancaExcepcio_converteixExcepcio() {
        // Arrange
        doThrow(new RuntimeException("Error en actualització")).when(paletaHelper).syncColors(any(), any());

        // Act & Assert
        assertThatThrownBy(() ->
                plantillaService.beforeUpdateSave(plantillaEntity, plantillaResource, answers)
        ).isInstanceOf(ResourceNotUpdatedException.class)
                .hasMessageContaining("Error en actualització")
                .hasMessageContaining(String.valueOf(plantillaEntity.getId()));
    }

    @Test
    @DisplayName("afterConversion: popula paletteGroups, styleProperties i paletes per defecte")
    void afterConversion_quanNoHiHaDadesExistentes_populaValorsPerDefecte() {
        // Arrange
        when(paletaRepository.findAllByOrderByNomAscIdAsc()).thenReturn(Collections.emptyList());

        // Act
        plantillaService.afterConversion(plantillaEntity, plantillaResource);

        // Assert
        assertThat(plantillaResource.getPaletes()).isNotEmpty();
        assertThat(plantillaResource.getPaletteGroups()).isNotEmpty();
        assertThat(plantillaResource.getStyleProperties()).isNotEmpty();
    }

    @Test
    @DisplayName("afterConversion: utilitza paletes existents quan n'hi ha")
    void afterConversion_quanHiHaPaletesExistentes_lesUtilitza() {
        // Arrange
        PaletaEntity paletaExistente = new PaletaEntity();
        paletaExistente.setId(910001L);
        paletaExistente.setNom("Paleta Test");
        when(paletaRepository.findAllByOrderByNomAscIdAsc()).thenReturn(Collections.singletonList(paletaExistente));

        // Act
        plantillaService.afterConversion(plantillaEntity, plantillaResource);

        // Assert
        assertThat(plantillaResource.getPaletes())
                .extracting(Paleta::getNom)
                .containsExactly("Paleta Test");
    }

    @Test
    @DisplayName("savePaletteResources: actualitza paleta existent quan arriba amb clientId numèric")
    void savePaletteResources_quanClientIdNumeric_actualitzaPaletaExistent() {
        // Arrange
        Paleta palette = crearPaleta("910001", "Institucional clar widget", "#ffffff");
        PaletaEntity existing = crearPaletaEntity(910001L, "Institucional clar widget");

        when(paletaRepository.findById(910001L)).thenReturn(Optional.of(existing));
        when(paletaRepository.saveAndFlush(existing)).thenReturn(existing);

        // Act
        Map<String, PaletaEntity> result = invokeSavePaletteResources(Collections.singletonList(palette));

        // Assert
        assertThat(result).containsEntry("910001", existing);
        assertThat(existing.getColors()).isNotNull();
        verify(paletaHelper).syncColors(eq(existing), eq(palette));
        verify(paletaRepository).saveAndFlush(existing);
    }

    @Test
    @DisplayName("savePaletteResources: crea nova paleta quan no existeix ni per ID ni per nom")
    void savePaletteResources_quanNoExisteixPaleta_creaNova() {
        // Arrange
        Paleta palette = crearPaleta(null, "Nova Paleta", "#000000");
        PaletaEntity nueva = new PaletaEntity();
        nueva.setId(999999L);

        when(paletaRepository.findByNom("Nova Paleta")).thenReturn(Optional.empty());
        when(paletaRepository.saveAndFlush(any(PaletaEntity.class))).thenReturn(nueva);

        // Act
        Map<String, PaletaEntity> result = invokeSavePaletteResources(Collections.singletonList(palette));

        // Assert
        assertThat(result).hasSize(2);
        verify(paletaHelper).syncColors(any(PaletaEntity.class), eq(palette));
        verify(paletaRepository).saveAndFlush(any(PaletaEntity.class));
    }

    @Test
    @DisplayName("validateTemplate: llança excepció si no hi ha 4 grups de paletes")
    void validateTemplate_quanMenysDe4Grups_llancaExcepcio() {
        // Arrange
        PlantillaEntity entity = new PlantillaEntity();
        entity.setPaletteGroups(new ArrayList<>());
        entity.getPaletteGroups().add(new PlantillaGrupPaletesEntity());
        entity.getPaletteGroups().add(new PlantillaGrupPaletesEntity());

        // Act & Assert
        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(plantillaService, "validateTemplate", entity)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactament quatre grups de paletes");
    }

    @Test
    @DisplayName("validateTemplate: llança excepció si una paleta no té colors")
    void validateTemplate_quanPaletaSenseColors_llancaExcepcio() {
        // Arrange
        PlantillaEntity entity = new PlantillaEntity();
        List<PlantillaGrupPaletesEntity> groups = new ArrayList<>();

        PlantillaGrupPaletesEntity group = new PlantillaGrupPaletesEntity();
        group.setGroupType(PaletteGroupType.LIGHT);

        PaletaEntity palette = new PaletaEntity();
        palette.setColors(new ArrayList<>());
        group.setWidgetPalette(palette);
        group.setChartPalette(palette);
        groups.add(group);

        for (int i = 1; i < PaletteGroupType.values().length; i++) {
            PlantillaGrupPaletesEntity g = new PlantillaGrupPaletesEntity();
            g.setGroupType(PaletteGroupType.values()[i]);
            g.setWidgetPalette(palette);
            g.setChartPalette(palette);
            groups.add(g);
        }
        entity.setPaletteGroups(groups);

        // Act & Assert
        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(plantillaService, "validateTemplate", entity)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no poden estar buides");
    }

    @Test
    @DisplayName("validateTemplate: llança excepció si propietat de color apunta a posició inexistent")
    void validateTemplate_quanPropietatColorPosicioInvalida_llancaExcepcio() {
        // Arrange
        PlantillaEntity entity = new PlantillaEntity();
        PaletaEntity palette = crearPaletaEntity(1L, "Test");
        palette.getColors().remove(6);
        palette.getColors().remove(5);

        PlantillaGrupPaletesEntity group = new PlantillaGrupPaletesEntity();
        group.setGroupType(PaletteGroupType.LIGHT);
        group.setWidgetPalette(palette);
        group.setChartPalette(palette);

        List<PlantillaGrupPaletesEntity> groups = new ArrayList<>();
        for (PaletteGroupType type : PaletteGroupType.values()) {
            PlantillaGrupPaletesEntity g = new PlantillaGrupPaletesEntity();
            g.setGroupType(type);
            g.setWidgetPalette(palette);
            g.setChartPalette(palette);
            groups.add(g);
        }
        entity.setPaletteGroups(groups);

        WidgetStylePropertyEntity property = new WidgetStylePropertyEntity();
        property.setValueType(WidgetStyleValueType.COLOR);
        property.setPaletteRole(PaletteRole.WIDGET);
        property.setPaletteIndex(5);
        property.setPropertyName("colorTest");
        entity.setStyleProperties(Collections.singletonList(property));

        // Act & Assert
        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(plantillaService, "validateTemplate", entity)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posicio inexistent")
                .hasMessageContaining("colorTest");
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PER A TESTS
    // ─────────────────────────────────────────────────────────────

    private Paleta crearPaleta(String clientId, String nom, String color) {
        Paleta palette = new Paleta();
        palette.setClientId(clientId);
        palette.setNom(nom);

        PaletaColor colorEntity = new PaletaColor();
        colorEntity.setPosicio(0);
        colorEntity.setValor(color);
        palette.setColors(Collections.singletonList(colorEntity));

        return palette;
    }

    private PaletaEntity crearPaletaEntity(Long id, String nom) {
        PaletaEntity entity = new PaletaEntity();
        entity.setId(id);
        entity.setNom(nom);

        List<PaletaColorEntity> colors = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            PaletaColorEntity color = new PaletaColorEntity();
            color.setPaleta(entity);
            color.setPosicio(i);
            color.setValor(String.format("#%06d", i * 111111));
            colors.add(color);
        }
        entity.setColors(colors);

        return entity;
    }

    private Paleta crearPaletaResource(String clientId, String nom) {
        Paleta palette = new Paleta();
        palette.setClientId(clientId);
        palette.setNom(nom);

        List<PaletaColor> colors = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            PaletaColor color = new PaletaColor();
            color.setPosicio(i);
            color.setValor(String.format("#%06d", i * 111111));
            colors.add(color);
        }
        palette.setColors(colors);

        return palette;
    }

    /** Invoca el mètode privat savePaletteResources via reflexió. */
    @SuppressWarnings("unchecked")
    private Map<String, PaletaEntity> invokeSavePaletteResources(List<Paleta> palettes) {
        try {
            java.lang.reflect.Method method = PlantillaServiceImpl.class
                    .getDeclaredMethod("savePaletteResources", List.class);
            method.setAccessible(true);
            return (Map<String, PaletaEntity>) method.invoke(plantillaService, palettes);
        } catch (Exception e) {
            throw new RuntimeException("Error invocant savePaletteResources", e);
        }
    }

    /** Invoca el mètode privat validateTemplate via reflexió per a tests. */
    private void invokeValidateTemplate(PlantillaEntity entity) {
        try {
            java.lang.reflect.Method method = PlantillaServiceImpl.class
                    .getDeclaredMethod("validateTemplate", PlantillaEntity.class);
            method.setAccessible(true);
            method.invoke(plantillaService, entity);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException("Error invocant validateTemplate", e);
        }
    }
}