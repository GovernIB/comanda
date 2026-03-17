package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DimensioServiceImpl")
class DimensioServiceImplTest {

    @Mock
    private SpringFilterHelper springFilterHelper;
    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @InjectMocks
    private DimensioServiceImpl dimensioService;

    @Test
    @DisplayName("namedFilterToSpecification retorna null per a filtres desconeguts")
    void namedFilterToSpecification_quanFiltreDesconegut_retornaNull() {
        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification("desconegut");

        // Assert
        assertThat(spec).isNull();
    }

    @Test
    @DisplayName("namedFilterToSpecification gestiona el filtre per app correctament")
    void namedFilterToSpecification_quanFiltreApp_retornaSpecification() {
        // Arrange
        String filterName = Dimensio.NAMED_FILTER_BY_APP_GROUP_BY_NOM + ":1";
        List<Long> ids = Arrays.asList(10L, 20L);
        when(estadisticaClientHelper.getEntornAppsIdByAppId(1L)).thenReturn(ids);

        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verify(estadisticaClientHelper).getEntornAppsIdByAppId(1L);
    }

    @Test
    @DisplayName("namedFilterToSpecification retorna especificació disjoint quan no hi ha entorns")
    void namedFilterToSpecification_quanSenseEntorns_retornaDisjunction() {
        // Arrange
        String filterName = Dimensio.NAMED_FILTER_BY_APP_GROUP_BY_NOM + ":1";
        when(estadisticaClientHelper.getEntornAppsIdByAppId(1L)).thenReturn(Collections.emptyList());

        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verify(estadisticaClientHelper).getEntornAppsIdByAppId(1L);
    }

    @Test
    @DisplayName("namedFilterToSpecification gestiona filtre sense app id")
    void namedFilterToSpecification_quanSenseAppId_retornaDisjunction() {
        // Arrange
        String filterName = Dimensio.NAMED_FILTER_BY_APP_GROUP_BY_NOM + ":";

        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verifyNoInteractions(estadisticaClientHelper);
    }

    @Test
    @DisplayName("additionalSpringFilter afegeix filtres d'aplicació si s'indiquen a namedQueries")
    void additionalSpringFilter_quanNamedQueriesAmbApp_afegeixFiltre() {
        // Arrange
        String currentFilter = "codi:'TEST'";
        String[] namedQueries = {"filterByApp:100"};

        // Act
        dimensioService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        verify(springFilterHelper).filterByApp(eq(100L), anyString());
    }

    @Test
    @DisplayName("additionalSpringFilter retorna null quan no hi ha filtres")
    void additionalSpringFilter_quanSenseFiltres_retornaNull() {
        // Act
        String result = dimensioService.additionalSpringFilter("", new String[0]);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("additionalSpringFilter combina filtre actual amb namedQueries")
    void additionalSpringFilter_quanFiltreINamedQueries_combinaFiltres() {
        // Arrange
        String currentFilter = "codi:'TEST'";
        String[] namedQueries = {"filterByApp:100"};

        // Act
        String result = dimensioService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        assertThat(result).isNotNull();
        verify(springFilterHelper).filterByApp(eq(100L), anyString());
    }
}
