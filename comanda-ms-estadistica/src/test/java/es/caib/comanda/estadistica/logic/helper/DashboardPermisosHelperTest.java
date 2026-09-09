package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.io.Serializable;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardPermisosHelper")
class DashboardPermisosHelperTest {

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @InjectMocks
    private DashboardPermisosHelper dashboardPermisosHelper;

    @BeforeEach
    void setUp() {
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        lenient().when(authenticationHelper.getCurrentUserName()).thenReturn("testUser");
        lenient().when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{"ROLE_USER"});
    }

    // ========================================================================
    // 1. ROLS ADMINISTRATIUS
    // ========================================================================

    @Test
    @DisplayName("isAdminOrConsulta retorna true quan usuari té rol ADMIN")
    void isAdminOrConsulta_quanAdmin_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);
        assertThat(dashboardPermisosHelper.isAdminOrConsulta()).isTrue();
    }

    @Test
    @DisplayName("isAdminOrConsulta retorna true quan usuari té rol CONSULTA")
    void isAdminOrConsulta_quanConsulta_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(true);
        assertThat(dashboardPermisosHelper.isAdminOrConsulta()).isTrue();
    }

    @Test
    @DisplayName("isAdminOrConsulta retorna false quan usuari no té cap dels rols")
    void isAdminOrConsulta_quanSenseRols_retornaFalse() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);
        assertThat(dashboardPermisosHelper.isAdminOrConsulta()).isFalse();
    }

    @Test
    @DisplayName("isAdmin retorna true quan usuari té rol ADMIN")
    void isAdmin_quanAdmin_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);
        assertThat(dashboardPermisosHelper.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("isAdmin retorna false quan usuari no té rol ADMIN")
    void isAdmin_quanNoAdmin_retornaFalse() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        assertThat(dashboardPermisosHelper.isAdmin()).isFalse();
    }

    // ========================================================================
    // 2. COMPROVACIÓ DE PERMISOS ACL
    // ========================================================================

    @Test
    @DisplayName("hasPermission retorna false si resourceId és null")
    void hasPermission_quanResourceIdNull_retornaFalse() {
        assertThat(dashboardPermisosHelper.hasPermission(ResourceType.DASHBOARD, null, List.of(PermissionEnum.WRITE))).isFalse();
        verifyNoInteractions(aclServiceClient);
    }

    @Test
    @DisplayName("hasPermission retorna true quan ACL retorna true")
    void hasPermission_quanAclRetornaTrue_retornaTrue() {
        when(aclServiceClient.anyPermissionGranted(eq(ResourceType.DASHBOARD), eq(10L), anyList(), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(true));

        assertThat(dashboardPermisosHelper.hasPermission(ResourceType.DASHBOARD, 10L, List.of(PermissionEnum.WRITE))).isTrue();
    }

    @Test
    @DisplayName("hasPermission retorna false quan ACL retorna false")
    void hasPermission_quanAclRetornaFalse_retornaFalse() {
        when(aclServiceClient.anyPermissionGranted(eq(ResourceType.DASHBOARD), eq(10L), anyList(), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(false));

        assertThat(dashboardPermisosHelper.hasPermission(ResourceType.DASHBOARD, 10L, List.of(PermissionEnum.WRITE))).isFalse();
    }

    @Test
    @DisplayName("hasPermission captura excepcions i retorna false")
    void hasPermission_quanAclLlanxaExcepcio_retornaFalse() {
        when(aclServiceClient.anyPermissionGranted(any(), any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Error de connexió"));

        assertThat(dashboardPermisosHelper.hasPermission(ResourceType.DASHBOARD, 10L, List.of(PermissionEnum.WRITE))).isFalse();
    }

    @Test
    @DisplayName("getAllowedIds retorna conjunt buit si ACL retorna null")
    void getAllowedIds_quanAclNull_retornaConjuntBuit() {
        when(aclServiceClient.findIdsWithAnyPermission(any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(null));

        assertThat(dashboardPermisosHelper.getAllowedIds(ResourceType.APP, List.of(PermissionEnum.PERM1))).isEmpty();
    }

    @Test
    @DisplayName("getAllowedIds retorna conjunt amb IDs permesos")
    void getAllowedIds_quanAclRetornaIds_retornaConjunt() {
        Set<Serializable> ids = Set.of(1L, 2L);
        when(aclServiceClient.findIdsWithAnyPermission(any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(ids));

        assertThat(dashboardPermisosHelper.getAllowedIds(ResourceType.APP, List.of(PermissionEnum.PERM1)))
            .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("countSidsWithPermission retorna 0 si resourceId és null")
    void countSidsWithPermission_quanResourceIdNull_retornaZero() {
        assertThat(dashboardPermisosHelper.countSidsWithPermission(ResourceType.DASHBOARD, null)).isEqualTo(0);
    }

    @Test
    @DisplayName("countSidsWithPermission retorna nombre de SIDs")
    void countSidsWithPermission_quanAclRetornaNombre_retornaNombre() {
        when(aclServiceClient.countSidsWithPermission(eq(ResourceType.DASHBOARD), eq(10L), anyString()))
            .thenReturn(ResponseEntity.ok(5));

        assertThat(dashboardPermisosHelper.countSidsWithPermission(ResourceType.DASHBOARD, 10L)).isEqualTo(5);
    }

    @Test
    @DisplayName("countSidsWithPermission captura excepcions i retorna 0")
    void countSidsWithPermission_quanAclLlanxaExcepcio_retornaZero() {
        when(aclServiceClient.countSidsWithPermission(any(), any(), any()))
            .thenThrow(new RuntimeException("Error ACL"));

        assertThat(dashboardPermisosHelper.countSidsWithPermission(ResourceType.DASHBOARD, 10L)).isEqualTo(0);
    }

    // ========================================================================
    // 3. DRETS DE DISSENY I CREACIÓ
    // ========================================================================

    @Test
    @DisplayName("canDesign retorna true si l'usuari és ADMIN")
    void canDesign_quanAdmin_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);
        assertThat(dashboardPermisosHelper.canDesign(1L, 2L, 3L)).isTrue();
        verifyNoInteractions(aclServiceClient);
    }

    @Test
    @DisplayName("canDesign retorna true si té permís WRITE sobre el DASHBOARD")
    void canDesign_quanTePermisDashboard_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(aclServiceClient.anyPermissionGranted(eq(ResourceType.DASHBOARD), eq(1L), eq(List.of(PermissionEnum.WRITE)), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(true));

        assertThat(dashboardPermisosHelper.canDesign(1L, 2L, 3L)).isTrue();
    }

    @Test
    @DisplayName("canDesign retorna true si té permís PERM1 sobre l'APP")
    void canDesign_quanTePermisApp_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(aclServiceClient.anyPermissionGranted(eq(ResourceType.DASHBOARD), eq(1L), eq(List.of(PermissionEnum.WRITE)), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(false));
        when(aclServiceClient.anyPermissionGranted(eq(ResourceType.APP), eq(2L), eq(List.of(PermissionEnum.PERM1)), anyString(), anyList(), anyString()))
                .thenReturn(ResponseEntity.ok(true));

        assertThat(dashboardPermisosHelper.canDesign(1L, 2L, 3L)).isTrue();
    }

    @Test
    @DisplayName("canDesign retorna true si té permís PERM1 sobre l'ENTORN_APP associat")
    void canDesign_quanTePermisEntornApp_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(aclServiceClient.anyPermissionGranted(eq(ResourceType.DASHBOARD), eq(1L), eq(List.of(PermissionEnum.WRITE)), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(false));
        when(aclServiceClient.anyPermissionGranted(eq(ResourceType.APP), eq(2L), eq(List.of(PermissionEnum.PERM1)), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(false));

        EntornApp ea = new EntornApp();
        ea.setId(99L);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 3L)).thenReturn(ea);
        when(aclServiceClient.anyPermissionGranted(eq(ResourceType.ENTORN_APP), eq(99L), eq(List.of(PermissionEnum.PERM1)), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(true));

        assertThat(dashboardPermisosHelper.canDesign(1L, 2L, 3L)).isTrue();
    }

    @Test
    @DisplayName("canDesign retorna false si no té cap permís")
    void canDesign_quanSensePermisos_retornaFalse() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(aclServiceClient.anyPermissionGranted(any(), any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(false));
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 3L)).thenReturn(null);

        assertThat(dashboardPermisosHelper.canDesign(1L, 2L, 3L)).isFalse();
    }

    @Test
    @DisplayName("canCreate delega correctament a canDesign sense dashboardId")
    void canCreate_quanSensePermisos_retornaFalse() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(aclServiceClient.anyPermissionGranted(any(), any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(false));

        assertThat(dashboardPermisosHelper.canCreate(2L, 3L)).isFalse();
    }

    @Test
    @DisplayName("canDesignDashboard retorna false si dashboardId és null")
    void canDesignDashboard_quanIdNull_retornaFalse() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        assertThat(dashboardPermisosHelper.canDesignDashboard(null)).isFalse();
    }

    @Test
    @DisplayName("canDesignDashboard retorna false si el dashboard no existeix")
    void canDesignDashboard_quanNoExisteix_retornaFalse() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(dashboardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThat(dashboardPermisosHelper.canDesignDashboard(10L)).isFalse();
    }

    @Test
    @DisplayName("canDesignDashboard retorna true quan l'usuari pot dissenyar el dashboard existent")
    void canDesignDashboard_quanPotDissenyar_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setId(10L);
        dashboard.setAppId(2L);
        dashboard.setEntornId(3L);
        when(dashboardRepository.findById(10L)).thenReturn(Optional.of(dashboard));
        when(aclServiceClient.anyPermissionGranted(eq(ResourceType.DASHBOARD), eq(10L), eq(List.of(PermissionEnum.WRITE)), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(true));

        assertThat(dashboardPermisosHelper.canDesignDashboard(10L)).isTrue();
    }

    // ========================================================================
    // 4. GUÀRDIES AMB EXCEPCIONS
    // ========================================================================

    @Test
    @DisplayName("checkCanDesignDashboard llança AccessDeniedException si no es pot dissenyar")
    void checkCanDesignDashboard_quanDenegat_llancaExcepcio() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(dashboardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardPermisosHelper.checkCanDesignDashboard(10L, "Accés denegat"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Accés denegat");
    }

    @Test
    @DisplayName("checkCanDesignDashboard no llança excepció si es permet")
    void checkCanDesignDashboard_quanPermes_noLlancaExcepcio() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);
        dashboardPermisosHelper.checkCanDesignDashboard(10L, "Accés denegat");
    }

    @Test
    @DisplayName("checkCanDesign llança AccessDeniedException si no es pot dissenyar")
    void checkCanDesign_quanDenegat_llancaExcepcio() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(aclServiceClient.anyPermissionGranted(any(), any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(false));

        assertThatThrownBy(() -> dashboardPermisosHelper.checkCanDesign(1L, 2L, 3L, "Accés denegat"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Accés denegat");
    }

    @Test
    @DisplayName("checkCanCreate llança AccessDeniedException si no es pot crear")
    void checkCanCreate_quanDenegat_llancaExcepcio() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(aclServiceClient.anyPermissionGranted(any(), any(), any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(false));

        assertThatThrownBy(() -> dashboardPermisosHelper.checkCanCreate(2L, 3L, "Accés denegat crear"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Accés denegat crear");
    }

    // ========================================================================
    // 5. CONSTRUCCIÓ DE FILTRES SPRING RSQL
    // ========================================================================

    @Test
    @DisplayName("buildEntornAppFilter retorna null si el conjunt és null o buit")
    void buildEntornAppFilter_quanConjuntBuit_retornaNull() {
        assertThat(dashboardPermisosHelper.buildEntornAppFilter(null, null)).isNull();
        assertThat(dashboardPermisosHelper.buildEntornAppFilter(Collections.emptySet(), "prefix")).isNull();
    }

    @Test
    @DisplayName("buildEntornAppFilter construeix clàusula sense prefix")
    void buildEntornAppFilter_sensePrefix_construeixClausula() {
        EntornApp ea = new EntornApp();
        ea.setApp(AppRef.builder().id(10L).build());
        ea.setEntorn(EntornRef.builder().id(20L).build());
        when(estadisticaClientHelper.entornAppFindById(1L)).thenReturn(ea);

        String filter = dashboardPermisosHelper.buildEntornAppFilter(Set.of(1L), null);
        assertThat(filter).isEqualTo("(appId:10 and entornId:20)");
    }

    @Test
    @DisplayName("buildEntornAppFilter construeix clàusula amb prefix de propietat")
    void buildEntornAppFilter_ambPrefix_construeixClausulaAmbPrefix() {
        EntornApp ea = new EntornApp();
        ea.setApp(AppRef.builder().id(10L).build());
        ea.setEntorn(EntornRef.builder().id(20L).build());
        when(estadisticaClientHelper.entornAppFindById(1L)).thenReturn(ea);

        String filter = dashboardPermisosHelper.buildEntornAppFilter(Set.of(1L), "dashboard");
        assertThat(filter).isEqualTo("(dashboard.appId:10 and dashboard.entornId:20)");
    }

    @Test
    @DisplayName("buildEntornAppFilter construeix clàusula amb propietats personalitzades per a app i entorn")
    void buildEntornAppFilter_ambPropietatsPersonalitzades_construeixClausula() {
        EntornApp ea = new EntornApp();
        ea.setApp(AppRef.builder().id(10L).build());
        ea.setEntorn(EntornRef.builder().id(20L).build());
        when(estadisticaClientHelper.entornAppFindById(1L)).thenReturn(ea);

        String filter = dashboardPermisosHelper.buildEntornAppFilter(Set.of(1L), "widget.appId", "entornId");
        assertThat(filter).isEqualTo("(widget.appId:10 and entornId:20)");
    }

    @Test
    @DisplayName("buildEntornAppFilter gestiona entrades invàlides o errors sense fallar")
    void buildEntornAppFilter_quanErrorResolvent_ometEntrada() {
        when(estadisticaClientHelper.entornAppFindById(999L)).thenThrow(new RuntimeException("Not found"));

        String filter = dashboardPermisosHelper.buildEntornAppFilter(Set.of(999L), null);
        assertThat(filter).isNull();
    }
}
