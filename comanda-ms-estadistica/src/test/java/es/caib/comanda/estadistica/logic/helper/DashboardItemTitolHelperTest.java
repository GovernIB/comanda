package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardItemTitolHelper")
class DashboardItemTitolHelperTest {

    @Mock
    private DashboardItemRepository dashboardItemRepository;

    @Mock
    private DashboardTitolRepository dashboardTitolRepository;

    @InjectMocks
    private DashboardItemTitolHelper dashboardItemTitolHelper;

    // ========================================================================
    // 1. TESTOS PER A completeResourceItemLogic
    // ========================================================================

    @Test
    @DisplayName("completeResourceItemLogic: no modifica posY quan el dashboard és null")
    void completeResourceItemLogic_quanDashboardEsNull_llavorsNoModificaPosY() {
        // Arrange
        DashboardItem resource = new DashboardItem();
        resource.setDashboard(null);
        resource.setPosY(5);

        // Act
        dashboardItemTitolHelper.completeResourceItemLogic(resource);

        // Assert
        assertThat(resource.getPosY()).isEqualTo(5);
        verify(dashboardItemRepository, never()).findByDashboardId(anyLong());
    }

    @Test
    @DisplayName("completeResourceItemLogic: manté el posX i posY existents quan ja tenen valor")
    void completeResourceItemLogic_quanPosJaTéValor_llavorsMantéElValor() {
        // Arrange
        DashboardItem resource = new DashboardItem();
        Dashboard dashboard = new Dashboard();
        dashboard.setId(1L);
        resource.setDashboard(ResourceReference.toResourceReference(dashboard.getId()));
        resource.setPosX(2);
        resource.setPosY(10);

        // Act
        dashboardItemTitolHelper.completeResourceItemLogic(resource);

        // Assert
        assertThat(resource.getPosX()).isEqualTo(2);
        assertThat(resource.getPosY()).isEqualTo(10);
        verify(dashboardItemRepository, never()).findByDashboardId(anyLong());
    }

    @Test
    @DisplayName("completeResourceItemLogic: calcula i assigna posX i posY quan són nulls")
    void completeResourceItemLogic_quanPosEsNull_llavorsCalculaIAssignaEspaiLliure() {
        // Arrange
        DashboardItem resource = new DashboardItem();
        Dashboard dashboard = new Dashboard();
        dashboard.setId(1L);
        resource.setDashboard(ResourceReference.toResourceReference(dashboard.getId()));
        resource.setWidth(3);
        resource.setHeight(3);
        resource.setPosY(null);

        es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity existingItem =
            new es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity();
        existingItem.setPosX(0);
        existingItem.setPosY(0);
        existingItem.setWidth(3);
        existingItem.setHeight(3);

        when(dashboardItemRepository.findByDashboardId(1L)).thenReturn(Collections.singletonList(existingItem));
        when(dashboardTitolRepository.findByDashboardId(1L)).thenReturn(Collections.emptyList());

        // Act
        dashboardItemTitolHelper.completeResourceItemLogic(resource);

        // Assert
        assertThat(resource.getPosX()).isEqualTo(3);
        assertThat(resource.getPosY()).isEqualTo(0);
    }

    // ========================================================================
    // 2. TESTOS PER A completeResourceTitolLogic
    // ========================================================================

    @Test
    @DisplayName("completeResourceTitolLogic: no modifica posY quan el dashboard és null")
    void completeResourceTitolLogic_quanDashboardEsNull_llavorsNoModificaPosY() {
        // Arrange
        DashboardTitol resource = new DashboardTitol();
        resource.setDashboard(null);
        resource.setPosY(5);

        // Act
        dashboardItemTitolHelper.completeResourceTitolLogic(resource);

        // Assert
        assertThat(resource.getPosY()).isEqualTo(5);
        verify(dashboardTitolRepository, never()).findByDashboardId(anyLong());
    }

    @Test
    @DisplayName("completeResourceTitolLogic: calcula i assigna posX i posY quan són nulls")
    void completeResourceTitolLogic_quanPosEsNull_llavorsCalculaIAssignaEspaiLliure() {
        // Arrange
        DashboardTitol resource = new DashboardTitol();
        Dashboard dashboard = new Dashboard();
        dashboard.setId(2L);
        resource.setDashboard(ResourceReference.toResourceReference(dashboard.getId()));
        resource.setWidth(24);
        resource.setHeight(1);
        resource.setPosY(null);

        es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity existingTitol =
            new es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity();
        existingTitol.setPosX(0);
        existingTitol.setPosY(0);
        existingTitol.setWidth(24);
        existingTitol.setHeight(1);

        when(dashboardItemRepository.findByDashboardId(2L)).thenReturn(Collections.emptyList());
        when(dashboardTitolRepository.findByDashboardId(2L)).thenReturn(Collections.singletonList(existingTitol));

        // Act
        dashboardItemTitolHelper.completeResourceTitolLogic(resource);

        // Assert
        assertThat(resource.getPosX()).isEqualTo(0);
        assertThat(resource.getPosY()).isEqualTo(1);
    }

