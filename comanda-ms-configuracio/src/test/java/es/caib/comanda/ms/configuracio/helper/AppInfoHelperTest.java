package es.caib.comanda.ms.configuracio.helper;

import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.configuracio.logic.helper.*;
import es.caib.comanda.configuracio.persist.entity.*;
import es.caib.comanda.configuracio.persist.repository.*;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.IntegracioInfo;
import es.caib.comanda.model.v1.salut.SubsistemaInfo;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppInfoHelperTest {

    @Mock
    private EntornAppRepository entornAppRepository;

    @Mock
    private AppIntegracioRepository appIntegracioRepository;

    @Mock
    private IntegracioRepository integracioRepository;

    @Mock
    private SubsistemaRepository subsistemaRepository;

    @Mock
    private ContextRepository contextRepository;

    @Mock
    private ManualRepository manualRepository;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private MonitorServiceClient monitorServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CacheHelper cacheHelper;

    @Captor
    private ArgumentCaptor<IntegracioEntity> integracioEntityCaptor;

    @Captor
    private ArgumentCaptor<AppSubsistemaEntity> subsistemaEntityCaptor;

    @Captor
    private ArgumentCaptor<AppContextEntity> contextEntityCaptor;

    private AppInfoHelper appInfoHelper;
    private AppInfoEntornAppHelper appInfoEntornAppHelper;
    private AppInfoIntegracionsHelper appInfoIntegracionsHelper;
    private AppInfoSubsistemesHelper appInfoSubsistemesHelper;
    private AppInfoContextsHelper appInfoContextsHelper;

    private EntornAppEntity entornAppEntity;
    private AppInfoHelper.AppInfoEntornAppProjection entornAppProjection;
    private List<EntornAppEntity> activeEntornApps;
    private AppInfo appInfo;
    private List<IntegracioInfo> integracions;
    private List<SubsistemaInfo> subsistemes;

    @BeforeEach
    void setUp() {
        // Instantiate real helpers with mocked repositories
	    appInfoEntornAppHelper = spy(new AppInfoEntornAppHelper(entornAppRepository));
	    appInfoIntegracionsHelper = spy(new AppInfoIntegracionsHelper(entornAppRepository, appIntegracioRepository, integracioRepository));
	    appInfoSubsistemesHelper = spy(new AppInfoSubsistemesHelper(entornAppRepository, subsistemaRepository));
	    appInfoContextsHelper = spy(new AppInfoContextsHelper(entornAppRepository, contextRepository, manualRepository));

        // Create the helper with mocked dependencies
        appInfoHelper = new AppInfoHelper(
                httpAuthorizationHeaderHelper,
                monitorServiceClient,
                restTemplate,
                cacheHelper,
		        appInfoEntornAppHelper,
		        appInfoIntegracionsHelper,
		        appInfoSubsistemesHelper,
		        appInfoContextsHelper);

        // Setup test data
        AppEntity appEntity = new AppEntity();
        appEntity.setId(1L);
        appEntity.setNom("Test App");
        appEntity.setActiva(true);

        EntornEntity entornEntity = new EntornEntity();
        entornEntity.setId(1L);
        entornEntity.setNom("Test Entorn");

        entornAppEntity = new EntornAppEntity();
        entornAppEntity.setId(1L);
        entornAppEntity.setApp(appEntity);
        entornAppEntity.setEntorn(entornEntity);
        entornAppEntity.setInfoUrl("http://test.com/info");
        entornAppEntity.setSalutUrl("http://test.com/salut");
        entornAppEntity.setEstadisticaUrl("http://test.com/estadistica");
        entornAppEntity.setEstadisticaCron("0 0 * * * *");
        entornAppEntity.setActiva(true);

        activeEntornApps = new ArrayList<>();
        activeEntornApps.add(entornAppEntity);
        entornAppProjection = new AppInfoHelper.AppInfoEntornAppProjection(
                entornAppEntity.getId(),
                entornAppEntity.getInfoUrl(),
                entornAppEntity.isSalutAuth(),
                appEntity.getNom(),
                entornEntity.getNom()
        );

        // Setup integracions
        IntegracioInfo integracio = IntegracioInfo.builder()
                .codi("INT1")
                .nom("Integracio 1")
                .build();

        integracions = new ArrayList<>();
        integracions.add(integracio);

        // Setup subsistemes
        SubsistemaInfo subsistema = SubsistemaInfo.builder()
                .codi("SUB1")
                .nom("Subsistema 1")
                .build();

        subsistemes = new ArrayList<>();
        subsistemes.add(subsistema);

        // Setup AppInfo response
        appInfo = AppInfo.builder()
                .codi("APP1")
                .nom("Test App")
                .versio("1.0.0")
                .revisio("rev123")
                .jdkVersion("17")
                .versioJboss("7.4")
                .data(OffsetDateTime.now())
                .integracions(integracions)
                .subsistemes(subsistemes)
                .build();

//        // Mock keycloakHelper
//        when(keycloakHelper.getAuthorizationHeader()).thenReturn("Bearer token");
    }

    @Test
    void testRefreshAppInfoById() {
        // Mock repositories
        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));
        when(appIntegracioRepository.findByEntornApp(entornAppEntity)).thenReturn(new ArrayList<>());
        when(subsistemaRepository.findByEntornApp(entornAppEntity)).thenReturn(new ArrayList<>());
        when(contextRepository.findByEntornApp(entornAppEntity)).thenReturn(new ArrayList<>());

        // Mock RestTemplate
        when(restTemplate.exchange(eq("http://test.com/info"), eq(HttpMethod.GET), any(), eq(AppInfo.class)))
                .thenReturn(new ResponseEntity<>(appInfo, HttpStatus.OK));

        // Call the method to test
        appInfoHelper.refreshAppInfo(entornAppProjection);

        // Verify that the RestTemplate was called
        verifyRestTemplateCall();

        // Verify that the entity was updated
        assertEquals(appInfo.getVersio(), entornAppEntity.getVersio());
        assertEquals(appInfo.getRevisio(), entornAppEntity.getRevisio());
        assertEquals(appInfo.getJdkVersion(), entornAppEntity.getJdkVersion());
        assertNotNull(entornAppEntity.getInfoData());

        // Verify that integracions, subsistemes and contexts were refreshed
        verify(appInfoIntegracionsHelper).refreshIntegracions(eq(entornAppEntity.getId()), any());
        verify(appInfoSubsistemesHelper).refreshSubsistemes(eq(entornAppEntity.getId()), any());
        verify(appInfoContextsHelper).refreshContexts(eq(entornAppEntity.getId()), any());
    }

    @Test
    void testRefreshAppInfoWithRestClientException() {
        // Mock RestTemplate to throw an exception
        when(restTemplate.exchange(eq("http://test.com/info"), eq(HttpMethod.GET), any(), eq(AppInfo.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // Call the method to test
        appInfoHelper.refreshAppInfo(entornAppProjection);

        // Verify that the RestTemplate was called
        verifyRestTemplateCall();

        // Verify that the entity was not updated
        verify(appInfoEntornAppHelper, never()).storeAppInfo(any(), any());
        // Verify that integracions, subsistemes and contexts were not refreshed
        verify(appInfoIntegracionsHelper, never()).refreshIntegracions(any(), any());
        verify(appInfoSubsistemesHelper, never()).refreshSubsistemes(any(), any());
        verify(appInfoContextsHelper, never()).refreshContexts(any(), any());
    }

    @Test
    void testRefreshAppInfoWithEmptySalutUrlStillUsesInfoUrl() {
        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));
        when(appIntegracioRepository.findByEntornApp(entornAppEntity)).thenReturn(new ArrayList<>());
        when(subsistemaRepository.findByEntornApp(entornAppEntity)).thenReturn(new ArrayList<>());
        when(contextRepository.findByEntornApp(entornAppEntity)).thenReturn(new ArrayList<>());

        when(restTemplate.exchange(eq("http://test.com/info"), eq(HttpMethod.GET), any(), eq(AppInfo.class)))
                .thenReturn(new ResponseEntity<>(appInfo, HttpStatus.OK));

        entornAppEntity.setSalutUrl("   ");

        appInfoHelper.refreshAppInfo(entornAppProjection);

        verify(restTemplate).exchange(eq("http://test.com/info"), eq(HttpMethod.GET), any(), eq(AppInfo.class));
        assertEquals(appInfo.getVersio(), entornAppEntity.getVersio());
    }
//    private void mockRestTemplate() {
//        // Use ReflectionTestUtils to set the restTemplate field
//        ReflectionTestUtils.setField(appInfoHelper, "restTemplate", restTemplate);
//    }

    private void verifyRestTemplateCall() {
        verify(restTemplate).exchange(eq("http://test.com/info"), eq(HttpMethod.GET), any(), eq(AppInfo.class));
    }
}
