package es.caib.comanda.salut.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.*;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ObjectMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.salut.logic.helper.MetricsHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutEstatHelper;
import es.caib.comanda.salut.logic.intf.model.*;
import es.caib.comanda.salut.logic.intf.model.Salut;
import es.caib.comanda.salut.logic.intf.model.SalutDetall;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutIntegracio;
import es.caib.comanda.salut.logic.intf.model.SalutSubsistema;
import es.caib.comanda.salut.persist.entity.*;
import es.caib.comanda.salut.persist.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a SalutServiceImpl")
class SalutServiceImplTest {

    @Mock private SalutIntegracioRepository salutIntegracioRepository;
    @Mock private SalutSubsistemaRepository salutSubsistemaRepository;
    @Mock private SalutMissatgeRepository salutMissatgeRepository;
    @Mock private SalutDetallRepository salutDetallRepository;
    @Mock private SalutHistRepository salutHistRepository;
    @Mock private SalutClientHelper salutClientHelper;
    @Mock private MetricsHelper metricsHelper;
    @Mock private AuthenticationHelper authenticationHelper;
    @Mock private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock private AclServiceClient aclServiceClient;
    @Mock private SalutEstatHelper salutEstatHelper;
    @Mock private SalutRepository salutRepository;
    @Mock private ObjectMappingHelper objectMappingHelper;
    @Mock private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @InjectMocks
    private SalutServiceImpl salutService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(salutService, "entityRepository", salutRepository);
        ReflectionTestUtils.setField(salutService, "objectMappingHelper", objectMappingHelper);
        ReflectionTestUtils.setField(salutService, "resourceEntityMappingHelper", resourceEntityMappingHelper);
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        lenient().when(authenticationHelper.getCurrentUserName()).thenReturn("user");
        lenient().when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{"ROLE_USER"});
    }

    // ========================================================================
    // 1. TESTOS PER A netejaPerEntornApp
    // ========================================================================

    @Test
    @DisplayName("netejaPerEntornApp: quan hi ha saluts, esborra totes les entitats relacionades")
    void netejaPerEntornApp_quanHiHaSaluts_llavorsEsborraRelacionats() {
        // Arrange
        Long entornAppId = 1L;
        List<Long> salutIds = List.of(10L, 20L);
        when(salutRepository.findIdsByEntornAppId(entornAppId)).thenReturn(salutIds);

        // Act
        salutService.netejaPerEntornApp(entornAppId);

        // Assert
        verify(salutIntegracioRepository, times(1)).deleteAllBySalutIdIn(salutIds);
        verify(salutSubsistemaRepository, times(1)).deleteAllBySalutIdIn(salutIds);
        verify(salutMissatgeRepository, times(1)).deleteAllBySalutIdIn(salutIds);
        verify(salutDetallRepository, times(1)).deleteAllBySalutIdIn(salutIds);
        verify(salutRepository, times(1)).deleteAllByIdInBatch(salutIds);
        verify(salutHistRepository, times(1)).deleteByEntornAppId(entornAppId);
    }

    @Test
    @DisplayName("netejaPerEntornApp: quan no hi ha saluts, només esborra els històrics")
    void netejaPerEntornApp_quanNoHiHaSaluts_llavorsNomésEsborraHistorics() {
        // Arrange
        Long entornAppId = 1L;
        when(salutRepository.findIdsByEntornAppId(entornAppId)).thenReturn(Collections.emptyList());

        // Act
        salutService.netejaPerEntornApp(entornAppId);

        // Assert
        verify(salutIntegracioRepository, never()).deleteAllBySalutIdIn(any());
        verify(salutHistRepository, times(1)).deleteByEntornAppId(entornAppId);
    }

    // ========================================================================
    // 2. TESTOS PER A additionalSpringFilter
    // ========================================================================

    @Test
    @DisplayName("additionalSpringFilter: retorna null quan l'usuari és ADMIN")
    void additionalSpringFilter_quanEsAdmin_llavorsRetornaNull() {
        // Arrange
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        // Act
        String result = salutService.additionalSpringFilter("filter", new String[0]);

        // Assert
        assertThat(result).isNull();
        verify(aclServiceClient, never()).findIdsWithAnyPermission(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("additionalSpringFilter: retorna null quan l'usuari té rol CONSULTA")
    void additionalSpringFilter_quanEsConsulta_llavorsRetornaNull() {
        // Arrange
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(true);

        // Act
        String result = salutService.additionalSpringFilter("filter", new String[0]);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("additionalSpringFilter: retorna filtre zero quan no té permisos d'App ni d'EntornApp")
    void additionalSpringFilter_quanNoTePermisos_llavorsRetornaFiltreZero() {
        // Arrange
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);
        when(aclServiceClient.findIdsWithAnyPermission(any(), any(), any(), any(), anyString()))
            .thenReturn(ResponseEntity.ok(Collections.emptySet()));

        // Act
        String result = salutService.additionalSpringFilter("filter", new String[0]);

        // Assert
        assertThat(result).isEqualTo("entornAppId:0");
    }

    @Test
    @DisplayName("additionalSpringFilter: retorna filtre mergejat quan té permisos d'App")
    void additionalSpringFilter_quanTePermisosApp_llavorsRetornaFiltreMerged() {
        // Arrange
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);

        Set<Serializable> appPerms = Set.of(1L);
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.APP), anyList(), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(appPerms));
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.ENTORN_APP), anyList(), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(Collections.emptySet()));

        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        when(salutClientHelper.entornAppFindByActivaTrue("app.id:1")).thenReturn(List.of(entornApp));

        // Act
        String result = salutService.additionalSpringFilter("filter", new String[0]);

        // Assert
        assertThat(result).isEqualTo("entornAppId:10");
    }

    // ========================================================================
    // 3. TESTOS PER A Perspectives
    // ========================================================================

    @Test
    @DisplayName("PerspectiveUltimEstatOperatiuInfo: no fa res quan no hi ha històric")
    void perspectiveUltimEstatOperatiuInfo_quanNoHiHaHistoric_llavorsNoFaRes() {
        // Arrange
        SalutServiceImpl.PerspectiveUltimEstatOperatiuInfo perspective = salutService.new PerspectiveUltimEstatOperatiuInfo();
        SalutEntity entity = new SalutEntity();
        entity.setEntornAppId(1L);
        Salut resource = new Salut();

        when(salutHistRepository.findTopByEntornAppIdOrderByDataDescIdDesc(1L)).thenReturn(null);

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getUltimEstatInfo()).isNull();
    }

    @Test
    @DisplayName("PerspectiveUltimEstatOperatiuInfo: no fa res quan l'estat actual és estable")
    void perspectiveUltimEstatOperatiuInfo_quanEstatEsEstable_llavorsNoFaRes() {
        // Arrange
        SalutServiceImpl.PerspectiveUltimEstatOperatiuInfo perspective = salutService.new PerspectiveUltimEstatOperatiuInfo();
        SalutEntity entity = new SalutEntity();
        entity.setEntornAppId(1L);
        Salut resource = new Salut();

        SalutHistEntity historic = new SalutHistEntity();
        historic.setAppEstat(SalutEstat.UP); // Estat estable
        when(salutHistRepository.findTopByEntornAppIdOrderByDataDescIdDesc(1L)).thenReturn(historic);

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getUltimEstatInfo()).isNull();
    }

    @Test
    @DisplayName("PerspectiveUltimEstatOperatiuInfo: assigna info quan l'estat és inestable i hi ha un anterior estable")
    void perspectiveUltimEstatOperatiuInfo_quanEstatInestableIAnteriorEstable_llavorsAssignaInfo() {
        // Arrange
        SalutServiceImpl.PerspectiveUltimEstatOperatiuInfo perspective = salutService.new PerspectiveUltimEstatOperatiuInfo();
        SalutEntity entity = new SalutEntity();
        entity.setEntornAppId(1L);
        Salut resource = new Salut();

        SalutHistEntity historicInestable = new SalutHistEntity();
        historicInestable.setAppEstat(SalutEstat.DOWN);
        historicInestable.setData(LocalDateTime.now());

        SalutHistEntity historicEstable = new SalutHistEntity();
        historicEstable.setAppEstat(SalutEstat.UP);
        historicEstable.setData(LocalDateTime.now().minusHours(1));

        when(salutHistRepository.findTopByEntornAppIdOrderByDataDescIdDesc(1L)).thenReturn(historicInestable);
        when(salutHistRepository.findTopByEntornAppIdAndAppEstatInOrderByDataDesc(eq(1L), anyList())).thenReturn(Optional.of(historicEstable));
        when(salutHistRepository.findSeguentData(eq(1L), any())).thenReturn(Optional.of(LocalDateTime.now().minusMinutes(30)));

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getUltimEstatInfo()).isNotNull();
        assertThat(resource.getUltimEstatInfo().getEstat()).isEqualTo(SalutEstat.UP);
    }

    @Test
    @DisplayName("PerspectiveDetalls: applyMultiple aplica correctament a totes les entitats")
    void perspectiveDetalls_applyMultiple_quanHiHaEntitats_llavorsAplicaSingle() throws PerspectiveApplicationException {
        // Arrange
        SalutServiceImpl.PerspectiveDetalls perspective = salutService.new PerspectiveDetalls();

        SalutEntity entity = new SalutEntity();
        entity.setId(1L);
        Salut resource = new Salut();
        resource.setId(1L);

        List<SalutEntity> entities = List.of(entity);
        List<Salut> resources = List.of(resource);

        when(salutDetallRepository.findBySalut(entity)).thenReturn(Collections.emptyList());

        // Act
        boolean result = perspective.applyMultiple("CODE", entities, resources);

        // Assert
        assertThat(result).isTrue();
        verify(salutDetallRepository, times(1)).findBySalut(entity);
    }

    // ========================================================================
    // 4. TESTOS PER A Report Generators
    // ========================================================================

    @Test
    @DisplayName("InformeEstat: delega correctament a SalutEstatHelper")
    void informeEstat_generateData_quanEsCrida_llavorsDelegaASalutEstatHelper() {
        // Arrange
        SalutServiceImpl.InformeEstat generator = salutService.new InformeEstat();
        SalutInformeParams params = new SalutInformeParams();
        params.setEntornAppId(1L);
        params.setAgrupacio(SalutInformeAgrupacio.HORA);
        params.setDataReferencia(LocalDateTime.now());

        when(salutEstatHelper.mapTipusAgrupacio(SalutInformeAgrupacio.HORA)).thenReturn(TipusRegistreSalut.HORA);
        when(salutEstatHelper.getDataIniciAjustada(any(), any())).thenReturn(LocalDateTime.now().minusHours(1));
        when(salutEstatHelper.generateEstatList(any(), any(), eq(1L))).thenReturn(Collections.emptyList());

        // Act
        List<SalutInformeEstatItem> result = generator.generateData("CODE", new SalutEntity(), params);

        // Assert
        assertThat(result).isEmpty();
        verify(salutEstatHelper, times(1)).generateEstatList(any(), any(), eq(1L));
    }

    @Test
    @DisplayName("InformeGrupsDates: genera correctament els grups de dates")
    void informeGrupsDates_generateData_quanEsCrida_llavorsGeneraGrups() {
        // Arrange
        SalutServiceImpl.InformeGrupsDates generator = salutService.new InformeGrupsDates();
        SalutInformeGrupsParams params = new SalutInformeGrupsParams();
        params.setAgrupacio(SalutInformeAgrupacio.HORA);
        params.setDataReferencia(LocalDateTime.now());

        LocalDateTime dataInici = LocalDateTime.now().minusHours(1);
        when(salutEstatHelper.getDataIniciAjustada(any(), any())).thenReturn(dataInici);
        when(salutEstatHelper.generarGrupsDates(eq(dataInici), eq(SalutInformeAgrupacio.HORA)))
            .thenReturn(List.of(dataInici, dataInici.plusHours(1)));

        // Act
        List<SalutInformeGrupItem> result = generator.generateData("CODE", new SalutEntity(), params);

        // Assert
        assertThat(result).hasSize(2);
        verify(salutEstatHelper, times(1)).generarGrupsDates(eq(dataInici), eq(SalutInformeAgrupacio.HORA));
    }

    @Test
    @DisplayName("InformeEstats: genera un mapa amb les llistes per a cada entornAppId")
    void informeEstats_generateData_quanEsCrida_llavorsGeneraMapaPerEntornApp() {
        // Arrange
        SalutServiceImpl.InformeEstats generator = salutService.new InformeEstats();
        SalutInformeLlistatParams params = new SalutInformeLlistatParams();
        params.setAgrupacio(SalutInformeAgrupacio.HORA);
        params.setDataReferencia(LocalDateTime.now());
        params.setEntornAppIdList(List.of(1L, 2L));

        when(salutEstatHelper.mapTipusAgrupacio(any())).thenReturn(TipusRegistreSalut.HORA);
        when(salutEstatHelper.getDataIniciAjustada(any(), any())).thenReturn(LocalDateTime.now().minusHours(1));
        when(salutEstatHelper.generateEstatList(any(), any(), anyLong())).thenReturn(Collections.emptyList());

        // Act
        List<HashMap<String, Object>> result = generator.generateData("CODE", new SalutEntity(), params);

        // Assert
        assertThat(result).hasSize(1);
        HashMap<String, Object> map = result.get(0);
        assertThat(map).containsKeys("1", "2");
        verify(salutEstatHelper, times(2)).generateEstatList(any(), any(), anyLong());
    }

    // ========================================================================
    // 5. TESTOS PER A Perspectives (Faltants)
    // ========================================================================

    @Test
    @DisplayName("PerspectiveIntegracions: aplica correctament les integracions i els seus noms/logos")
    void perspectiveIntegracions_applySingle_quanHiHaDades_llavorsAplicaCorrectament() throws PerspectiveApplicationException {
        // Arrange
        SalutServiceImpl.PerspectiveIntegracions perspective = salutService.new PerspectiveIntegracions();
        SalutEntity entity = new SalutEntity();
        entity.setEntornAppId(1L);
        Salut resource = new Salut();
        resource.setIntegracions(new ArrayList<>());

        SalutIntegracioEntity integracioEntity = new SalutIntegracioEntity();
        integracioEntity.setCodi("INT1");
        when(salutIntegracioRepository.findBySalutOrderByCodiAsc(entity)).thenReturn(List.of(integracioEntity));

        SalutIntegracio integracioResource = new SalutIntegracio();
        integracioResource.setCodi("INT1");
        when(objectMappingHelper.newInstanceMap(any(), eq(SalutIntegracio.class), eq("salut"))).thenReturn(integracioResource);

        EntornApp entornApp = new EntornApp();
        AppIntegracio integracio = new AppIntegracio();
        ReflectionTestUtils.setField(integracio, "codi", "INT1");
        IntegracioRef integracioRef = new IntegracioRef();
        ReflectionTestUtils.setField(integracioRef, "nom", "Nom Integracio");
        ReflectionTestUtils.setField(integracio, "integracio", integracioRef);
        entornApp.setIntegracions(List.of(integracio));
        when(salutClientHelper.entornAppFindByIdWithIntegracionsSubsistemesContexts(1L)).thenReturn(entornApp);

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getIntegracions()).hasSize(1);
        assertThat(resource.getIntegracions().get(0).getNom()).isEqualTo("Nom Integracio");
    }

    @Test
    @DisplayName("PerspectiveSubsistemes: aplica correctament els subsistemes i els seus noms")
    void perspectiveSubsistemes_applySingle_quanHiHaDades_llavorsAplicaCorrectament() throws PerspectiveApplicationException {
        // Arrange
        SalutServiceImpl.PerspectiveSubsistemes perspective = salutService.new PerspectiveSubsistemes();
        SalutEntity entity = new SalutEntity();
        entity.setEntornAppId(1L);
        Salut resource = new Salut();
        resource.setSubsistemes(new ArrayList<>());

        SalutSubsistemaEntity subsistemaEntity = new SalutSubsistemaEntity();
        subsistemaEntity.setCodi("SUB1");
        when(salutSubsistemaRepository.findBySalutOrderByCodiAsc(entity)).thenReturn(List.of(subsistemaEntity));

        SalutSubsistema subsistemaResource = new SalutSubsistema();
        subsistemaResource.setCodi("SUB1");
        when(objectMappingHelper.newInstanceMap(any(), eq(SalutSubsistema.class), eq("salut"))).thenReturn(subsistemaResource);

        EntornApp entornApp = new EntornApp();
        AppSubsistema subsistema = new AppSubsistema();
        ReflectionTestUtils.setField(subsistema, "codi", "SUB1");
        ReflectionTestUtils.setField(subsistema, "nom", "Nom Subsistema");
        entornApp.setSubsistemes(List.of(subsistema));
        when(salutClientHelper.entornAppFindByIdWithIntegracionsSubsistemesContexts(1L)).thenReturn(entornApp);

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getSubsistemes()).hasSize(1);
        assertThat(resource.getSubsistemes().get(0).getNom()).isEqualTo("Nom Subsistema");
    }

    @Test
    @DisplayName("PerspectiveContexts: aplica correctament els contextos")
    void perspectiveContexts_applySingle_quanHiHaDades_llavorsAplicaCorrectament() throws PerspectiveApplicationException {
        // Arrange
        SalutServiceImpl.PerspectiveContexts perspective = salutService.new PerspectiveContexts();
        SalutEntity entity = new SalutEntity();
        entity.setEntornAppId(1L);
        Salut resource = new Salut();

        EntornApp entornApp = new EntornApp();
        AppContext appContext1 = new AppContext();
        ReflectionTestUtils.setField(appContext1, "codi", "CTX1");
        AppContext appContext2 = new AppContext();
        ReflectionTestUtils.setField(appContext2, "codi", "CTX2");
        entornApp.setContexts(List.of(appContext1, appContext2));
        when(salutClientHelper.entornAppFindByIdWithIntegracionsSubsistemesContexts(1L)).thenReturn(entornApp);

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getContexts()).containsExactly(appContext1, appContext2);
    }

    @Test
    @DisplayName("PerspectiveMissatges: aplica correctament els missatges")
    void perspectiveMissatges_applySingle_quanHiHaDades_llavorsAplicaCorrectament() throws PerspectiveApplicationException {
        // Arrange
        SalutServiceImpl.PerspectiveMissatges perspective = salutService.new PerspectiveMissatges();
        SalutEntity entity = new SalutEntity();
        Salut resource = new Salut();

        SalutMissatgeEntity missatgeEntity = new SalutMissatgeEntity();
        when(salutMissatgeRepository.findBySalut(entity)).thenReturn(List.of(missatgeEntity));
        when(objectMappingHelper.newInstanceMap(any(), eq(SalutMissatge.class), eq("salut"))).thenReturn(new SalutMissatge());

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getMissatges()).hasSize(1);
    }

    @Test
    @DisplayName("PerspectiveMissatges: no fa res quan els missatges són null")
    void perspectiveMissatges_applySingle_quanMissatgesSonNull_llavorsNoFaRes() throws PerspectiveApplicationException {
        // Arrange
        SalutServiceImpl.PerspectiveMissatges perspective = salutService.new PerspectiveMissatges();
        SalutEntity entity = new SalutEntity();
        Salut resource = new Salut();

        when(salutMissatgeRepository.findBySalut(entity)).thenReturn(null);

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getMissatges()).isNull();
    }

    @Test
    @DisplayName("PerspectiveDetalls: aplica correctament els detalls (applySingle)")
    void perspectiveDetalls_applySingle_quanHiHaDades_llavorsAplicaCorrectament() throws PerspectiveApplicationException {
        // Arrange
        SalutServiceImpl.PerspectiveDetalls perspective = salutService.new PerspectiveDetalls();
        SalutEntity entity = new SalutEntity();
        Salut resource = new Salut();

        SalutDetallEntity detallEntity = new SalutDetallEntity();
        when(salutDetallRepository.findBySalut(entity)).thenReturn(List.of(detallEntity));
        when(objectMappingHelper.newInstanceMap(any(), eq(SalutDetall.class), eq("salut"))).thenReturn(new SalutDetall());

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getDetalls()).hasSize(1);
    }

    @Test
    @DisplayName("PerspectiveDetalls: no fa res quan els detalls són null")
    void perspectiveDetalls_applySingle_quanDetallsSonNull_llavorsNoFaRes() throws PerspectiveApplicationException {
        // Arrange
        SalutServiceImpl.PerspectiveDetalls perspective = salutService.new PerspectiveDetalls();
        SalutEntity entity = new SalutEntity();
        Salut resource = new Salut();

        when(salutDetallRepository.findBySalut(entity)).thenReturn(null);

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getDetalls()).isNull();
    }

    @Test
    @DisplayName("PerspectiveHistorics: aplica correctament els històrics")
    void perspectiveHistorics_applySingle_quanHiHaDades_llavorsAplicaCorrectament() throws PerspectiveApplicationException {
        // Arrange
        SalutServiceImpl.PerspectiveHistorics perspective = salutService.new PerspectiveHistorics();
        SalutEntity entity = new SalutEntity();
        entity.setEntornAppId(1L);
        Salut resource = new Salut();

        SalutHistEntity historicEntity = new SalutHistEntity();
        when(salutHistRepository.findByEntornAppIdOrderByDataDescIdDesc(1L)).thenReturn(List.of(historicEntity));
        when(objectMappingHelper.newInstanceMap(any(), eq(SalutHist.class))).thenReturn(new SalutHist());

        // Act
        perspective.applySingle("CODE", entity, resource);

        // Assert
        assertThat(resource.getHistorics()).hasSize(1);
    }

    // ========================================================================
    // 6. TESTOS PER A Report Generators (Faltants)
    // ========================================================================

    @Test
    @DisplayName("InformeSalutLast: genera correctament l'informe de l'últim estat")
    void informeSalutLast_generateData_quanEsCrida_llavorsGeneraInforme() {
        // Arrange
        SalutServiceImpl.InformeSalutLast generator = salutService.new InformeSalutLast();
        SalutEntity entity = new SalutEntity();

        EntornApp entornApp = new EntornApp();
        entornApp.setId(1L);
        when(salutClientHelper.entornAppFindByActivaTrue(anyString())).thenReturn(List.of(entornApp));

        SalutEntity salutEntity = new SalutEntity();
        salutEntity.setId(1L);
        salutEntity.setEntornAppId(1L);
        when(salutRepository.informeSalutLast(eq(List.of(1L)), any())).thenReturn(List.of(salutEntity));

        // Mock dels timers de mètriques
        when(metricsHelper.getSalutLastEntornAppsTimer()).thenReturn(mock(io.micrometer.core.instrument.Timer.class));
        when(metricsHelper.getSalutLastDadesTimer()).thenReturn(mock(io.micrometer.core.instrument.Timer.class));
        when(metricsHelper.getSalutLastGlobalTimer()).thenReturn(mock(io.micrometer.core.instrument.Timer.class));

        Salut salut = new Salut();
        salut.setId(1L);
        when(resourceEntityMappingHelper.entityToResource(any(), any())).thenReturn(salut);

        // Act
        List<Salut> result = generator.generateData("CODE", entity, "filter");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(metricsHelper, times(1)).getSalutLastEntornAppsTimer();
        verify(metricsHelper, times(1)).getSalutLastDadesTimer();
        verify(metricsHelper, times(1)).getSalutLastGlobalTimer();
    }

    @Test
    @DisplayName("InformeSalutLast: retorna llista buida quan no hi ha saluts")
    void informeSalutLast_generateData_quanNoHiHaSaluts_llavorsRetornaBuit() {
        // Arrange
        SalutServiceImpl.InformeSalutLast generator = salutService.new InformeSalutLast();
        SalutEntity entity = new SalutEntity();

        when(salutClientHelper.entornAppFindByActivaTrue(anyString())).thenReturn(Collections.emptyList());
        when(salutRepository.informeSalutLast(any(), any())).thenReturn(null);

        when(metricsHelper.getSalutLastEntornAppsTimer()).thenReturn(mock(io.micrometer.core.instrument.Timer.class));
        when(metricsHelper.getSalutLastDadesTimer()).thenReturn(mock(io.micrometer.core.instrument.Timer.class));
        when(metricsHelper.getSalutLastGlobalTimer()).thenReturn(mock(io.micrometer.core.instrument.Timer.class));

        // Act
        List<Salut> result = generator.generateData("CODE", entity, "filter");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("InformeLatencia: genera correctament l'informe de latència")
    void informeLatencia_generateData_quanEsCrida_llavorsGeneraInforme() {
        // Arrange
        SalutServiceImpl.InformeLatencia generator = salutService.new InformeLatencia();
        SalutEntity entity = new SalutEntity();

        SalutInformeParams params = new SalutInformeParams();
        params.setEntornAppId(1L);
        params.setAgrupacio(SalutInformeAgrupacio.HORA);
        params.setDataReferencia(LocalDateTime.now());

        when(salutEstatHelper.mapTipusAgrupacio(SalutInformeAgrupacio.HORA)).thenReturn(TipusRegistreSalut.HORA);
        when(salutEstatHelper.getDataIniciAjustada(any(), any())).thenReturn(LocalDateTime.now().minusHours(1));

        SalutEntity salutEntity = new SalutEntity();
        salutEntity.setData(LocalDateTime.now());
        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(anyLong(), any(), any()))
            .thenReturn(List.of(salutEntity));

        // Act
        List<SalutInformeLatenciaItem> result = generator.generateData("CODE", entity, params);

        // Assert
        assertThat(result).hasSize(1);
    }
}
