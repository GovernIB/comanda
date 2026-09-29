package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a OrganitzativaTreeHelper")
class OrganitzativaTreeHelperTest {

    @Mock
    private UnitatOrganitzativaRepository unitatOrganitzativaRepository;

    @InjectMocks
    private OrganitzativaTreeHelper organitzativaTreeHelper;

    @Test
    @DisplayName("getIndexFills: retorna mapa buit quan codiUnitatArrel és null")
    void getIndexFills_quanCodiArrelNull_retornaMapaBuit() {
        Map<String, List<String>> result = organitzativaTreeHelper.getIndexFills(null);
        assertThat(result).isEmpty();
        verifyNoInteractions(unitatOrganitzativaRepository);
    }

    @Test
    @DisplayName("getIndexFills: construeix l'índex pare -> fills correctament")
    void getIndexFills_construeixIndexCorrectament() {
        UnitatOrganitzativaEntity root = UnitatOrganitzativaEntity.builder()
            .codi("ROOT")
            .codiUnitatArrel("ROOT")
            .build();
        UnitatOrganitzativaEntity fill1 = UnitatOrganitzativaEntity.builder()
            .codi("FILL_1")
            .codiUnitatSuperior("ROOT")
            .codiUnitatArrel("ROOT")
            .build();
        UnitatOrganitzativaEntity fill2 = UnitatOrganitzativaEntity.builder()
            .codi("FILL_2")
            .codiUnitatSuperior("ROOT")
            .codiUnitatArrel("ROOT")
            .build();
        UnitatOrganitzativaEntity net1 = UnitatOrganitzativaEntity.builder()
            .codi("NET_1")
            .codiUnitatSuperior("FILL_1")
            .codiUnitatArrel("ROOT")
            .build();

        when(unitatOrganitzativaRepository.findByCodiUnitatArrel("ROOT"))
            .thenReturn(List.of(root, fill1, fill2, net1));

        Map<String, List<String>> index = organitzativaTreeHelper.getIndexFills("ROOT");

        assertThat(index).hasSize(2);
        assertThat(index.get("ROOT")).containsExactlyInAnyOrder("FILL_1", "FILL_2");
        assertThat(index.get("FILL_1")).containsExactly("NET_1");
    }

    @Test
    @DisplayName("getDescendentsIElMateix(arrel, codi): resol descendents recursivament")
    void getDescendentsIElMateix_individual_resolDescendents() {
        UnitatOrganitzativaEntity root = UnitatOrganitzativaEntity.builder()
            .codi("ROOT")
            .codiUnitatArrel("ROOT")
            .build();
        UnitatOrganitzativaEntity fill1 = UnitatOrganitzativaEntity.builder()
            .codi("FILL_1")
            .codiUnitatSuperior("ROOT")
            .codiUnitatArrel("ROOT")
            .build();
        UnitatOrganitzativaEntity net1 = UnitatOrganitzativaEntity.builder()
            .codi("NET_1")
            .codiUnitatSuperior("FILL_1")
            .codiUnitatArrel("ROOT")
            .build();

        when(unitatOrganitzativaRepository.findByCodiUnitatArrel("ROOT"))
            .thenReturn(List.of(root, fill1, net1));

        Set<String> descendents = organitzativaTreeHelper.getDescendentsIElMateix("ROOT", "FILL_1");

        assertThat(descendents).containsExactlyInAnyOrder("FILL_1", "NET_1");
    }

    @Test
    @DisplayName("getDescendentsIElMateix(llista): agrupa per arrel i fa 1 sola consulta per arrel")
    void getDescendentsIElMateix_llista_agrupaPerArrelIUnifica() {
        // Arbre 1 (A001): ROOT1 -> F1 -> N1
        UnitatOrganitzativaEntity uoRoot1 = UnitatOrganitzativaEntity.builder().codi("ROOT1").codiUnitatArrel("A001").build();
        UnitatOrganitzativaEntity uoF1 = UnitatOrganitzativaEntity.builder().codi("F1").codiUnitatSuperior("ROOT1").codiUnitatArrel("A001").build();
        UnitatOrganitzativaEntity uoN1 = UnitatOrganitzativaEntity.builder().codi("N1").codiUnitatSuperior("F1").codiUnitatArrel("A001").build();

        // Arbre 2 (A002): ROOT2 -> F2
        UnitatOrganitzativaEntity uoRoot2 = UnitatOrganitzativaEntity.builder().codi("ROOT2").codiUnitatArrel("A002").build();
        UnitatOrganitzativaEntity uoF2 = UnitatOrganitzativaEntity.builder().codi("F2").codiUnitatSuperior("ROOT2").codiUnitatArrel("A002").build();

        when(unitatOrganitzativaRepository.findByCodiUnitatArrel("A001")).thenReturn(List.of(uoRoot1, uoF1, uoN1));
        when(unitatOrganitzativaRepository.findByCodiUnitatArrel("A002")).thenReturn(List.of(uoRoot2, uoF2));

        // Passem múltiples unitats del mateix arbre A001 (ROOT1 i F1) i de l'arbre A002 (ROOT2)
        List<UnitatOrganitzativaEntity> input = List.of(uoRoot1, uoF1, uoRoot2);

        Set<String> result = organitzativaTreeHelper.getDescendentsIElMateix(input);

        // findByCodiUnitatArrel s'ha d'haver cridat EXACTAMENT 1 vegada per a A001 (i 1 per a A002), no 2 vegades per a A001!
        verify(unitatOrganitzativaRepository, times(1)).findByCodiUnitatArrel("A001");
        verify(unitatOrganitzativaRepository, times(1)).findByCodiUnitatArrel("A002");

        assertThat(result).containsExactlyInAnyOrder("ROOT1", "F1", "N1", "ROOT2", "F2");
    }

    @Test
    @DisplayName("getDescendentsIElMateix(llista): gestiona unitats sense arrel coneguda")
    void getDescendentsIElMateix_llista_senseArrelConceguda() {
        UnitatOrganitzativaEntity uo = UnitatOrganitzativaEntity.builder()
            .codi("SENSE_ARREL")
            .codiUnitatArrel(null)
            .build();

        Set<String> result = organitzativaTreeHelper.getDescendentsIElMateix(List.of(uo));

        assertThat(result).containsExactly("SENSE_ARREL");
        verifyNoInteractions(unitatOrganitzativaRepository);
    }

    @Test
    @DisplayName("getDescendentsIElMateix(llista): retorna set buit si la llista és null o buida")
    void getDescendentsIElMateix_llista_nullOBuida() {
        assertThat(organitzativaTreeHelper.getDescendentsIElMateix(null)).isEmpty();
        assertThat(organitzativaTreeHelper.getDescendentsIElMateix(Collections.emptyList())).isEmpty();
    }
}
