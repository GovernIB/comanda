package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SalutClientHelperTest {

    @Mock
    private KeycloakHelper keycloakHelper;

    @Mock
    private MonitorServiceClient monitorServiceClient;

    @Mock
    private EntornAppServiceClient entornAppServiceClient;

    @InjectMocks
    private SalutClientHelper salutClientHelper;

    private EntornApp entornApp;
    private Monitor monitor;
    private String authHeader;

    @BeforeEach
    void setUp() {
        // Setup test data
        entornApp = EntornApp.builder()
                .id(1L)
                .activa(true)
                .build();

        monitor = Monitor.builder()
                .entornAppId(1L)
                .modul(ModulEnum.SALUT)
                .tipus(AccioTipusEnum.SORTIDA)
                .url("http://test.com/health")
                .estat(EstatEnum.OK)
                .build();

        authHeader = "Bearer test-token";
        when(keycloakHelper.getAuthorizationHeader()).thenReturn(authHeader);
    }

    @Test
    void testEntornAppFindById() {
        // Arrange
        EntityModel<EntornApp> entityModel = EntityModel.of(entornApp);
        when(entornAppServiceClient.getOne(eq(1L), isNull(), eq(authHeader))).thenReturn(entityModel);

        // Act
        EntornApp result = salutClientHelper.entornAppFindById(1L);

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
        EntornApp result = salutClientHelper.entornAppFindById(1L);

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
        List<EntornApp> result = salutClientHelper.entornAppFindByActivaTrue();

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
    void testMonitorCreate() {
        // Act
        salutClientHelper.monitorCreate(monitor);

        // Assert
        verify(monitorServiceClient).create(eq(monitor), eq(authHeader));
    }

    @Test
    void testMonitorCreate_HandlesException() {
        // Arrange
        doThrow(new RuntimeException("Test exception")).when(monitorServiceClient).create(eq(monitor), eq(authHeader));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> salutClientHelper.monitorCreate(monitor));
        verify(monitorServiceClient).create(eq(monitor), eq(authHeader));
    }
}