    // ========================================================================
    // 3. TESTOS PARAMETritzats PER A getPosYValue (Lògica del Stream i Màxims)
    // ========================================================================

    @ParameterizedTest
    @MethodSource("provideMaxPosYCases")
    @DisplayName("getPosYValue: calcula correctament el màxim posY filtrant nulls")
    void getPosYValue_quanPosYEsNull_llavorsCalculaMaximCorrectament(Integer maxItems, Integer maxTitols, Integer expectedMax) {
        // Arrange
        Long dashboardId = 1L;
        when(dashboardItemRepository.findMaxBottomPositionByDashboardId(dashboardId)).thenReturn(maxItems);
        when(dashboardTitolRepository.findMaxBottomPositionByDashboardId(dashboardId)).thenReturn(maxTitols);

        // Act
        Integer result = (Integer) ReflectionTestUtils.invokeMethod(
            dashboardItemTitolHelper, "getPosYValue", dashboardId, (Integer) null);

        // Assert
        assertThat(result).isEqualTo(expectedMax);
    }

    private static Stream<Arguments> provideMaxPosYCases() {
        return Stream.of(
            arguments(null, null, 0),          // Ambdós nulls -> retorna 0
            arguments(5, null, 5),             // Només items té valor
            arguments(null, 8, 8),             // Només titols té valor
            arguments(10, 5, 10),              // Items és major
            arguments(3, 15, 15)               // Titols és major
        );
    }

    // ========================================================================
    // 4. TESTOS PER A CASOS LÍMIT DE getPosYValue (Early Returns)
    // ========================================================================

    @Test
    @DisplayName("getPosYValue: retorna el posY original quan ja té un valor definit")
    void getPosYValue_quanPosYJaTéValor_llavorsRetornaAquestValorSenseConsultarRepositoris() {
        // Arrange
        Long dashboardId = 1L;
        Integer existingPosY = 20;

        // Act
        Integer result = (Integer) ReflectionTestUtils.invokeMethod(
            dashboardItemTitolHelper, "getPosYValue", dashboardId, existingPosY);

        // Assert
        assertThat(result).isEqualTo(20);
        verify(dashboardItemRepository, never()).findMaxBottomPositionByDashboardId(anyLong());
        verify(dashboardTitolRepository, never()).findMaxBottomPositionByDashboardId(anyLong());
    }

    @Test
    @DisplayName("getPosYValue: retorna null quan el dashboardId és null (independentment del posY)")
    void getPosYValue_quanDashboardIdEsNull_llavorsRetornaNullSenseConsultarRepositoris() {
        // Arrange
        Integer posY = null;

        // Act
        Integer result = (Integer) ReflectionTestUtils.invokeMethod(
            dashboardItemTitolHelper, "getPosYValue", null, posY);

        // Assert
        assertThat(result).isNull();
        verify(dashboardItemRepository, never()).findMaxBottomPositionByDashboardId(anyLong());
        verify(dashboardTitolRepository, never()).findMaxBottomPositionByDashboardId(anyLong());
    }

    // ========================================================================
    // 5. TESTOS PER A findFirstAvailableSpace (Algorisme 2D)
    // ========================================================================

    @Test
    @DisplayName("findFirstAvailableSpace: retorna (0, 0) quan el dashboardId és null")
    void findFirstAvailableSpace_quanDashboardIdNull_llavorsRetornaZeroZero() {
        DashboardItemTitolHelper.GridPosition pos = dashboardItemTitolHelper.findFirstAvailableSpace(null, 3, 3);
        assertThat(pos.getPosX()).isEqualTo(0);
        assertThat(pos.getPosY()).isEqualTo(0);
    }

    @Test
    @DisplayName("findFirstAvailableSpace: retorna (0, 0) quan el dashboard està buit")
    void findFirstAvailableSpace_quanDashboardBuit_llavorsRetornaZeroZero() {
        when(dashboardItemRepository.findByDashboardId(1L)).thenReturn(Collections.emptyList());
        when(dashboardTitolRepository.findByDashboardId(1L)).thenReturn(Collections.emptyList());

        DashboardItemTitolHelper.GridPosition pos = dashboardItemTitolHelper.findFirstAvailableSpace(1L, 3, 3);
        assertThat(pos.getPosX()).isEqualTo(0);
        assertThat(pos.getPosY()).isEqualTo(0);
    }

