package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.configuracio.logic.intf.model.Parametre;
import es.caib.comanda.configuracio.persist.entity.ParametreEntity;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static es.caib.comanda.ms.back.config.HazelCastCacheConfig.PARAMETRE_CACHE;
import static org.mockito.Mockito.*;

/**
 * Tests per ParametreServiceImpl
 */
class ParametreServiceImplTest {

    @Mock
    private MonitorServiceClient monitorServiceClient;
    @Mock
    private CacheHelper cacheHelper;
    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    // S'utilitza la classe real amb mocks injectats (el mètode protegit és accessible dins el mateix paquet)
    @InjectMocks
    private ParametreServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("AuthHdr");
    }

    private ParametreEntity entity(Long id, String codi) {
        ParametreEntity e = new ParametreEntity();
        e.setId(id);
        e.setCodi(codi);
        return e;
    }

    @Test
    void afterUpdateSave_evictsCache_and_programsDeletion_forPeriode() {
        ParametreEntity e = entity(10L, BaseConfig.PROP_MONITOR_BUIDAT_PERIODE_MINUTS);
        service.afterUpdateSave(e, (Parametre) null, Map.of(), false);

        verify(cacheHelper).evictCacheItem(PARAMETRE_CACHE, "10");
        verify(monitorServiceClient).programarBorrat("AuthHdr");
    }

    @Test
    void afterUpdateSave_evictsCache_and_programsDeletion_forRetencio() {
        ParametreEntity e = entity(11L, BaseConfig.PROP_MONITOR_BUIDAT_RETENCIO_DIES);
        service.afterUpdateSave(e, (Parametre) null, Map.of(), false);

        verify(cacheHelper).evictCacheItem(PARAMETRE_CACHE, "11");
        verify(monitorServiceClient).programarBorrat("AuthHdr");
    }

    @Test
    void afterUpdateSave_evictsCache_only_forOtherCodes() {
        ParametreEntity e = entity(12L, "other.code");
        service.afterUpdateSave(e, (Parametre) null, Map.of(), false);

        verify(cacheHelper).evictCacheItem(PARAMETRE_CACHE, "12");
        verify(monitorServiceClient, never()).programarBorrat(anyString());
    }
}
