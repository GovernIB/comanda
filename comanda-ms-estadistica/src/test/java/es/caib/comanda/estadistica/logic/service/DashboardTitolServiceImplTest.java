package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.helper.DashboardItemTitolHelper;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardTitolServiceImpl")
class DashboardTitolServiceImplTest {

    @Mock
    private DashboardItemTitolHelper dashboardItemTitolHelper;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    @InjectMocks
    private DashboardTitolServiceImpl dashboardTitolService;

    @BeforeEach
    void setUp() {
        // Configuració lenient per evitar NPEs en crides internes de getAllowedIds
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        lenient().when(authenticationHelper.getCurrentUserName()).thenReturn("testUser");
        lenient().when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{"ROLE_USER"});
    }

    // ========================================================================
    // 1. TESTOS PER A additionalSpringFilter
    // ========================================================================

    @Test
    @DisplayName("additionalSpringFilter: retorna el filtre original quan l'usuari és ADMIN")
    void additionalSpringFilter_quanEsAdmin_llavorsRetornaFiltreOriginal() {
        // Arrange
        String currentFilter = "titol:'Test'";
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        // Act
        String result = dashboardTitolService.additionalSpringFilter(currentFilter, new String[0]);

        // Assert
        assertThat(result).isEqualTo(currentFilter);
        verify(aclServiceClient, never()).findIdsWithAnyPermission(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("additionalSpringFilter: retorna el filtre original quan l'usuari té rol CONSULTA")
    void additionalSpringFilter_quanEsConsulta_llavorsRetornaFiltreOriginal() {
        // Arrange
        String currentFilter = "titol:'Test'";
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(true);

        // Act
        String result = dashboardTitolService.additionalSpringFilter(currentFilter, new String[0]);

        // Assert
        assertThat(result).isEqualTo(currentFilter);
        verify(aclServiceClient, never()).findIdsWithAnyPermission(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("additionalSpringFilter: aplica filtres de permisos quan l'usuari normal té accés")
    void additionalSpringFilter_quanEsUsuariNormalIAmbPermisos_llavorsAplicaFiltres() {
        // Arrange
        String currentFilter = "titol:'Test'";
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);

        Set<Serializable> permisos = Set.of(1L, 2L);
        when(aclServiceClient.findIdsWithAnyPermission(any(ResourceType.class), anyList(), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(permisos));

        // Act
        String result = dashboardTitolService.additionalSpringFilter(currentFilter, new String[0]);

        // Assert
        assertThat(result).contains(currentFilter);
        assertThat(result).contains("and");
        // Verifiquem que s'ha cridat 3 vegades (APP, ENTORN_APP, DASHBOARD)
        verify(aclServiceClient, times(3)).findIdsWithAnyPermission(any(), any(), any(), any(), anyString());
    }

    @Test
    @DisplayName("additionalSpringFilter: retorna fallback 'id:0' quan l'usuari normal no té cap permís")
    void additionalSpringFilter_quanEsUsuariNormalISensePermisos_llavorsRetornaIdZero() {
        // Arrange
        String currentFilter = "titol:'Test'";
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);

        // Simulem que l'usuari no té permisos per a cap recurs
        when(aclServiceClient.findIdsWithAnyPermission(any(ResourceType.class), anyList(), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(Collections.emptySet()));

        // Act
        String result = dashboardTitolService.additionalSpringFilter(currentFilter, new String[0]);

        // Assert
        // SpringFilterHelper.or de nulls retorna null o buit, i la lògica aplica "id:0"
        assertThat(result).contains("id:0");
        verify(aclServiceClient, times(3)).findIdsWithAnyPermission(any(), any(), any(), any(), anyString());
    }

    // ========================================================================
    // 2. TESTOS PER A getAllowedIds
    // ========================================================================

    @Test
    @DisplayName("getAllowedIds: retorna el conjunt d'IDs quan el client ACL respon correctament")
    void getAllowedIds_quanAclRetornaDades_llavorsRetornaConjunt() {
        // Arrange
        Set<Serializable> expectedIds = Set.of(10L, 20L);
        when(aclServiceClient.findIdsWithAnyPermission(
            eq(ResourceType.DASHBOARD),
            eq(List.of(PermissionEnum.READ, PermissionEnum.WRITE)),
            anyString(),
            anyList(),
            anyString()))
            .thenReturn(ResponseEntity.ok(expectedIds));

        // Act
        Set<Serializable> result = dashboardTitolService.getAllowedIds(ResourceType.DASHBOARD, List.of(PermissionEnum.READ, PermissionEnum.WRITE));

        // Assert
        assertThat(result).containsExactlyInAnyOrder(10L, 20L);
    }

    @Test
    @DisplayName("getAllowedIds: retorna un conjunt buit quan el client ACL retorna null")
    void getAllowedIds_quanAclRetornaNull_llavorsRetornaConjuntBuit() {
        // Arrange
        when(aclServiceClient.findIdsWithAnyPermission(
            any(ResourceType.class),
            anyList(),
            anyString(),
            anyList(),
            anyString()))
            .thenReturn(ResponseEntity.ok(null));

        // Act
        Set<Serializable> result = dashboardTitolService.getAllowedIds(ResourceType.APP, List.of(PermissionEnum.PERM0));

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 3. TESTOS PER A completeResource
    // ========================================================================

    @Test
    @DisplayName("completeResource: delega correctament la lògica a DashboardItemTitolHelper")
    void completeResource_quanEsCrida_llavorsDelegaAHelper() {
        // Arrange
        DashboardTitol resource = new DashboardTitol();

        // Act
        dashboardTitolService.completeResource(resource);

        // Assert
        verify(dashboardItemTitolHelper, times(1)).completeResourceTitolLogic(resource);
    }
}
