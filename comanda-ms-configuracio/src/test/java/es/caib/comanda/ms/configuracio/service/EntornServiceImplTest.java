package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.configuracio.logic.helper.EntornAppHelper;
import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.configuracio.logic.service.EntornServiceImpl;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.repository.EntornRepository;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ENTORN_CACHE;

@ExtendWith(MockitoExtension.class)
public class EntornServiceImplTest {

    static class TestableEntornServiceImpl extends EntornServiceImpl {
        public TestableEntornServiceImpl(EntornRepository entornRepository,
                                         CacheHelper cacheHelper,
                                         EntornAppHelper entornAppHelper,
                                         ApplicationEventPublisher eventPublisher) {
            super(entornRepository, cacheHelper, entornAppHelper, eventPublisher);
        }

        @Override
        public void afterCreateSave(EntornEntity entity, Entorn resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterCreateSave(entity, resource, answers, anyOrderChanged);
        }

        @Override
        public void afterUpdateSave(EntornEntity entity, Entorn resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        }

        @Override
        public void afterDelete(EntornEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
            super.afterDelete(entity, answers);
        }
    }

    @Mock
    private EntornRepository entornRepository;

    @Mock
    private CacheHelper cacheHelper;

    @Mock
    private EntornAppHelper entornAppHelper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TestableEntornServiceImpl entornService;

    private EntornEntity entornEntity;
    private Entorn entornResource;

    @BeforeEach
    void setUp() {
        entornService = new TestableEntornServiceImpl(
                entornRepository,
                cacheHelper,
                entornAppHelper,
                eventPublisher);

        // Setup test data
        entornEntity = new EntornEntity();
        entornEntity.setId(1L);
        entornEntity.setNom("Test Entorn");
        entornEntity.setEntornAppEntities(new HashSet<>());

        entornResource = new Entorn();
        entornResource.setId(1L);
        entornResource.setNom("Test Entorn");
    }

    @Test
    void testEntornServiceExists() {
        assertNotNull(entornService);
    }

    @Test
    void testAfterCreateSave() {
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        entornService.afterCreateSave(entornEntity, entornResource, answers, false);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(ComandaSseEventTypes.ENTORN_CHANGED, captor.getValue().getEvent().getType());
        assertEquals(entornEntity.getId(), captor.getValue().getEvent().getPayload());
    }

    @Test
    void testAfterUpdateSave() {
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        entornService.afterUpdateSave(entornEntity, entornResource, answers, false);

        verify(cacheHelper, times(1)).evictCacheItem(ENTORN_CACHE, entornEntity.getId().toString());

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(ComandaSseEventTypes.ENTORN_CHANGED, captor.getValue().getEvent().getType());
        assertEquals(entornEntity.getId(), captor.getValue().getEvent().getPayload());
    }

    @Test
    void testAfterDelete() {
        EntornAppEntity entornAppEntity = new EntornAppEntity();
        entornAppEntity.setId(10L);
        entornEntity.setEntornAppEntities(Set.of(entornAppEntity));

        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        entornService.afterDelete(entornEntity, answers);

        verify(cacheHelper, times(1)).evictCacheItem(ENTORN_CACHE, entornEntity.getId().toString());
        verify(entornAppHelper, times(1)).logicAfterDelete(10L);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(ComandaSseEventTypes.ENTORN_CHANGED, captor.getValue().getEvent().getType());
        assertEquals(entornEntity.getId(), captor.getValue().getEvent().getPayload());
    }
}
