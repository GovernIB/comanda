package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletaColor;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaColorEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.repository.PaletaColorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a PaletaHelper")
class PaletaHelperTest {

    @Mock private PaletaColorRepository paletaColorRepository;

    @InjectMocks
    private PaletaHelper paletaHelper;

    @Captor
    private ArgumentCaptor<List<PaletaColorEntity>> colorsCaptor;

    private PaletaEntity paletaEntity;
    private Paleta paletaResource;

    private static final Long PALETA_ID = 910001L;
    private static final String PALETA_NOM = "Test Paleta";

    @BeforeEach
    void setUp() {
        paletaEntity = new PaletaEntity();
        paletaEntity.setId(PALETA_ID);
        paletaEntity.setNom(PALETA_NOM);

        paletaResource = new Paleta();
        paletaResource.setNom(PALETA_NOM);
    }

    @Test
    @DisplayName("syncColors: crea nous colors quan no n'hi ha d'existentes")
    void syncColors_quanNoHiHaColorsExistentes_creaNous() {
        // Arrange
        paletaResource.setColors(Arrays.asList(
                crearPaletaColorResource(0, "#FFFFFF"),
                crearPaletaColorResource(1, "#000000"),
                crearPaletaColorResource(2, "#FF0000")
        ));
        when(paletaColorRepository.findByPaletaId(PALETA_ID)).thenReturn(Collections.emptyList());

        // Act
        paletaHelper.syncColors(paletaEntity, paletaResource);

        // Assert
        verify(paletaColorRepository, never()).deleteAll(any());
        verify(paletaColorRepository).saveAll(colorsCaptor.capture());

        List<PaletaColorEntity> saved = colorsCaptor.getValue();
        assertThat(saved).hasSize(3);
        assertThat(saved).extracting(PaletaColorEntity::getPosicio).containsExactly(0, 1, 2);
        assertThat(saved).extracting(PaletaColorEntity::getValor).containsExactly("#FFFFFF", "#000000", "#FF0000");
        assertThat(paletaEntity.getColors()).isEqualTo(saved);
    }

    @Test
    @DisplayName("syncColors: actualitza colors existents quan coincideixen per posicio")
    void syncColors_quanColorsExistentes_actualitzaValors() {
        // Arrange
        List<PaletaColorEntity> existing = Arrays.asList(
                crearPaletaColorEntity(1L, 0, "#OLD0"),
                crearPaletaColorEntity(2L, 1, "#OLD1"),
                crearPaletaColorEntity(3L, 2, "#OLD2")
        );
        when(paletaColorRepository.findByPaletaId(PALETA_ID)).thenReturn(existing);

        paletaResource.setColors(Arrays.asList(
                crearPaletaColorResource(0, "#NEW0"),
                crearPaletaColorResource(1, "#NEW1"),
                crearPaletaColorResource(2, "#NEW2")
        ));

        // Act
        paletaHelper.syncColors(paletaEntity, paletaResource);

        // Assert
        verify(paletaColorRepository, never()).deleteAll(any());
        verify(paletaColorRepository).saveAll(colorsCaptor.capture());

        List<PaletaColorEntity> saved = colorsCaptor.getValue();
        assertThat(saved).hasSize(3);
        assertThat(saved).extracting(PaletaColorEntity::getId).containsExactly(1L, 2L, 3L);
        assertThat(saved).extracting(PaletaColorEntity::getValor).containsExactly("#NEW0", "#NEW1", "#NEW2");
    }

    @Test
    @DisplayName("syncColors: elimina colors que ja no estan al resource")
    void syncColors_quanColorsOrphans_eliminaExistentes() {
        // Arrange
        List<PaletaColorEntity> existing = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            existing.add(crearPaletaColorEntity((long) i, i, "#OLD" + i));
        }
        when(paletaColorRepository.findByPaletaId(PALETA_ID)).thenReturn(existing);

        paletaResource.setColors(Arrays.asList(
                crearPaletaColorResource(0, "#KEEP1"),
                crearPaletaColorResource(1, "#KEEP3")
        ));

        // Act
        paletaHelper.syncColors(paletaEntity, paletaResource);

