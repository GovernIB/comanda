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
        verify(dashboardItemRepository, never()).findMaxBottomPositionByDashboardId(anyLong());
    }

    @Test
    @DisplayName("completeResourceItemLogic: manté el posY existent quan ja té valor")
    void completeResourceItemLogic_quanPosYJaTéValor_llavorsMantéElValor() {
        // Arrange
        DashboardItem resource = new DashboardItem();
        Dashboard dashboard = new Dashboard();
        dashboard.setId(1L);
        resource.setDashboard(ResourceReference.toResourceReference(dashboard.getId()));
        resource.setPosY(10);

        // Act
        dashboardItemTitolHelper.completeResourceItemLogic(resource);

        // Assert
        assertThat(resource.getPosY()).isEqualTo(10);
        verify(dashboardItemRepository, never()).findMaxBottomPositionByDashboardId(anyLong());
    }

    @Test
    @DisplayName("completeResourceItemLogic: calcula i assigna el maxY quan el posY és null")
    void completeResourceItemLogic_quanPosYEsNull_llavorsCalculaIAssignaMaxY() {
        // Arrange
        DashboardItem resource = new DashboardItem();
        Dashboard dashboard = new Dashboard();
        dashboard.setId(1L);
        resource.setDashboard(ResourceReference.toResourceReference(dashboard.getId()));
        resource.setPosY(null);

        when(dashboardItemRepository.findMaxBottomPositionByDashboardId(1L)).thenReturn(5);
        when(dashboardTitolRepository.findMaxBottomPositionByDashboardId(1L)).thenReturn(8);

        // Act
        dashboardItemTitolHelper.completeResourceItemLogic(resource);

        // Assert
        assertThat(resource.getPosY()).isEqualTo(8);
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
        verify(dashboardTitolRepository, never()).findMaxBottomPositionByDashboardId(anyLong());
    }

    @Test
    @DisplayName("completeResourceTitolLogic: calcula i assigna el maxY quan el posY és null")
    void completeResourceTitolLogic_quanPosYEsNull_llavorsCalculaIAssignaMaxY() {
        // Arrange
        DashboardTitol resource = new DashboardTitol();
        Dashboard dashboard = new Dashboard();
        dashboard.setId(2L);
        resource.setDashboard(ResourceReference.toResourceReference(dashboard.getId()));
        resource.setPosY(null);

        when(dashboardItemRepository.findMaxBottomPositionByDashboardId(2L)).thenReturn(null);
        when(dashboardTitolRepository.findMaxBottomPositionByDashboardId(2L)).thenReturn(12);

        // Act
        dashboardItemTitolHelper.completeResourceTitolLogic(resource);

        // Assert
        assertThat(resource.getPosY()).isEqualTo(12);
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
}
