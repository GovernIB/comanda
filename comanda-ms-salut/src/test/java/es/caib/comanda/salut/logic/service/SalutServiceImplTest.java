package es.caib.comanda.salut.logic.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.AppContext;
import es.caib.comanda.client.model.AppIntegracio;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.IntegracioRef;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.JasperReportsHelper;
import es.caib.comanda.ms.logic.helper.ObjectMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import es.caib.comanda.salut.logic.helper.MetricsHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.intf.model.Salut;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutInformeAgrupacio;
import es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem;
import es.caib.comanda.salut.logic.intf.model.SalutInformeGrupItem;
import es.caib.comanda.salut.logic.intf.model.SalutIntegracio;
import es.caib.comanda.salut.logic.intf.model.SalutMissatge;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.entity.SalutIntegracioEntity;
import es.caib.comanda.salut.persist.entity.SalutMissatgeEntity;
import es.caib.comanda.salut.persist.repository.*;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.noop.NoopTimer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static es.caib.comanda.salut.logic.helper.SalutInfoHelper.MINUTS_PER_AGRUPACIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalutServiceImplTest {

    static class TestableSalutServiceImpl extends SalutServiceImpl {
        public TestableSalutServiceImpl(SalutIntegracioRepository salutIntegracioRepository,
                                        SalutSubsistemaRepository salutSubsistemaRepository,
                                        SalutMissatgeRepository salutMissatgeRepository,
                                        SalutDetallRepository salutDetallRepository,
                                        SalutHistRepository salutHistRepository,
                                        SalutClientHelper salutClientHelper,
                                        MetricsHelper metricsHelper,
                                        AuthenticationHelper authenticationHelper,
                                        HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper,
                                        AclServiceClient aclServiceClient) {
            super(salutIntegracioRepository,
                    salutSubsistemaRepository,
                    salutMissatgeRepository,
                    salutDetallRepository,
                    salutHistRepository,
                    salutClientHelper,
                    metricsHelper,
                    authenticationHelper,
                    httpAuthorizationHeaderHelper,
                    aclServiceClient);
        }

        public String exposedAdditionalSpringFilter() {
            return super.additionalSpringFilter(null, null);
        }
    }

    @Mock
    private SalutRepository salutRepository;

    @Mock
    private SalutIntegracioRepository salutIntegracioRepository;

    @Mock
    private SalutSubsistemaRepository salutSubsistemaRepository;

    @Mock
    private SalutMissatgeRepository salutMissatgeRepository;

    @Mock
    private SalutDetallRepository salutDetallRepository;

    @Mock
    private SalutHistRepository salutHistRepository;

    @Mock
    private SalutClientHelper salutClientHelper;

    @Mock
    private MetricsHelper metricsHelper;

    @Mock
    private ObjectMappingHelper objectMappingHelper;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    private TestableSalutServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new TestableSalutServiceImpl(
                salutIntegracioRepository,
                salutSubsistemaRepository,
                salutMissatgeRepository,
                salutDetallRepository,
                salutHistRepository,
                salutClientHelper,
                metricsHelper,
                authenticationHelper,
                httpAuthorizationHeaderHelper,
                aclServiceClient);
        injectBaseField("entityRepository", salutRepository);
        injectBaseField("objectMappingHelper", objectMappingHelper);
        injectBaseField("resourceEntityMappingHelper", new ResourceEntityMappingHelper(new ObjectMappingHelper()));
        injectBaseField("jasperReportsHelper", org.mockito.Mockito.mock(JasperReportsHelper.class));
        service.init();
    }

    private void stubAclContext(String... roles) {
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer test");
        when(authenticationHelper.isCurrentUserInRole(anyString())).thenReturn(false);
        when(authenticationHelper.getCurrentUserName()).thenReturn("anna");
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(roles);
    }

    @Test
    void additionalSpringFilter_quanLusuariEsAdmin_noAplicaCapRestriccioAcl() {
        when(authenticationHelper.isCurrentUserInRole(anyString())).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole("COM_ADMIN")).thenReturn(true);

        String result = service.exposedAdditionalSpringFilter();

        assertThat(result).isNull();
        verifyNoInteractions(aclServiceClient);
    }

    @Test
    void additionalSpringFilter_quanTePermisosPerAppIEntornApp_retornaElsEntornsActiusPermesos() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(1L)));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(5L)));
        when(salutClientHelper.entornAppFindByActivaTrue("app.id:1"))
                .thenReturn(List.of(EntornApp.builder().id(10L).build(), EntornApp.builder().id(11L).build()));
        when(salutClientHelper.entornAppFindByActivaTrue("id:5"))
                .thenReturn(List.of(EntornApp.builder().id(11L).build(), EntornApp.builder().id(12L).build()));

        String result = service.exposedAdditionalSpringFilter();

        assertThat(result).isEqualTo("entornAppId:10 or entornAppId:11 or entornAppId:12");
    }

    @Test
    void additionalSpringFilter_quanNomesHiHaPermisosPerApp_retornaElsEntornsAppActiusDeLaApp() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(1L)));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(salutClientHelper.entornAppFindByActivaTrue("app.id:1"))
                .thenReturn(List.of(EntornApp.builder().id(10L).build(), EntornApp.builder().id(11L).build()));

        String result = service.exposedAdditionalSpringFilter();

        assertThat(result).isEqualTo("entornAppId:10 or entornAppId:11");
    }

    @Test
    void additionalSpringFilter_quanNomesHiHaPermisosPerEntornApp_retornaElsEntornsAppPermesos() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(5L, 7L)));
        when(salutClientHelper.entornAppFindByActivaTrue("id:5 or id:7"))
                .thenReturn(List.of(EntornApp.builder().id(11L).build(), EntornApp.builder().id(12L).build()));

        String result = service.exposedAdditionalSpringFilter();

        assertThat(result).isEqualTo("entornAppId:11 or entornAppId:12");
    }

    @Test
    void additionalSpringFilter_quanConsultaAcl_passaUsuariIRolsActuals() {
        stubAclContext("COM_USER", "COM_EXTRA");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER", "COM_EXTRA")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER", "COM_EXTRA")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));

        service.exposedAdditionalSpringFilter();

        verify(aclServiceClient).findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER", "COM_EXTRA")),
                eq("Bearer test"));
        verify(aclServiceClient).findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER", "COM_EXTRA")),
                eq("Bearer test"));
    }

    @Test
    void additionalSpringFilter_quanNoHiHaPermisos_retornaFiltreSenseResultats() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(null));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));

        String result = service.exposedAdditionalSpringFilter();

        assertThat(result).isEqualTo("entornAppId:0");
    }

    @Test
    void informeSalutLast_quanHiHaDadesPerEntornsActius_retornaElsRecursosMapejats() throws Exception {
        EntornApp firstEntorn = EntornApp.builder().id(10L).build();
        EntornApp secondEntorn = EntornApp.builder().id(20L).build();
        SalutEntity firstEntity = sampleSalutEntity(1L, 10L, LocalDateTime.of(2026, 3, 16, 8, 0));
        SalutEntity secondEntity = sampleSalutEntity(2L, 20L, LocalDateTime.of(2026, 3, 16, 8, 1));
        NoopTimer timer = new NoopTimer(new Meter.Id("test", Tags.empty(), null, null, Meter.Type.TIMER));

        when(metricsHelper.getSalutLastEntornAppsTimer()).thenReturn(timer);
        when(metricsHelper.getSalutLastDadesTimer()).thenReturn(timer);
        when(metricsHelper.getSalutLastGlobalTimer()).thenReturn(timer);
        when(salutClientHelper.entornAppFindByActivaTrue("illes")).thenReturn(Arrays.asList(firstEntorn, null, secondEntorn));
        when(salutRepository.informeSalutLast(eq(List.of(10L, 20L)), any(LocalDateTime.class)))
                .thenReturn(List.of(firstEntity, secondEntity));

        List<Salut> result = service.new InformeSalutLast().generateData(Salut.SALUT_REPORT_LAST, null, "illes");

        assertThat(result)
                .extracting(Salut::getEntornAppId)
                .containsExactly(10L, 20L);
        verify(salutRepository).informeSalutLast(eq(List.of(10L, 20L)), any(LocalDateTime.class));
    }

    @Test
    void perspectiveIntegracions_quanClientRetornaMetadades_completaNomILogoDeLesIntegracions() throws Exception {
        SalutEntity entity = sampleSalutEntity(11L, 33L, LocalDateTime.of(2026, 3, 16, 8, 2));
        Salut resource = sampleSalutResource(11L, 33L);
        SalutIntegracioEntity integracioEntity = new SalutIntegracioEntity();
        integracioEntity.setCodi("INT-A");
        integracioEntity.setSalut(entity);

        SalutIntegracio integracio = new SalutIntegracio();
        integracio.setCodi("INT-A");

        IntegracioRef integracioRef = new IntegracioRef(9L, "Integracio A");
        AppIntegracio appIntegracio = new AppIntegracio();
        ReflectionTestUtils.setField(appIntegracio, "codi", "INT-A");
        ReflectionTestUtils.setField(appIntegracio, "integracio", integracioRef);
        ReflectionTestUtils.setField(appIntegracio, "logo", "logo-a".getBytes());

        EntornApp entornApp = EntornApp.builder()
                .id(33L)
                .integracions(List.of(appIntegracio))
                .build();

        when(salutIntegracioRepository.findBySalut(entity)).thenReturn(List.of(integracioEntity));
        when(salutClientHelper.entornAppFindById(33L)).thenReturn(entornApp);
        when(objectMappingHelper.newInstanceMap(integracioEntity, SalutIntegracio.class, "salut")).thenReturn(integracio);

        service.new PerspectiveIntegracions().applySingle(Salut.PERSP_INTEGRACIONS, entity, resource);

        assertThat(resource.getIntegracions()).hasSize(1);
        assertThat(resource.getIntegracions().get(0).getNom()).isEqualTo("Integracio A");
        assertThat(resource.getIntegracions().get(0).getLogo()).isEqualTo("logo-a".getBytes());
    }

    @Test
    void perspectiveContexts_quanLEntornTeContexts_elsPropagaAlRecurs() throws Exception {
        SalutEntity entity = sampleSalutEntity(12L, 44L, LocalDateTime.of(2026, 3, 16, 8, 3));
        Salut resource = sampleSalutResource(12L, 44L);
        AppContext context = new AppContext();
        ReflectionTestUtils.setField(context, "codi", "ctx");
        ReflectionTestUtils.setField(context, "nom", "Context principal");
        EntornApp entornApp = EntornApp.builder().id(44L).contexts(List.of(context)).build();
        when(salutClientHelper.entornAppFindById(44L)).thenReturn(entornApp);

        service.new PerspectiveContexts().applySingle(Salut.PERSP_CONTEXTS, entity, resource);

        assertThat(resource.getContexts())
                .singleElement()
                .extracting(AppContext::getCodi)
                .isEqualTo("ctx");
    }

    @Test
    void perspectiveMissatges_quanRepositoriRetornaNull_noAssignaLlistaDeMissatges() throws Exception {
        SalutEntity entity = sampleSalutEntity(13L, 55L, LocalDateTime.of(2026, 3, 16, 8, 4));
        Salut resource = sampleSalutResource(13L, 55L);
        when(salutMissatgeRepository.findBySalut(entity)).thenReturn(null);

        service.new PerspectiveMissatges().applySingle(Salut.PERSP_MISSATGES, entity, resource);

        assertThat(resource.getMissatges()).isNull();
    }

    @Test
    void perspectiveMissatges_quanHiHaMissatges_elsMapejaAlRecurs() throws Exception {
        SalutEntity entity = sampleSalutEntity(14L, 56L, LocalDateTime.of(2026, 3, 16, 8, 5));
        Salut resource = sampleSalutResource(14L, 56L);
        SalutMissatgeEntity missatgeEntity = new SalutMissatgeEntity();
        missatgeEntity.setSalut(entity);
        SalutMissatge missatge = new SalutMissatge();
        missatge.setMissatge("warning");
        when(salutMissatgeRepository.findBySalut(entity)).thenReturn(List.of(missatgeEntity));
        when(objectMappingHelper.newInstanceMap(missatgeEntity, SalutMissatge.class, "salut")).thenReturn(missatge);

        service.new PerspectiveMissatges().applySingle(Salut.PERSP_MISSATGES, entity, resource);

        assertThat(resource.getMissatges())
                .extracting(SalutMissatge::getMissatge)
                .containsExactly("warning");
    }

    @Test
    void informeGrupsDates_quanAgrupacioEsMinutsHora_alineaLaDataInicialIIncrementaPerFranges() throws Exception {
        LocalDateTime referencia = LocalDateTime.of(2026, 3, 16, 8, 11);
        var params = new es.caib.comanda.salut.logic.intf.model.SalutInformeGrupsParams(referencia, SalutInformeAgrupacio.MINUTS_HORA);

        List<SalutInformeGrupItem> result = service.new InformeGrupsDates().generateData(Salut.SALUT_REPORT_GRUPS_DATES, null, params);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 7, 8));
        assertThat(result.get(result.size() - 1).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 8, 8));
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i + 1).getData())
                    .isEqualTo(result.get(i).getData().plusMinutes(MINUTS_PER_AGRUPACIO));
        }
    }

    @Test
    void informeGrupsDates_quanAgrupacioEsMinut_retornaFrangesDunMinutDurantEls15MinutsPrevis() throws Exception {
        LocalDateTime referencia = LocalDateTime.of(2026, 3, 16, 8, 11, 49);
        var params = new es.caib.comanda.salut.logic.intf.model.SalutInformeGrupsParams(referencia, SalutInformeAgrupacio.MINUT);

        List<SalutInformeGrupItem> result = service.new InformeGrupsDates().generateData(Salut.SALUT_REPORT_GRUPS_DATES, null, params);

        assertThat(result.get(0).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 7, 56));
        assertThat(result.get(result.size() - 1).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 8, 11));
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i + 1).getData()).isEqualTo(result.get(i).getData().plusMinutes(1));
        }
    }

    @Test
    void informeGrupsDates_quanAgrupacioEsHora_retornaFrangesHorariesDurantElDarrerDia() throws Exception {
        LocalDateTime referencia = LocalDateTime.of(2026, 3, 16, 8, 11);
        var params = new es.caib.comanda.salut.logic.intf.model.SalutInformeGrupsParams(referencia, SalutInformeAgrupacio.HORA);

        List<SalutInformeGrupItem> result = service.new InformeGrupsDates().generateData(Salut.SALUT_REPORT_GRUPS_DATES, null, params);

        assertThat(result.get(0).getData()).isEqualTo(LocalDateTime.of(2026, 3, 15, 8, 0));
        assertThat(result.get(result.size() - 1).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 8, 0));
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i + 1).getData()).isEqualTo(result.get(i).getData().plusHours(1));
        }
    }

    @Test
    void informeGrupsDates_quanAgrupacioEsDiaSetmana_retornaFrangesDiariesDurantLaDarreraSetmana() throws Exception {
        LocalDateTime referencia = LocalDateTime.of(2026, 3, 16, 8, 11);
        var params = new es.caib.comanda.salut.logic.intf.model.SalutInformeGrupsParams(referencia, SalutInformeAgrupacio.DIA_SETMANA);

        List<SalutInformeGrupItem> result = service.new InformeGrupsDates().generateData(Salut.SALUT_REPORT_GRUPS_DATES, null, params);

        assertThat(result.get(0).getData()).isEqualTo(LocalDateTime.of(2026, 3, 9, 0, 0));
        assertThat(result.get(result.size() - 1).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 0, 0));
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i + 1).getData()).isEqualTo(result.get(i).getData().plusDays(1));
        }
    }

    @Test
    void informeGrupsDates_quanAgrupacioEsDiaMes_retornaFrangesDiariesDurantElsDarrers30Dies() throws Exception {
        LocalDateTime referencia = LocalDateTime.of(2026, 3, 16, 8, 11);
        var params = new es.caib.comanda.salut.logic.intf.model.SalutInformeGrupsParams(referencia, SalutInformeAgrupacio.DIA_MES);

        List<SalutInformeGrupItem> result = service.new InformeGrupsDates().generateData(Salut.SALUT_REPORT_GRUPS_DATES, null, params);

        assertThat(result.get(0).getData()).isEqualTo(LocalDateTime.of(2026, 2, 14, 0, 0));
        assertThat(result.get(result.size() - 1).getData()).isEqualTo(LocalDateTime.of(2026, 3, 16, 0, 0));
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i + 1).getData()).isEqualTo(result.get(i).getData().plusDays(1));
        }
    }

    @Test
    void informeEstat_quanAgrupacioEsMinutsHora_consultaElRepositoriAmbLaDataAjustadaIElTipusCorrecte() throws Exception {
        SalutEntity entity = sampleSalutEntity(31L, 88L, LocalDateTime.of(2026, 3, 16, 8, 6));
        var params = new es.caib.comanda.salut.logic.intf.model.SalutInformeParams(
                LocalDateTime.of(2026, 3, 16, 8, 11),
                88L,
                SalutInformeAgrupacio.MINUTS_HORA);
        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                88L,
                LocalDateTime.of(2026, 3, 16, 7, 8),
                TipusRegistreSalut.MINUTS))
                .thenReturn(List.of(entity));

        List<SalutInformeEstatItem> result = service.new InformeEstat().generateData(Salut.SALUT_REPORT_ESTAT, null, params);

        assertThat(result).hasSize(1);
        verify(salutRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                88L,
                LocalDateTime.of(2026, 3, 16, 7, 8),
                TipusRegistreSalut.MINUTS);
    }

    @Test
    void informeEstats_quanHiHaDiversosEntorns_consultaCadaEntornAmbLaMateixaFinestraTemporal() throws Exception {
        SalutEntity entity = sampleSalutEntity(41L, 0L, LocalDateTime.of(2026, 3, 16, 8, 6));
        var params = new es.caib.comanda.salut.logic.intf.model.SalutInformeLlistatParams(
                LocalDateTime.of(2026, 3, 16, 8, 11),
                List.of(5L, 6L, 7L),
                SalutInformeAgrupacio.MINUTS_HORA);
        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                org.mockito.ArgumentMatchers.anyLong(),
                eq(LocalDateTime.of(2026, 3, 16, 7, 8)),
                eq(TipusRegistreSalut.MINUTS)))
                .thenReturn(List.of(entity));

        List<HashMap<String, Object>> result = service.new InformeEstats().generateData(Salut.SALUT_REPORT_ESTATS, null, params);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsKeys("5", "6", "7");
        verify(salutRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                5L,
                LocalDateTime.of(2026, 3, 16, 7, 8),
                TipusRegistreSalut.MINUTS);
        verify(salutRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                6L,
                LocalDateTime.of(2026, 3, 16, 7, 8),
                TipusRegistreSalut.MINUTS);
        verify(salutRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                7L,
                LocalDateTime.of(2026, 3, 16, 7, 8),
                TipusRegistreSalut.MINUTS);
    }

    @Test
    void informeLatencia_quanConsultaPerAgrupacioMinutsHora_usaTipusIMargeTemporalCorrectes() throws Exception {
        SalutEntity entity = sampleSalutEntity(21L, 77L, LocalDateTime.of(2026, 3, 16, 8, 6));
        var params = new es.caib.comanda.salut.logic.intf.model.SalutInformeParams(
                LocalDateTime.of(2026, 3, 16, 8, 11),
                77L,
                SalutInformeAgrupacio.MINUTS_HORA);
        when(salutRepository.findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                77L,
                LocalDateTime.of(2026, 3, 16, 7, 8),
                TipusRegistreSalut.MINUTS))
                .thenReturn(List.of(entity));

        var result = service.new InformeLatencia().generateData(Salut.SALUT_REPORT_LATENCIA, null, params);

        assertThat(result).hasSize(1);
        verify(salutRepository).findByEntornAppIdAndDataGreaterThanEqualAndTipusRegistreOrderById(
                77L,
                LocalDateTime.of(2026, 3, 16, 7, 8),
                TipusRegistreSalut.MINUTS);
    }

    private void injectBaseField(String fieldName, Object value) throws Exception {
        Field field = BaseReadonlyResourceService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    private static SalutEntity sampleSalutEntity(Long id, Long entornAppId, LocalDateTime data) {
        SalutEntity entity = new SalutEntity();
        entity.setId(id);
        entity.setEntornAppId(entornAppId);
        entity.setData(data);
        entity.setDataApp(data);
        entity.setTipusRegistre(TipusRegistreSalut.MINUT);
        entity.setAppEstat(SalutEstat.UP);
        entity.setBdEstat(SalutEstat.WARN);
        entity.setAppLatencia(80);
        entity.setBdLatencia(15);
        entity.setNumElements(1);
        return entity;
    }

    private static Salut sampleSalutResource(Long id, Long entornAppId) {
        Salut salut = new Salut();
        salut.setId(id);
        salut.setEntornAppId(entornAppId);
        salut.setData(LocalDateTime.of(2026, 3, 16, 8, 0));
        salut.setVersio("1.0.0");
        salut.setAppEstat(SalutEstat.UP);
        salut.setBdEstat(SalutEstat.WARN);
        return salut;
    }
}
