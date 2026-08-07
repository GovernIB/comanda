package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.dir3.SistemaExternException;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPlugin;
import es.caib.comanda.estadistica.logic.helper.UnitatOrganitzativaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Entitat;
import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EntitatServiceImpl")
class EntitatServiceImplTest {

    @Mock
    private UnitatsOrganitzativesPlugin unitatsOrganitzativesPlugin;

    @Mock
    private UnitatOrganitzativaHelper unitatOrganitzativaHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @InjectMocks
    private EntitatServiceImpl entitatService;

    private static final String AUTH_HEADER = "Bearer test-token";

    @BeforeEach
    void setUp() {
        // Injecció del helper de mapeig que ve de la classe base BaseMutableResourceService
        ReflectionTestUtils.setField(entitatService, "resourceEntityMappingHelper", resourceEntityMappingHelper);

        // Configuració lenient per a crides comunes
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
    }

    // ========================================================================
    // 1. TESTOS PER A PermisPerspective
    // ========================================================================

    @Test
    @DisplayName("PermisPerspective: assigna el nombre de permisos quan l'ACL retorna un valor")
    void permisPerspective_quanAclRetornaValor_llavorsAssignaNumPermisos() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(10L);
        Entitat resource = new Entitat();

        when(aclServiceClient.countSidsWithPermission(eq(ResourceType.ENTITAT), eq(10L), eq(AUTH_HEADER)))
            .thenReturn(ResponseEntity.ok(5));

        EntitatServiceImpl.PermisPerspective perspective = entitatService.new PermisPerspective();

        // Act
        perspective.applySingle("PERSP_PERMIS_NUM", entity, resource);

        // Assert
        assertThat(resource.getNumPermisos()).isEqualTo(5);
        verify(aclServiceClient, times(1)).countSidsWithPermission(ResourceType.ENTITAT, 10L, AUTH_HEADER);
    }

    @Test
    @DisplayName("PermisPerspective: assigna 0 permisos quan l'ACL retorna un cos nul")
    void permisPerspective_quanAclRetornaNull_llavorsAssignaZeroPermisos() {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setId(10L);
        Entitat resource = new Entitat();

        when(aclServiceClient.countSidsWithPermission(eq(ResourceType.ENTITAT), eq(10L), eq(AUTH_HEADER)))
            .thenReturn(ResponseEntity.ok(null));

        EntitatServiceImpl.PermisPerspective perspective = entitatService.new PermisPerspective();

        // Act
        perspective.applySingle("PERSP_PERMIS_NUM", entity, resource);

        // Assert
        assertThat(resource.getNumPermisos()).isEqualTo(0);
    }

    // ========================================================================
    // 2. TESTOS PER A RefreshUOActionExecutor
    // ========================================================================

    @Test
    @DisplayName("RefreshUOActionExecutor: executa correctament l'actualització de les unitats organitzatives")
    void refreshUoActionExecutor_quanEsValid_llavorsActualitzaIretornaRecurs() throws ActionExecutionException, SistemaExternException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setCodiDir3("DIR3_123");

        UnitatOrganitzativaEntity uo = new UnitatOrganitzativaEntity();
        uo.setCodi("UO1");

        Entitat expectedResource = new Entitat();

        when(unitatsOrganitzativesPlugin.findAll("DIR3_123")).thenReturn(Collections.singletonList(uo));
        when(resourceEntityMappingHelper.entityToResource(entity, Entitat.class)).thenReturn(expectedResource);

        EntitatServiceImpl.RefreshUOActionExecutor executor = entitatService.new RefreshUOActionExecutor();

        // Act
        Entitat result = executor.exec(Entitat.ACTION_REFRESH_UO, entity, null);

        // Assert
        verify(unitatsOrganitzativesPlugin, times(1)).findAll("DIR3_123");
        verify(unitatOrganitzativaHelper, times(1)).updateAll(Collections.singletonList(uo));
        verify(resourceEntityMappingHelper, times(1)).entityToResource(entity, Entitat.class);
        assertThat(result).isSameAs(expectedResource);
    }

    @Test
    @DisplayName("RefreshUOActionExecutor: llança ActionExecutionException quan el plugin falla")
    void refreshUoActionExecutor_quanPluginFalla_llancaActionExecutionException() throws SistemaExternException {
        // Arrange
        EntitatEntity entity = new EntitatEntity();
        entity.setCodiDir3("DIR3_123");

        when(unitatsOrganitzativesPlugin.findAll("DIR3_123")).thenThrow(new RuntimeException("Error de connexió a DIR3"));

        EntitatServiceImpl.RefreshUOActionExecutor executor = entitatService.new RefreshUOActionExecutor();

        // Act & Assert
        assertThatThrownBy(() -> executor.exec(Entitat.ACTION_REFRESH_UO, entity, null))
            .isInstanceOf(ActionExecutionException.class)
            .hasMessageContaining("Error de connexió a DIR3");

        verify(unitatOrganitzativaHelper, times(0)).updateAll(any());
    }

    @Test
    @DisplayName("RefreshUOActionExecutor.onChange: s'executa sense errors (mètode buit)")
    void refreshUoActionExecutor_onChange_quanEsCrida_llavorsNoFaRes() {
        // Arrange
        EntitatServiceImpl.RefreshUOActionExecutor executor = entitatService.new RefreshUOActionExecutor();

        // Act & Assert
        assertDoesNotThrow(() ->
            executor.onChange(1L, null, "fieldName", "fieldValue", new HashMap<>(), new String[0], null)
        );
    }
}