    @Test
    @DisplayName("findFirstAvailableSpace: col·loca al costat dret quan hi ha espai a la mateixa fila")
    void findFirstAvailableSpace_quanHiHaEspaiAlCostat_llavorsCollocaAlCostat() {
        es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity item1 =
            new es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity();
        item1.setPosX(0);
        item1.setPosY(0);
        item1.setWidth(3);
        item1.setHeight(3);

        when(dashboardItemRepository.findByDashboardId(1L)).thenReturn(Collections.singletonList(item1));
        when(dashboardTitolRepository.findByDashboardId(1L)).thenReturn(Collections.emptyList());

        DashboardItemTitolHelper.GridPosition pos = dashboardItemTitolHelper.findFirstAvailableSpace(1L, 3, 3);
        assertThat(pos.getPosX()).isEqualTo(3);
        assertThat(pos.getPosY()).isEqualTo(0);
    }

    @Test
    @DisplayName("findFirstAvailableSpace: omple un forat a la fila existent")
    void findFirstAvailableSpace_quanHiHaForat_llavorsOmpleForat() {
        es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity item1 =
            new es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity();
        item1.setPosX(0);
        item1.setPosY(0);
        item1.setWidth(3);
        item1.setHeight(3);

        es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity item2 =
            new es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity();
        item2.setPosX(6);
        item2.setPosY(0);
        item2.setWidth(18);
        item2.setHeight(3);

        when(dashboardItemRepository.findByDashboardId(1L)).thenReturn(java.util.Arrays.asList(item1, item2));
        when(dashboardTitolRepository.findByDashboardId(1L)).thenReturn(Collections.emptyList());

        // El forat és de x=3 a x=6 (ample 3). Un widget d'ample 3 hi cap exactament
        DashboardItemTitolHelper.GridPosition pos = dashboardItemTitolHelper.findFirstAvailableSpace(1L, 3, 3);
        assertThat(pos.getPosX()).isEqualTo(3);
        assertThat(pos.getPosY()).isEqualTo(0);
    }

    @Test
    @DisplayName("findFirstAvailableSpace: si el forat és massa estret, passa a la següent fila")
    void findFirstAvailableSpace_quanForatMassaEstret_llavorsPassaASeguentFila() {
        es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity item1 =
            new es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity();
        item1.setPosX(0);
        item1.setPosY(0);
        item1.setWidth(3);
        item1.setHeight(3);

        es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity item2 =
            new es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity();
        item2.setPosX(5); // Forat de 3 a 5 (ample 2)
        item2.setPosY(0);
        item2.setWidth(19);
        item2.setHeight(3);

        when(dashboardItemRepository.findByDashboardId(1L)).thenReturn(java.util.Arrays.asList(item1, item2));
        when(dashboardTitolRepository.findByDashboardId(1L)).thenReturn(Collections.emptyList());

        // Un widget d'ample 3 no cap al forat d'ample 2, per tant ha d'anar a y=3
        DashboardItemTitolHelper.GridPosition pos = dashboardItemTitolHelper.findFirstAvailableSpace(1L, 3, 3);
        assertThat(pos.getPosX()).isEqualTo(0);
        assertThat(pos.getPosY()).isEqualTo(3);
    }

    @Test
    @DisplayName("findFirstAvailableSpace: respecta títols i widgets combinats")
    void findFirstAvailableSpace_quanCombinaTitolsIWidgets_llavorsTrobaEspaiCorrecte() {
        es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity titol =
            new es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity();
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(24);
        titol.setHeight(1);

        es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity item1 =
            new es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity();
        item1.setPosX(0);
        item1.setPosY(1);
        item1.setWidth(6);
        item1.setHeight(3);

        when(dashboardItemRepository.findByDashboardId(1L)).thenReturn(Collections.singletonList(item1));
        when(dashboardTitolRepository.findByDashboardId(1L)).thenReturn(Collections.singletonList(titol));

        DashboardItemTitolHelper.GridPosition pos = dashboardItemTitolHelper.findFirstAvailableSpace(1L, 6, 3);
        assertThat(pos.getPosX()).isEqualTo(6);
        assertThat(pos.getPosY()).isEqualTo(1);
    }
}
