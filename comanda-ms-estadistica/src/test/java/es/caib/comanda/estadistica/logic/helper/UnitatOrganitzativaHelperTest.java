package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.dir3.SistemaExternException;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPlugin;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UOEstatEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a UnitatOrganitzativaHelper")
class UnitatOrganitzativaHelperTest {

    @Mock
    private UnitatsOrganitzativesPlugin unitatsOrganitzativesPlugin;

    @Mock
    private UnitatOrganitzativaRepository unitatOrganitzativaRepository;

    @InjectMocks
    private UnitatOrganitzativaHelper unitatOrganitzativaHelper;

    private UnitatOrganitzativaEntity pluginResponse;

    @BeforeEach
    void setUp() {
        pluginResponse = new UnitatOrganitzativaEntity();
        pluginResponse.setCodi("UO001");
        pluginResponse.setDenominacio("Unitat de Prova");
    }

    // ========================================================================
    // 1. TESTOS PER A updateByCodi
    // ========================================================================

    @Test
    @DisplayName("updateByCodi: actualitza correctament quan no té conselleria associada")
    void updateByCodi_quanNoTeConselleria_llavorsActualitzaDirectament() throws SistemaExternException {
        // Arrange
        pluginResponse.setCodiConselleria(null);
        when(unitatsOrganitzativesPlugin.findUnidad("UO001")).thenReturn(pluginResponse);
        when(unitatOrganitzativaRepository.save(any(UnitatOrganitzativaEntity.class))).thenReturn(pluginResponse);

        // Act
        UnitatOrganitzativaEntity result = unitatOrganitzativaHelper.updateByCodi("UO001");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCodi()).isEqualTo("UO001");
        verify(unitatOrganitzativaRepository, never()).existsByCodi(anyString());
        verify(unitatOrganitzativaRepository, times(1)).save(pluginResponse);
    }

    @Test
    @DisplayName("updateByCodi: crida recursivament quan la conselleria no existeix al repositori")
    void updateByCodi_quanConselleriaNoExisteix_llavorsCridaRecursivament() throws SistemaExternException {
        // Arrange
        pluginResponse.setCodiConselleria("CONS001");

        UnitatOrganitzativaEntity conselleriaResponse = new UnitatOrganitzativaEntity();
        conselleriaResponse.setCodi("CONS001");
        conselleriaResponse.setCodiConselleria(null);

        when(unitatsOrganitzativesPlugin.findUnidad("UO001")).thenReturn(pluginResponse);
        when(unitatsOrganitzativesPlugin.findUnidad("CONS001")).thenReturn(conselleriaResponse);

        // La conselleria NO existeix
        when(unitatOrganitzativaRepository.existsByCodi("CONS001")).thenReturn(false);

        when(unitatOrganitzativaRepository.save(any(UnitatOrganitzativaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UnitatOrganitzativaEntity result = unitatOrganitzativaHelper.updateByCodi("UO001");

        // Assert
        assertThat(result).isNotNull();
        // Verifiquem que es va cridar a buscar la conselleria (recursivitat)
        verify(unitatsOrganitzativesPlugin, times(1)).findUnidad("CONS001");
        verify(unitatOrganitzativaRepository, times(1)).existsByCodi("CONS001");
        // S'han de guardar 2 entitats: la conselleria i la UO original
        verify(unitatOrganitzativaRepository, times(2)).save(any(UnitatOrganitzativaEntity.class));
    }

    @Test
    @DisplayName("updateByCodi: no crida recursivament quan la conselleria ja existeix al repositori")
    void updateByCodi_quanConselleriaJaExisteix_llavorsNoCridaRecursivament() throws SistemaExternException {
        // Arrange
        pluginResponse.setCodiConselleria("CONS001");
        when(unitatsOrganitzativesPlugin.findUnidad("UO001")).thenReturn(pluginResponse);
        when(unitatOrganitzativaRepository.existsByCodi("CONS001")).thenReturn(true);
        when(unitatOrganitzativaRepository.save(any(UnitatOrganitzativaEntity.class))).thenReturn(pluginResponse);

        // Act
        UnitatOrganitzativaEntity result = unitatOrganitzativaHelper.updateByCodi("UO001");

        // Assert
        assertThat(result).isNotNull();
        verify(unitatsOrganitzativesPlugin, never()).findUnidad("CONS001");
        verify(unitatOrganitzativaRepository, times(1)).existsByCodi("CONS001");
        verify(unitatOrganitzativaRepository, times(1)).save(pluginResponse);
    }

    // ========================================================================
    // 2. TESTOS PER A update
    // ========================================================================

    @Test
    @DisplayName("update: actualitza l'entitat existent quan ja es troba al repositori")
    void update_quanEntitatExisteix_llavorsActualitzaIGuarda() {
        // Arrange
        UnitatOrganitzativaEntity input = new UnitatOrganitzativaEntity();
        input.setCodi("UO001");
        input.setDenominacio("Nou Nom");

        // Usem spy per poder verificar que es crida al mètode update() de l'entitat
        UnitatOrganitzativaEntity existing = new UnitatOrganitzativaEntity();
        existing.setCodi("UO001");
        existing.setDenominacio("Nom Antic");
        UnitatOrganitzativaEntity spiedExisting = org.mockito.Mockito.spy(existing);

        when(unitatOrganitzativaRepository.findByCodi("UO001")).thenReturn(Optional.of(spiedExisting));
        when(unitatOrganitzativaRepository.save(spiedExisting)).thenReturn(spiedExisting);

        // Act
        UnitatOrganitzativaEntity result = unitatOrganitzativaHelper.update(input);

        // Assert
        assertThat(result).isSameAs(spiedExisting);
        // Verifiquem que es va cridar al mètode update de l'entitat existent
        verify(spiedExisting, times(1)).update(input);
        verify(unitatOrganitzativaRepository, times(1)).save(spiedExisting);
    }

    @Test
    @DisplayName("update: crea una entitat nova quan no es troba al repositori")
    void update_quanEntitatNoExisteix_llavorsCreaNova() {
        // Arrange
        UnitatOrganitzativaEntity input = new UnitatOrganitzativaEntity();
        input.setCodi("UO002");
        input.setDenominacio("Nova Unitat");

        when(unitatOrganitzativaRepository.findByCodi("UO002")).thenReturn(Optional.empty());
        when(unitatOrganitzativaRepository.save(input)).thenReturn(input);

        // Act
        UnitatOrganitzativaEntity result = unitatOrganitzativaHelper.update(input);

        // Assert
        assertThat(result).isSameAs(input);
        verify(unitatOrganitzativaRepository, times(1)).save(input);
    }

    // ========================================================================
    // 3. TESTOS PER A updateAll
    // ========================================================================

    @Test
    @DisplayName("updateAll: retorna llista buida quan l'entrada és null")
    void updateAll_quanLlistaEsNull_llavorsRetornaBuida() {
        // Act
        List<UnitatOrganitzativaEntity> result = unitatOrganitzativaHelper.updateAll(null);

        // Assert
        assertThat(result).isEmpty();
        verify(unitatOrganitzativaRepository, never()).findByCodiIn(anyList());
    }

    @Test
    @DisplayName("updateAll: retorna llista buida quan l'entrada és una llista buida")
    void updateAll_quanLlistaEsBuida_llavorsRetornaBuida() {
        // Act
        List<UnitatOrganitzativaEntity> result = unitatOrganitzativaHelper.updateAll(Collections.emptyList());

        // Assert
        assertThat(result).isEmpty();
        verify(unitatOrganitzativaRepository, never()).findByCodiIn(anyList());
    }

    @Test
    @DisplayName("updateAll: filtra correctament els codis nuls de la llista d'entrada")
    void updateAll_quanHiHaCodiNull_llavorsFiltraCorrectament() {
        // Arrange
        UnitatOrganitzativaEntity uo1 = new UnitatOrganitzativaEntity();
        uo1.setCodi("UO001");
        UnitatOrganitzativaEntity uo2 = new UnitatOrganitzativaEntity();
        uo2.setCodi(null); // Aquest s'ha de filtrar

        when(unitatOrganitzativaRepository.findByCodiIn(anyList())).thenReturn(Collections.emptyList());
        when(unitatOrganitzativaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<UnitatOrganitzativaEntity> result = unitatOrganitzativaHelper.updateAll(Arrays.asList(uo1, uo2));

        // Assert
        ArgumentCaptor<List<String>> codisCaptor = ArgumentCaptor.forClass(List.class);
        verify(unitatOrganitzativaRepository).findByCodiIn(codisCaptor.capture());

        // Només "UO001" hauria d'haver passat el filtre
        assertThat(codisCaptor.getValue()).containsExactly("UO001");
        assertThat(result).hasSize(2); // Les dues es guarden, però la cerca és només per la vàlida
    }

    @Test
    @DisplayName("updateAll: actualitza les entitats que ja existeixen al repositori")
    void updateAll_quanEntitatsExisteixen_llavorsActualitzaTarget() {
        // Arrange
        UnitatOrganitzativaEntity input = new UnitatOrganitzativaEntity();
        input.setCodi("UO001");
        input.setDenominacio("Nou Nom");

        UnitatOrganitzativaEntity existing = new UnitatOrganitzativaEntity();
        existing.setCodi("UO001");
        existing.setDenominacio("Nom Antic");
        UnitatOrganitzativaEntity spiedExisting = org.mockito.Mockito.spy(existing);

        when(unitatOrganitzativaRepository.findByCodiIn(Arrays.asList("UO001"))).thenReturn(Arrays.asList(spiedExisting));
        when(unitatOrganitzativaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<UnitatOrganitzativaEntity> result = unitatOrganitzativaHelper.updateAll(Arrays.asList(input));

        // Assert
        verify(spiedExisting, times(1)).update(input);
        assertThat(result.get(0)).isSameAs(spiedExisting);
    }

    @Test
    @DisplayName("updateAll: assigna estat T per defecte quan l'entitat nova no en té")
    void updateAll_quanEntitatNovaSenseEstat_llavorsAssignaEstatT() {
        // Arrange
        UnitatOrganitzativaEntity input = new UnitatOrganitzativaEntity();
        input.setCodi("UO002");
        input.setEstat(null); // Estat null

        when(unitatOrganitzativaRepository.findByCodiIn(Arrays.asList("UO002"))).thenReturn(Collections.emptyList());
        when(unitatOrganitzativaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<UnitatOrganitzativaEntity> result = unitatOrganitzativaHelper.updateAll(Arrays.asList(input));

        // Assert
        assertThat(result.get(0).getEstat()).isEqualTo(UOEstatEnum.T);
    }

    @Test
    @DisplayName("updateAll: respecta l'estat existent quan l'entitat nova ja en té un")
    void updateAll_quanEntitatNovaTeEstat_llavorsRespectaEstat() {
        // Arrange
        UnitatOrganitzativaEntity input = new UnitatOrganitzativaEntity();
        input.setCodi("UO002");
        input.setEstat(UOEstatEnum.A);

        when(unitatOrganitzativaRepository.findByCodiIn(Arrays.asList("UO002"))).thenReturn(Collections.emptyList());
        when(unitatOrganitzativaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<UnitatOrganitzativaEntity> result = unitatOrganitzativaHelper.updateAll(Arrays.asList(input));

        // Assert
        assertThat(result.get(0).getEstat()).isEqualTo(UOEstatEnum.A);
    }

    @Test
    @DisplayName("updateAll: elimina duplicats de codis abans de consultar al repositori")
    void updateAll_quanHiHaDuplicats_llavorsConsultaUnSolCop() {
        // Arrange
        UnitatOrganitzativaEntity uo1 = new UnitatOrganitzativaEntity();
        uo1.setCodi("UO001");
        UnitatOrganitzativaEntity uo2 = new UnitatOrganitzativaEntity();
        uo2.setCodi("UO001"); // Duplicat

        when(unitatOrganitzativaRepository.findByCodiIn(anyList())).thenReturn(Collections.emptyList());
        when(unitatOrganitzativaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        unitatOrganitzativaHelper.updateAll(Arrays.asList(uo1, uo2));

        // Assert
        ArgumentCaptor<List<String>> codisCaptor = ArgumentCaptor.forClass(List.class);
        verify(unitatOrganitzativaRepository).findByCodiIn(codisCaptor.capture());

        // La llista de codis ha de tenir només 1 element gràcies a .distinct()
        assertThat(codisCaptor.getValue()).hasSize(1);
        assertThat(codisCaptor.getValue().get(0)).isEqualTo("UO001");
    }
}
