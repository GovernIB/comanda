package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltre;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltreTipus;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardFiltreEntity;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests per a DashboardHelper.afterConversionLogic: el mapeig genèric d'entitat a recurs
 * (ObjectMappingHelper.map) no sap convertir camps de tipus List&lt;Entity&gt; i sempre els deixa a null,
 * així que cal comprovar que la conversió manual de filtres (afterConversionGetFiltres) realment
 * emplena Dashboard.filtres a partir de DashboardEntity.filtres (vegeu DashboardFiltreBar al frontend,
 * que depèn d'aquest camp per mostrar els filtres de capçalera a la pantalla de visualització).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardHelper")
class DashboardHelperTest {

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;
    @Mock
    private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @InjectMocks
    private DashboardHelper dashboardHelper;

    private DashboardFiltreEntity filtreEntity(Long id, DashboardFiltreTipus tipus) {
        DashboardFiltreEntity entity = new DashboardFiltreEntity();
        entity.setId(id);
        entity.setTipus(tipus);
        entity.setOrdre(0);
        return entity;
    }

    private DashboardFiltre filtreResource(Long id, DashboardFiltreTipus tipus) {
        DashboardFiltre resource = new DashboardFiltre();
        resource.setId(id);
        resource.setTipus(tipus);
        return resource;
    }

    @Test
    @DisplayName("afterConversionLogic: converteix els filtres de l'entitat i els assigna al recurs")
    void afterConversionLogic_ambFiltres_emplenaFiltresDelRecurs() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        DashboardFiltreEntity periodeEntity = filtreEntity(10L, DashboardFiltreTipus.PERIODE);
        entity.setFiltres(List.of(periodeEntity));

        DashboardFiltre periodeResource = filtreResource(10L, DashboardFiltreTipus.PERIODE);
        when(resourceEntityMappingHelper.entityToResource(periodeEntity, DashboardFiltre.class))
                .thenReturn(periodeResource);

        Dashboard resource = new Dashboard();

        // Act
        dashboardHelper.afterConversionLogic(entity, resource);

        // Assert
        assertThat(resource.getFiltres()).containsExactly(periodeResource);
    }

    @Test
    @DisplayName("afterConversionLogic: amb diversos filtres, en manté l'ordre i els converteix tots")
    void afterConversionLogic_ambDiversosFiltres_elsConverteixTots() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        DashboardFiltreEntity periodeEntity = filtreEntity(10L, DashboardFiltreTipus.PERIODE);
        DashboardFiltreEntity dimensioEntity = filtreEntity(20L, DashboardFiltreTipus.DIMENSIO);
        entity.setFiltres(List.of(periodeEntity, dimensioEntity));

        DashboardFiltre periodeResource = filtreResource(10L, DashboardFiltreTipus.PERIODE);
        DashboardFiltre dimensioResource = filtreResource(20L, DashboardFiltreTipus.DIMENSIO);
        when(resourceEntityMappingHelper.entityToResource(periodeEntity, DashboardFiltre.class))
                .thenReturn(periodeResource);
        when(resourceEntityMappingHelper.entityToResource(dimensioEntity, DashboardFiltre.class))
                .thenReturn(dimensioResource);

        Dashboard resource = new Dashboard();

        // Act
        dashboardHelper.afterConversionLogic(entity, resource);

        // Assert
        assertThat(resource.getFiltres()).containsExactly(periodeResource, dimensioResource);
    }

    @Test
    @DisplayName("afterConversionLogic: si l'entitat no té filtres, no en crea la llista ni crida el mapeig")
    void afterConversionLogic_senseFiltres_noModificaElRecurs() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setFiltres(null);

        Dashboard resource = new Dashboard();

        // Act
        dashboardHelper.afterConversionLogic(entity, resource);

        // Assert
        assertThat(resource.getFiltres()).isNull();
        verifyNoInteractions(resourceEntityMappingHelper);
    }

    @Test
    @DisplayName("afterConversionLogic: amb una llista de filtres buida, assigna una llista buida")
    void afterConversionLogic_ambFiltresBuits_assignaLlistaBuida() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setFiltres(Collections.emptyList());

        Dashboard resource = new Dashboard();

        // Act
        dashboardHelper.afterConversionLogic(entity, resource);

        // Assert
        assertThat(resource.getFiltres()).isEmpty();
    }
}
