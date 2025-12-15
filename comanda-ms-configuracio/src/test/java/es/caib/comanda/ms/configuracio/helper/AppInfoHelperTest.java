package es.caib.comanda.ms.configuracio.helper;

import es.caib.comanda.client.EstadisticaServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.persist.entity.AppContextEntity;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.entity.IntegracioEntity;
import es.caib.comanda.configuracio.persist.repository.AppIntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.ContextRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.IntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.ManualRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.model.v1.salut.ContextInfo;
import es.caib.comanda.model.v1.salut.IntegracioInfo;
import es.caib.comanda.model.v1.salut.SubsistemaInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    private SalutServiceClient salutServiceClient;

    @Mock
    private EstadisticaServiceClient estadisticaServiceClient;

    @Mock
    private MonitorServiceClient monitorServiceClient;

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<IntegracioEntity> integracioEntityCaptor;

    @Captor
    private ArgumentCaptor<AppSubsistemaEntity> subsistemaEntityCaptor;

    @Captor
    private ArgumentCaptor<AppContextEntity> contextEntityCaptor;

    private AppInfoHelper appInfoHelper;

    private EntornAppEntity entornAppEntity;
    private List<EntornAppEntity> activeEntornApps;
    private AppInfo appInfo;
    private List<IntegracioInfo> integracions;
    private List<SubsistemaInfo> subsistemes;

    @BeforeEach
    void setUp() {
        // Create the helper with mocked dependencies
        appInfoHelper = new AppInfoHelper(
                entornAppRepository,
                appIntegracioRepository,
                integracioRepository,
                subsistemaRepository,
                contextRepository,
                manualRepository,
                httpAuthorizationHeaderHelper,
                salutServiceClient,
                estadisticaServiceClient,
                monitorServiceClient,
                restTemplate);

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
                .versioJboss("7.4")
                .data(new Date())
                .integracions(integracions)
                .subsistemes(subsistemes)
                .build();

//        // Mock keycloakHelper
//        when(keycloakHelper.getAuthorizationHeader()).thenReturn("Bearer token");
    }

    @Test
    void testRefreshAppInfoById() {
        // Mock repository call
        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));

        // Mock RestTemplate