        // Assert
        verify(paletaColorRepository).deleteAll(colorsCaptor.capture());
        List<PaletaColorEntity> deleted = colorsCaptor.getValue();
        assertThat(deleted).extracting(PaletaColorEntity::getPosicio).containsExactly(2, 3, 4);
        verify(paletaColorRepository).saveAll(any());
    }

    @ParameterizedTest
    @MethodSource("proporcionarCasosNormalitzacio")
    @DisplayName("syncColors: normalitza posicions correctament")
    void syncColors_normalitzaPosicions(String descripcio, List<PaletaColor> inputColors, List<Integer> posicionsEsperades) {
        // Arrange
        paletaResource.setColors(inputColors);
        when(paletaColorRepository.findByPaletaId(PALETA_ID)).thenReturn(Collections.emptyList());

        // Act
        paletaHelper.syncColors(paletaEntity, paletaResource);

        // Assert
        verify(paletaColorRepository).saveAll(colorsCaptor.capture());
        List<PaletaColorEntity> saved = colorsCaptor.getValue();
        assertThat(saved).extracting(PaletaColorEntity::getPosicio).containsExactlyElementsOf(posicionsEsperades);
    }

    private static Stream<Arguments> proporcionarCasosNormalitzacio() {
        return Stream.of(
                Arguments.of(
                        "Posicions desordenades → es reordenen [0,1,2]",
                        Arrays.asList(
                                crearPaletaColorResource(2, "#C"),
                                crearPaletaColorResource(0, "#A"),
                                crearPaletaColorResource(1, "#B")
                        ),
                        Arrays.asList(0, 1, 2)
                ),
                Arguments.of(
                        "Posicions amb nulls al final → es reassignen",
                        Arrays.asList(
                                crearPaletaColorResource(0, "#A"),
                                crearPaletaColorResource(null, "#B"),
                                crearPaletaColorResource(null, "#C")
                        ),
                        Arrays.asList(0, 1, 2)
                ),
                Arguments.of(
                        "Colors amb valor null es filtren",
                        Arrays.asList(
                                crearPaletaColorResource(0, "#A"),
                                crearPaletaColorResource(1, null),
                                crearPaletaColorResource(2, "#B")
                        ),
                        Arrays.asList(0, 1)
                )
        );
    }

    @Test
    @DisplayName("paletaEntitytoColorResources: converteix entitat a recursos ordenats per posicio")
    void paletaEntitytoColorResources_quanHiHaColors_retornaLlistaOrdenada() {
        // Arrange
        List<PaletaColorEntity> colorsEntity = Arrays.asList(
                crearPaletaColorEntity(1L, 2, "#C"),
                crearPaletaColorEntity(2L, 0, "#A"),
                crearPaletaColorEntity(3L, 1, "#B")
        );
        when(paletaColorRepository.findByPaletaIdOrderByPosicioAsc(PALETA_ID))
                .thenReturn(colorsEntity);

        // Act
        List<PaletaColor> result = paletaHelper.paletaEntitytoColorResources(paletaEntity);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).extracting(PaletaColor::getPosicio).containsExactly(0, 1, 2);
        assertThat(result).extracting(PaletaColor::getValor).containsExactly("#A", "#B", "#C");
        assertThat(result.get(0).getPaleta()).isNotNull();
        assertThat(result.get(0).getPaleta().getId()).isEqualTo(PALETA_ID);
    }

    @Test
    @DisplayName("paletaEntitytoColorResources: retorna llista buida quan l'entitat no té colors")
    void paletaEntitytoColorResources_quanNoHiHaColors_retornaLlistaBuida() {
        // Arrange
        when(paletaColorRepository.findByPaletaIdOrderByPosicioAsc(PALETA_ID)).thenReturn(null);

        // Act
        List<PaletaColor> result = paletaHelper.paletaEntitytoColorResources(paletaEntity);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("normalizePositions: llista null o buida → retorna buida")
    void normalizePositions_quanListaNullOBuida_retornaBuida() {
        // Act & Assert
        assertThat(invokeNormalizePositions(null)).isEmpty();
        assertThat(invokeNormalizePositions(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("normalizePositions: reassigna posicions correlatives [0,1,2...]")
    void normalizePositions_reassignaPosicionsCorrelatives() {
        // Arrange
        List<PaletaColor> input = Arrays.asList(
                crearPaletaColorResource(2, "#C"),
                crearPaletaColorResource(null, "#B"),
                crearPaletaColorResource(0, "#A")
        );

        // Act
        List<PaletaColor> result = invokeNormalizePositions(input);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).extracting(PaletaColor::getPosicio).containsExactly(0, 1, 2);
        assertThat(result).extracting(PaletaColor::getValor).containsExactly("#A", "#C", "#B");
    }

    @Test
    @DisplayName("normalizePositions: filtra colors amb valor null")
    void normalizePositions_filtraColorsSenseValor() {
        // Arrange
        List<PaletaColor> input = Arrays.asList(
                crearPaletaColorResource(0, "#A"),
                crearPaletaColorResource(1, null),
                crearPaletaColorResource(2, "#B")
        );

        // Act
        List<PaletaColor> result = invokeNormalizePositions(input);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(PaletaColor::getValor).containsExactly("#A", "#B");
        assertThat(result).extracting(PaletaColor::getPosicio).containsExactly(0, 1);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PER A TESTS
    // ─────────────────────────────────────────────────────────────

    private static PaletaColor crearPaletaColorResource(Integer posicio, String valor) {
        PaletaColor color = new PaletaColor();
        color.setPosicio(posicio);
        color.setValor(valor);
        return color;
    }

    private PaletaColorEntity crearPaletaColorEntity(Long id, Integer posicio, String valor) {
        PaletaColorEntity color = new PaletaColorEntity();
        color.setId(id);
        color.setPosicio(posicio);
        color.setValor(valor);
        color.setPaleta(paletaEntity);
        return color;
    }

    /** Invoca el mètode privat normalizePositions via reflexió per a tests. */
    @SuppressWarnings("unchecked")
    private List<PaletaColor> invokeNormalizePositions(List<PaletaColor> colors) {
        try {
            java.lang.reflect.Method method = PaletaHelper.class
                    .getDeclaredMethod("normalizePositions", List.class);
            method.setAccessible(true);
            return (List<PaletaColor>) method.invoke(paletaHelper, colors);
        } catch (Exception e) {
            throw new RuntimeException("Error invocant normalizePositions", e);
        }
    }
}