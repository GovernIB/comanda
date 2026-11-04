package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.configuracio.logic.intf.model.AppGrup;
import es.caib.comanda.configuracio.logic.service.AppGrupServiceImpl;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.AppGrupEntity;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.persist.entity.ReorderableEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppGrupServiceImplTest {

    static class TestableAppGrupServiceImpl extends AppGrupServiceImpl {
        public void exposedBeforeDelete(AppGrupEntity entity) {
            super.beforeDelete(entity, new HashMap<>());
        }
    }

    @Mock
    private CacheHelper cacheHelper;

    private TestableAppGrupServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TestableAppGrupServiceImpl();
        ReflectionTestUtils.setField(service, "cacheHelper", cacheHelper);
    }

    @Test
    @DisplayName("beforeDelete should dissociate all apps and evict cache")
    void testBeforeDelete_unlinksApps() {
        AppGrupEntity grup = new AppGrupEntity();
        grup.setId(10L);
        grup.setNom("Grup Proves");

        AppEntity app1 = new AppEntity();
        app1.setId(101L);
        app1.setCodi("APP1");
        app1.setGrup(grup);

        AppEntity app2 = new AppEntity();
        app2.setId(102L);
        app2.setCodi("APP2");
        app2.setGrup(grup);

        grup.setApps(new ArrayList<>(Arrays.asList(app1, app2)));

        service.exposedBeforeDelete(grup);

        assertNull(app1.getGrup());
        assertNull(app2.getGrup());
        verify(cacheHelper, times(1)).evictAppCacheItem(101L, "APP1");
        verify(cacheHelper, times(1)).evictAppCacheItem(102L, "APP2");
    }

    @Test
    @DisplayName("beforeDelete with null apps should not throw")
    void testBeforeDelete_nullApps() {
        AppGrupEntity grup = new AppGrupEntity();
        grup.setId(20L);
        grup.setNom("Grup Buit");
        grup.setApps(null);

        assertDoesNotThrow(() -> service.exposedBeforeDelete(grup));
        verifyNoInteractions(cacheHelper);
    }

    @Test
    @DisplayName("beforeDelete with empty apps list should not throw")
    void testBeforeDelete_emptyApps() {
        AppGrupEntity grup = new AppGrupEntity();
        grup.setId(40L);
        grup.setNom("Grup Sense Apps");
        grup.setApps(new ArrayList<>());

        assertDoesNotThrow(() -> service.exposedBeforeDelete(grup));
        verifyNoInteractions(cacheHelper);
    }

    @Test
    @DisplayName("AppGrup should sort by nom by default and not define orderField")
    void testAppGrupResourceConfig() {
        ResourceConfig config = AppGrup.class.getAnnotation(ResourceConfig.class);
        assertNotNull(config);
        assertEquals("", config.orderField());
        assertEquals(1, config.defaultSortFields().length);
        assertEquals(AppGrup.Fields.nom, config.defaultSortFields()[0].field());
    }

    @Test
    @DisplayName("AppGrupEntity should not implement ReorderableEntity")
    void testAppGrupEntityNotReorderable() {
        assertFalse(ReorderableEntity.class.isAssignableFrom(AppGrupEntity.class));
    }

    @Test
    @DisplayName("AppGrup and AppGrupEntity should preserve ordre property")
    void testAppGrupOrdrePropertyPreserved() {
        AppGrup model = new AppGrup();
        model.setOrdre(5L);
        assertEquals(5L, model.getOrdre());

        AppGrupEntity entity = new AppGrupEntity();
        entity.setOrdre(10L);
        assertEquals(10L, entity.getOrdre());
    }
}
