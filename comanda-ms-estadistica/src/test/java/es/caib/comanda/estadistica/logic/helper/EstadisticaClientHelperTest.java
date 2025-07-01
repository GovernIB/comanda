package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstadisticaClientHelperTest {

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private MonitorServiceClient monitorServiceClient;

    @Mock
    private EntornAppServiceClient entornAppServiceClient;

    @Mock
    private AppServiceClient appServiceClient;

    @InjectMocks
    private EstadisticaClientHelper estadisticaClientHelper;

    private App app;
    private EntornApp entornApp;
    private Monitor monitor;
    private String authHeader;

    @BeforeEach
    void setUp() {
        // Setup test data
        app = new App();
        ReflectionTestUtils.setField(app, "id", 1L);
        ReflectionTestUtils.setField(app, "nom", "Test App");
        ReflectionTestUtils.setField(app, "activa", true);

        entornApp = EntornApp.builder()
                .id(1L)
                .activa(true)
                .build();

        monitor = Monitor.builder()
                .entornAppId(1L)
                .modul(ModulEnum.ESTADISTICA)
                .tipus(AccioTipusEnum.SORTIDA)
                .url("http://test.com/estadistica")
                .estat(EstatEnum.OK)
                .build();

        authHeader = "Bearer test-token";
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(authHeader);
    }

    @Test
    void testAppFindById() {
        // Arrange
        EntityModel<App> entityModel = EntityModel.of(app);
        when(appServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(entityModel);

        // Act
        App result = estadisticaClientHelper.appFindById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("Test App", result.getNom());
        assertTrue(result.isActiva());
        verify(appServiceClient).getOne(eq(1L), isNull(), eq(authHeader));
    }

    @Test
    void testAppFindById_ReturnsNull() {
        // Arrange
        when(appServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(null);

        // Act
        App result = estadisticaClientHelper.appFindById(1L);

        // Assert
        assertNull(result);
        verify(appServiceClient).getOne(eq(1L), isNull(), eq(authHeader));
    }

    @Test
    void testEntornAppFindById() {
        // Arrange
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        when(entornAppServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(entityModel);

        // Act
        EntornApp result = estadisticaClientHelper.entornAppFindById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertTrue(result.isActiva());
        verify(entornAppServiceClient).getOne(eq(1L), isNull(), eq(authHeader));
    }

    @Test
    void testEntornAppFindById_ReturnsNull() {
        // Arrange
        when(entornAppServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(null);

        // Act
        EntornApp result = estadisticaClientHelper.entornAppFindById(1L);

        // Assert
        assertNull(result);
        verify(entornAppServiceClient).getOne(eq(1L), isNull(), eq(authHeader));
    }

    @Test
    void testEntornAppFindByActivaTrue() {
        // Arrange
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        PagedModel.PageMetadata metadata = new PagedModel.PageMetadata(1, 0, 1);
        PagedModel<EntityModel<EntornApp>> pagedModel = PagedModel.of(
                Collections.singletonList(entityModel),
                metadata
        );

        when(entornAppServiceClient.find(
                isNull(),
                eq("activa:true and app.activa:true"),
                isNull(),
                isNull(),
                eq("UNPAGED"),
                isNull(),
                eq(authHeader)
        )).thenReturn(pagedModel);

        // Act
        List<EntornApp> result = estadisticaClientHelper.entornAppFindByActivaTrue();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId().longValue());
        verify(entornAppServiceClient).find(
                isNull(),
                eq("activa:true and app.activa:true"),
                isNull(),
                isNull(),
                eq("UNPAGED"),
                isNull(),
                eq(authHeader)
        );
    }

    @Test
    void testEntornAppFindByActivaTrue_ReturnsEmptyList() {
        // Arrange
        when(entornAppServiceClient.find(
                isNull(),
                eq("activa:true and app.activa:true"),
                isNull(),
                isNull(),
                eq("UNPAGED"),
                isNull(),
                eq(authHeader)
        )).thenReturn(null);

        // Act
        List<EntornApp> result = estadisticaClientHelper.entornAppFindByActivaTrue();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(entornAppServiceClient).find(
                isNull(),
                eq("activa:true and app.activa:true"),
                isNull(),
                isNull(),
                eq("UNPAGED"),
                isNull(),
                eq(authHeader)
        );
    }

    @Test
    void testMonitorCreate() {
        // Act
        estadisticaClientHelper.monitorCreate(monitor);

        // Assert
        verify(monitorServiceClient).create(eq(monitor), eq(authHeader));
    }

    @Test
    void testMonitorCreate_HandlesException() {
        // Arrange
        doThrow(new RuntimeException("Test exception")).when(monitorServiceClient).create(eq(monitor), eq(authHeader));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> estadisticaClientHelper.monitorCreate(monitor));
        verify(monitorServiceClient).create(eq(monitor), eq(authHeader));
    }
}
