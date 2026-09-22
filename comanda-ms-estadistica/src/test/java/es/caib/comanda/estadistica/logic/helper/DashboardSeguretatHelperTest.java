package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.intf.model.consulta.SeguretatDadesResultat;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.EntitatValorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.EntitatRepository;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardSeguretatHelper")
class DashboardSeguretatHelperTest {

    @Mock private AuthenticationHelper authenticationHelper;
    @Mock private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock private AclServiceClient aclServiceClient;
    @Mock private DimensioRepository dimensioRepository;
    @Mock private EntitatRepository entitatRepository;
    @Mock private UnitatOrganitzativaRepository unitatOrganitzativaRepository;
    @Mock private OrganitzativaTreeHelper organitzativaTreeHelper;

    @InjectMocks
    private DashboardSeguretatHelper dashboardSeguretatHelper;

    @Test
    @DisplayName("isExempt: retorna true quan l'usuari té rol ADMIN")
    void isExempt_quanTeRolAdmin_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);
        assertThat(dashboardSeguretatHelper.isExempt()).isTrue();
    }

    @Test
    @DisplayName("isExempt: retorna true quan l'usuari té rol CONSULTA")
    void isExempt_quanTeRolConsulta_retornaTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(true);
        assertThat(dashboardSeguretatHelper.isExempt()).isTrue();
    }

    @Test
    @DisplayName("isExempt: retorna false quan l'usuari no té cap dels rols exempts")
    void isExempt_quanNoTeCapDelsDosRols_retornaFalse() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);
        assertThat(dashboardSeguretatHelper.isExempt()).isFalse();
    }

    @Test
    @DisplayName("resoldreEntitatsPermeses: retorna null si l'usuari és exempt")
    void resoldreEntitatsPermeses_quanEsExempt_retornaNull() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        assertThat(dashboardSeguretatHelper.resoldreEntitatsPermeses()).isNull();
        verify(aclServiceClient, never()).findIdsWithAnyPermission(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("resoldreEntitatsPermeses: retorna llista buida si l'usuari no té permisos d'entitat")
    void resoldreEntitatsPermeses_quanNoTePermisos_retornaLlistaBuida() {
        mockNonExemptUser();
        mockAclResponse(ResourceType.ENTITAT, Collections.emptySet());

        List<EntitatEntity> result = dashboardSeguretatHelper.resoldreEntitatsPermeses();

        assertThat(result).isEmpty();
        verify(entitatRepository, never()).findAllById(anySet());
    }

    @Test
    @DisplayName("resoldreEntitatsPermeses: retorna les entitats permeses si l'usuari en té")
    void resoldreEntitatsPermeses_quanTePermisos_retornaLlistaEntitats() {
        mockNonExemptUser();
        Set<Serializable> ids = new HashSet<>(Arrays.asList(1L, 2L));
        mockAclResponse(ResourceType.ENTITAT, ids);

        EntitatEntity e1 = new EntitatEntity(); e1.setId(1L);
        EntitatEntity e2 = new EntitatEntity(); e2.setId(2L);
        when(entitatRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(e1, e2));

        List<EntitatEntity> result = dashboardSeguretatHelper.resoldreEntitatsPermeses();

        assertThat(result).hasSize(2).containsExactlyInAnyOrder(e1, e2);
    }

    @Test
    @DisplayName("resoldre: retorna exempt=true si l'usuari és exempt")
    void resoldre_quanEsExempt_retornaExemptTrue() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        SeguretatDadesResultat result = dashboardSeguretatHelper.resoldre(1L);

        assertThat(result.isExempt()).isTrue();
        assertThat(result.isSensePermisos()).isFalse();
        verify(aclServiceClient, never()).findIdsWithAnyPermission(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("resoldre: retorna sensePermisos=true si l'usuari no té cap permís (llista buida)")
    void resoldre_quanNoTeCapPermis_retornaSensePermisosTrue() {
        mockNonExemptUser();
        mockAclResponse(ResourceType.ENTITAT, Collections.emptySet());
        mockAclResponse(ResourceType.UNITAT, Collections.emptySet());

        SeguretatDadesResultat result = dashboardSeguretatHelper.resoldre(1L);

        assertThat(result.isSensePermisos()).isTrue();
        assertThat(result.isExempt()).isFalse();
    }

    @Test
    @DisplayName("resoldre: retorna exempt=true si l'app no té dimensions ENTITAT ni ORGAN_GESTOR")
    void resoldre_quanAppNoTeDimensionsAplicables_retornaExemptTrue() {
        mockNonExemptUser();
        Set<Serializable> ids = new HashSet<>(Arrays.asList(1L));
        mockAclResponse(ResourceType.ENTITAT, ids);
        mockAclResponse(ResourceType.UNITAT, Collections.emptySet());

        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ENTITAT)).thenReturn(Optional.empty());
        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ORGAN_GESTOR)).thenReturn(Optional.empty());

        SeguretatDadesResultat result = dashboardSeguretatHelper.resoldre(1L);

        assertThat(result.isExempt()).isTrue();
    }

    @Test
    @DisplayName("resoldre: aplica filtre d'entitats usant CODI_DIR3 quan la dimensió ho indica")
    void resoldre_quanDimensioEntitatUsaCodiDir3_retornaFiltreAmbCodiDir3() {
        mockNonExemptUser();
        Set<Serializable> entitatIds = new HashSet<>(Arrays.asList(10L));
        mockAclResponse(ResourceType.ENTITAT, entitatIds);
        mockAclResponse(ResourceType.UNITAT, Collections.emptySet());

        DimensioEntity dimensioEntitat = new DimensioEntity();
        dimensioEntitat.setCodi("DIM_ENT");
        dimensioEntitat.setEntitatValorTipus(EntitatValorTipus.CODI_DIR3);
        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ENTITAT)).thenReturn(Optional.of(dimensioEntitat));
        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ORGAN_GESTOR)).thenReturn(Optional.empty());

        EntitatEntity entitat = new EntitatEntity();
        entitat.setId(10L);
        entitat.setCodiDir3("DIR3_001");
        entitat.setCodi("CODI_001");
        when(entitatRepository.findAllById(List.of(10L))).thenReturn(List.of(entitat));

        SeguretatDadesResultat result = dashboardSeguretatHelper.resoldre(1L);

        assertThat(result.isExempt()).isFalse();
        assertThat(result.getFiltreSql()).isNotNull();
        assertThat(result.getFiltreSql().getDimensioEntitatCodi()).isEqualTo("DIM_ENT");
        assertThat(result.getFiltreSql().getValorsEntitatPermesos()).containsExactly("DIR3_001");
    }

    @Test
    @DisplayName("resoldre: aplica filtre d'entitats usant getCodi() quan EntitatValorTipus NO és CODI_DIR3")
    void resoldre_quanDimensioEntitatNoUsaCodiDir3_retornaFiltreAmbCodiNormal() {
        mockNonExemptUser();
        Set<Serializable> entitatIds = new HashSet<>(Arrays.asList(10L));
        mockAclResponse(ResourceType.ENTITAT, entitatIds);
        mockAclResponse(ResourceType.UNITAT, Collections.emptySet());

        DimensioEntity dimensioEntitat = new DimensioEntity();
        dimensioEntitat.setCodi("DIM_ENT");
        dimensioEntitat.setEntitatValorTipus(null);
        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ENTITAT)).thenReturn(Optional.of(dimensioEntitat));
        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ORGAN_GESTOR)).thenReturn(Optional.empty());

        EntitatEntity entitat = new EntitatEntity();
        entitat.setId(10L);
        entitat.setCodiDir3("DIR3_001");
        entitat.setCodi("CODI_001");
        when(entitatRepository.findAllById(List.of(10L))).thenReturn(List.of(entitat));

        SeguretatDadesResultat result = dashboardSeguretatHelper.resoldre(1L);

        assertThat(result.getFiltreSql().getValorsEntitatPermesos()).containsExactly("CODI_001");
    }

    @Test
    @DisplayName("resoldre: aplica filtre d'òrgans incloent-hi descendents")
    void resoldre_quanTePermisosUnitat_retornaFiltreSqlAmbOrgansIDescendents() {
        mockNonExemptUser();
        mockAclResponse(ResourceType.ENTITAT, Collections.emptySet());
        Set<Serializable> unitatIds = new HashSet<>(Arrays.asList(100L));
        mockAclResponse(ResourceType.UNITAT, unitatIds);

        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ENTITAT)).thenReturn(Optional.empty());
        DimensioEntity dimensioOrgan = new DimensioEntity();
        dimensioOrgan.setCodi("DIM_ORG");
        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ORGAN_GESTOR)).thenReturn(Optional.of(dimensioOrgan));

        UnitatOrganitzativaEntity uo = new UnitatOrganitzativaEntity();
        uo.setId(100L);
        when(unitatOrganitzativaRepository.findAllById(List.of(100L))).thenReturn(List.of(uo));

        Set<String> descendents = new LinkedHashSet<>(Arrays.asList("ORG_100", "ORG_101"));
        when(organitzativaTreeHelper.getDescendentsIElMateix(List.of(uo))).thenReturn(descendents);

        SeguretatDadesResultat result = dashboardSeguretatHelper.resoldre(1L);

        assertThat(result.getFiltreSql()).isNotNull();
        assertThat(result.getFiltreSql().getDimensioOrganCodi()).isEqualTo("DIM_ORG");
        assertThat(result.getFiltreSql().getValorsOrganPermesos()).containsExactlyInAnyOrder("ORG_100", "ORG_101");
    }

    @Test
    @DisplayName("resoldre: combina filtre d'entitats i òrgans si l'usuari té permisos d'ambdós")
    void resoldre_quanTePermisosAmbdos_retornaFiltreSqlCombinat() {
        mockNonExemptUser();
        Set<Serializable> entitatIds = new HashSet<>(Arrays.asList(10L));
        Set<Serializable> unitatIds = new HashSet<>(Arrays.asList(100L));
        mockAclResponse(ResourceType.ENTITAT, entitatIds);
        mockAclResponse(ResourceType.UNITAT, unitatIds);

        DimensioEntity dimensioEntitat = new DimensioEntity();
        dimensioEntitat.setCodi("DIM_ENT");
        dimensioEntitat.setEntitatValorTipus(null);
        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ENTITAT)).thenReturn(Optional.of(dimensioEntitat));

        DimensioEntity dimensioOrgan = new DimensioEntity();
        dimensioOrgan.setCodi("DIM_ORG");
        when(dimensioRepository.findByEntornAppIdAndTipus(1L, TipusDimensioEnum.ORGAN_GESTOR)).thenReturn(Optional.of(dimensioOrgan));

        EntitatEntity entitat = new EntitatEntity();
        entitat.setId(10L);
        entitat.setCodi("CODI_001");
        when(entitatRepository.findAllById(List.of(10L))).thenReturn(List.of(entitat));

        UnitatOrganitzativaEntity uo = new UnitatOrganitzativaEntity();
        uo.setId(100L);
        when(unitatOrganitzativaRepository.findAllById(List.of(100L))).thenReturn(List.of(uo));
        when(organitzativaTreeHelper.getDescendentsIElMateix(List.of(uo))).thenReturn(new LinkedHashSet<>(List.of("ORG_100")));

        SeguretatDadesResultat result = dashboardSeguretatHelper.resoldre(1L);

        assertThat(result.getFiltreSql()).isNotNull();
        assertThat(result.getFiltreSql().getDimensioEntitatCodi()).isEqualTo("DIM_ENT");
        assertThat(result.getFiltreSql().getValorsEntitatPermesos()).containsExactly("CODI_001");
        assertThat(result.getFiltreSql().getDimensioOrganCodi()).isEqualTo("DIM_ORG");
        assertThat(result.getFiltreSql().getValorsOrganPermesos()).containsExactly("ORG_100");
    }

    @Test
    @DisplayName("resoldre: gestiona correctament quan el AclServiceClient retorna null (cos de la resposta buit)")
    void resoldre_quanAclRetornaNull_retornaSetBuit() {
        mockNonExemptUser();

        when(aclServiceClient.findIdsWithAnyPermission(
            eq(ResourceType.ENTITAT),
            eq(List.of(PermissionEnum.PERM0)),
            anyString(),
            anyList(),
            any()
        )).thenReturn(ResponseEntity.ok(null));

        when(aclServiceClient.findIdsWithAnyPermission(
            eq(ResourceType.UNITAT),
            eq(List.of(PermissionEnum.PERM0)),
            anyString(),
            anyList(),
            any()
        )).thenReturn(ResponseEntity.ok(null));

        SeguretatDadesResultat result = dashboardSeguretatHelper.resoldre(1L);

        assertThat(result.isSensePermisos()).isTrue();
    }

    private void mockNonExemptUser() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);
        when(authenticationHelper.getCurrentUserName()).thenReturn("user_test");
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{"ROLE_USER"});
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer dummy_token");
    }

    private void mockAclResponse(ResourceType resourceType, Set<Serializable> ids) {
        when(aclServiceClient.findIdsWithAnyPermission(
            eq(resourceType),
            eq(List.of(PermissionEnum.PERM0)),
            anyString(),
            anyList(),
            any()
        )).thenReturn(ResponseEntity.ok(ids));
    }
}