//        mockRestTemplate();
        when(restTemplate.getForObject(eq("http://test.com/info"), eq(AppInfo.class))).thenReturn(appInfo);

        // Call the method to test
        appInfoHelper.refreshAppInfo(1L);

        // Verify that the repository was called
        verify(entornAppRepository).findById(1L);

        // Verify that the RestTemplate was called
        verifyRestTemplateCall();

        // Verify that the entity was updated
        assertEquals(appInfo.getVersio(), entornAppEntity.getVersio());
        assertNotNull(entornAppEntity.getInfoData());

        // Verify that integracions and subsistemes were refreshed
        verify(appIntegracioRepository).findByEntornApp(entornAppEntity);
        verify(subsistemaRepository).findByEntornApp(entornAppEntity);
    }

    @Test
    void testRefreshAppInfoByIdNotFound() {
        // Mock repository call
        when(entornAppRepository.findById(1L)).thenReturn(Optional.empty());

        // Call the method to test and verify that it throws an exception
        assertThrows(ResourceNotFoundException.class, () -> {
            appInfoHelper.refreshAppInfo(1L);
        });

        // Verify that the repository was called
        verify(entornAppRepository).findById(1L);
    }

    @Test
    void testRefreshAppInfoAll() {
        // Mock repository call
        when(entornAppRepository.findByActivaTrueAndAppActivaTrue()).thenReturn(activeEntornApps);

        // Mock RestTemplate
//        mockRestTemplate();
        when(restTemplate.getForObject(eq("http://test.com/info"), eq(AppInfo.class))).thenReturn(appInfo);

        // Call the method to test
        appInfoHelper.refreshAppInfo();

        // Verify that the repository was called
        verify(entornAppRepository).findByActivaTrueAndAppActivaTrue();

        // Verify that the RestTemplate was called
        verifyRestTemplateCall();

        // Verify that the entity was updated
        assertEquals(appInfo.getVersio(), entornAppEntity.getVersio());
        assertNotNull(entornAppEntity.getInfoData());

        // Verify that integracions and subsistemes were refreshed
        verify(appIntegracioRepository).findByEntornApp(entornAppEntity);
        verify(subsistemaRepository).findByEntornApp(entornAppEntity);
    }

    @Test
    void testRefreshAppInfoAll_IgnoresValidationErrors() {
        // Mock repository call
        when(entornAppRepository.findByActivaTrueAndAppActivaTrue()).thenReturn(activeEntornApps);

        // Rebuild AppInfo with invalid data
        appInfo = new AppInfo(
                appInfo.getCodi(),
                appInfo.getNom(),
                appInfo.getVersio(),
                appInfo.getData(),
                appInfo.getRevisio(),
                appInfo.getJdkVersion(),
                appInfo.getVersioJboss(),
                Arrays.asList(appInfo.getIntegracions().get(0), new IntegracioInfo("INT2", "")),
                Arrays.asList(appInfo.getSubsistemes().get(0), new SubsistemaInfo("SUB1", "")),
                List.of(new ContextInfo("CON1", "", "", null, ""))
        );

        // Mock RestTemplate
        when(restTemplate.getForObject(eq("http://test.com/info"), eq(AppInfo.class))).thenReturn(appInfo);

        // Call the method to test
        appInfoHelper.refreshAppInfo();

        // Verify that integracions and subsistemes were refreshed
        verify(integracioRepository).save(integracioEntityCaptor.capture());
        assertEquals(1, integracioEntityCaptor.getAllValues().size());
        verify(subsistemaRepository).save(subsistemaEntityCaptor.capture());
        assertEquals(1, subsistemaEntityCaptor.getAllValues().size());
        verify(contextRepository, never()).save(any());

    }

    @Test
    void testRefreshAppInfoWithRestClientException() {
        // Mock repository call
        when(entornAppRepository.findById(1L)).thenReturn(Optional.of(entornAppEntity));

        // Mock RestTemplate to throw an exception
//        mockRestTemplate();
        when(restTemplate.getForObject(eq("http://test.com/info"), eq(AppInfo.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // Call the method to test
        appInfoHelper.refreshAppInfo(1L);

        // Verify that the repository was called
        verify(entornAppRepository).findById(1L);

        // Verify that the RestTemplate was called
        verifyRestTemplateCall();

        // Verify that integracions and subsistemes were not refreshed
        verify(appIntegracioRepository, never()).findByEntornApp(entornAppEntity);
        verify(subsistemaRepository, never()).findByEntornApp(entornAppEntity);
    }

    @Test
    void testProgramarTasquesSalutEstadistica() {
        // Mock keycloakHelper
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer token");

        // Call the method to test
        appInfoHelper.programarTasquesSalutEstadistica(entornAppEntity);

        // Verify that the clients were called
        verify(salutServiceClient).programar(any(EntornApp.class), anyString());
        verify(estadisticaServiceClient).programar(any(EntornApp.class), anyString());

        // Verify that the keycloakHelper was called
        verify(httpAuthorizationHeaderHelper, times(2)).getAuthorizationHeader();
    }

    @Test
    void testProgramarTasquesSalutEstadisticaWithException() {
        // Mock clients to throw exceptions
        doThrow(new RuntimeException("Salut error")).when(salutServiceClient).programar(any(EntornApp.class), anyString());
        doThrow(new RuntimeException("Estadistica error")).when(estadisticaServiceClient).programar(any(EntornApp.class), anyString());
        // Mock keycloakHelper
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer token");

        // Call the method to test
        appInfoHelper.programarTasquesSalutEstadistica(entornAppEntity);

        // Verify that the clients were called despite the exceptions
        verify(salutServiceClient).programar(any(EntornApp.class), anyString());
        verify(estadisticaServiceClient).programar(any(EntornApp.class), anyString());

        // Verify that the keycloakHelper was called
        verify(httpAuthorizationHeaderHelper, times(2)).getAuthorizationHeader();
    }

//    private void mockRestTemplate() {
//        // Use ReflectionTestUtils to set the restTemplate field
//        ReflectionTestUtils.setField(appInfoHelper, "restTemplate", restTemplate);
//    }

    private void verifyRestTemplateCall() {
        verify(restTemplate).getForObject(eq("http://test.com/info"), eq(AppInfo.class));
    }
}
