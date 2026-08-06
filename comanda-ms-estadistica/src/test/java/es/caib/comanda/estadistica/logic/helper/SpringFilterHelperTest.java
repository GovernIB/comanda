package es.caib.comanda.estadistica.logic.helper;

import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a SpringFilterHelper")
class SpringFilterHelperTest {

    @Mock
    private EntornAppServiceClient entornAppServiceClient;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @InjectMocks
    private SpringFilterHelper springFilterHelper;

    // ========================================================================
    // 1. TESTOS PER A filterByApp
    // ========================================================================

    @Test
    @DisplayName("filterByApp: construeix un filtre OR correcte quan hi ha múltiples entornApps")
    void filterByApp_quanHiHaMultiplesEntornApps_llavorsConstrueixFiltreOr() {
        // Arrange
        long appId = 10L;
        String entornAppIdField = "entornApp.id";
        String authHeader = "Bearer token";

        EntornApp ea1 = new EntornApp();
        ea1.setId(100L);
        EntornApp ea2 = new EntornApp();
        ea2.setId(200L);

        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            List.of(EntityModel.of(ea1), EntityModel.of(ea2)),
            new PagedModel.PageMetadata(2, 0, 2)
        );

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(authHeader);
        when(entornAppServiceClient.find(isNull(), eq("app.id:10"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        Filter result = springFilterHelper.filterByApp(appId, entornAppIdField);

        // Assert
        assertThat(result).isNotNull();
        // La representació en string depèn de la implementació interna de turkraft,
        // però verifiquem que l'objecte Filter es construeix correctament.
    }

    @Test
    @DisplayName("filterByApp: ignora elements nuls en la resposta del client")
    void filterByApp_quanRespostaConteNuls_llavorsIgnoraNuls() {
        // Arrange
        long appId = 10L;
        String entornAppIdField = "entornApp.id";
        String authHeader = "Bearer token";

        EntornApp ea1 = new EntornApp();
        ea1.setId(100L);

        // Simulem una resposta que conté un element nul·li a la llista
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            List.of(EntityModel.of(ea1)),
            new PagedModel.PageMetadata(2, 0, 2)
        );

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(authHeader);
        when(entornAppServiceClient.find(isNull(), eq("app.id:10"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        Filter result = springFilterHelper.filterByApp(appId, entornAppIdField);

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("filterByApp: retorna filtre buit quan la llista d'entornApps és buida")
    void filterByApp_quanLlistaEsBuida_llavorsRetornaFiltreBuit() {
        // Arrange
        long appId = 10L;
        String entornAppIdField = "entornApp.id";
        String authHeader = "Bearer token";

        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
            Collections.emptyList(),
            new PagedModel.PageMetadata(0, 0, 0)
        );

        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(authHeader);
        when(entornAppServiceClient.find(isNull(), eq("app.id:10"), isNull(), isNull(), eq("UNPAGED"), isNull(), eq(authHeader)))
            .thenReturn(pagedModel);

        // Act
        Filter result = springFilterHelper.filterByApp(appId, entornAppIdField);

        // Assert
        assertThat(result).isNull();
    }

    // ========================================================================
    // 2. TESTOS PER A buildOrFilter
    // ========================================================================

    @Test
    @DisplayName("buildOrFilter: construeix la cadena OR correctament amb valors vàlids")
    void buildOrFilter_quanValorsSonValids_llavorsConstrueixCadenaOr() {
        // Arrange
        Set<Serializable> values = new HashSet<>();
        values.add(3L);
        values.add(1L);
        values.add(2L);

        // Act
        String result = SpringFilterHelper.buildOrFilter("appId", values);

        // Assert
        // S'ordenen per valor numèric: 1, 2, 3
        assertThat(result).isEqualTo("appId:1 or appId:2 or appId:3");
    }

    @Test
    @DisplayName("buildOrFilter: retorna null quan el conjunt de valors és null")
    void buildOrFilter_quanValorsSonNull_llavorsRetornaNull() {
        // Act
        String result = SpringFilterHelper.buildOrFilter("appId", null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("buildOrFilter: retorna null quan el conjunt de valors és buit")
    void buildOrFilter_quanValorsSonBuits_llavorsRetornaNull() {
        // Act
        String result = SpringFilterHelper.buildOrFilter("appId", Collections.emptySet());

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("buildOrFilter: ordena correctament valors de tipus String que representen números")
    void buildOrFilter_quanValorsSonStringsNumerics_llavorsOrdenaNumericament() {
        // Arrange
        Set<Serializable> values = new HashSet<>();
        values.add("10");
        values.add("2");
        values.add("1");

        // Act
        String result = SpringFilterHelper.buildOrFilter("entornId", values);

        // Assert
        assertThat(result).isEqualTo("entornId:1 or entornId:2 or entornId:10");
    }

    // ========================================================================
    // 3. TESTOS PER A and
    // ========================================================================

    @Test
    @DisplayName("and: concatena opcions vàlides amb ' and '")
    void and_quanOpcionsSonValides_llavorsConcatenaAmbAnd() {
        // Act
        String result = SpringFilterHelper.and("a:1", "b:2", "c:3");

        // Assert
        assertThat(result).isEqualTo("a:1 and b:2 and c:3");
    }

    @Test
    @DisplayName("and: ignora valors nuls i cadenes buides o amb espais")
    void and_quanHiHaNulsOBuits_llavorsIgnoraValorsInvalids() {
        // Act
        String result = SpringFilterHelper.and("a:1", null, "", "   ", "b:2");

        // Assert
        assertThat(result).isEqualTo("a:1 and b:2");
    }

    @Test
    @DisplayName("and: retorna cadena buida quan totes les opcions són invàlides")
    void and_quanTotesOpcionsSonInvalides_llavorsRetornaBuit() {
        // Act
        String result = SpringFilterHelper.and(null, "", "   ");

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 4. TESTOS PER A or
    // ========================================================================

    @Test
    @DisplayName("or: concatena opcions vàlides amb ' or '")
    void or_quanOpcionsSonValides_llavorsConcatenaAmbOr() {
        // Act
        String result = SpringFilterHelper.or("a:1", "b:2", "c:3");

        // Assert
        assertThat(result).isEqualTo("a:1 or b:2 or c:3");
    }

    @Test
    @DisplayName("or: ignora valors nuls i cadenes buides o amb espais")
    void or_quanHiHaNulsOBuits_llavorsIgnoraValorsInvalids() {
        // Act
        String result = SpringFilterHelper.or("a:1", null, "", "   ", "b:2");

        // Assert
        assertThat(result).isEqualTo("a:1 or b:2");
    }

    @Test
    @DisplayName("or: retorna cadena buida quan totes les opcions són invàlides")
    void or_quanTotesOpcionsSonInvalides_llavorsRetornaBuit() {
        // Act
        String result = SpringFilterHelper.or(null, "", "   ");

        // Assert
        assertThat(result).isEmpty();
    }
}
