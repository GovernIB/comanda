package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a IndicadorServiceImpl")
class IndicadorServiceImplTest {

    @Mock
    private SpringFilterHelper springFilterHelper;
    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @InjectMocks
    private IndicadorServiceImpl indicadorService;

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
}
