package es.caib.comanda.acl.logic.service;

import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.persist.entity.AclEntryMapEntity;
import es.caib.comanda.client.model.acl.AclAction;
import es.caib.comanda.client.model.acl.AclEffect;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.client.model.acl.SubjectType;
import es.caib.comanda.acl.persist.repository.AclEntryMapRepository;
import es.caib.comanda.ms.logic.helper.ObjectMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.helper.ResourceReferenceToEntityHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AclEntryServiceCrudTest {

    @Mock
    private AclEntryMapRepository aclEntryMapRepository;
    @Mock
    private MutableAclService mutableAclService;

    // Mocks to inject into BaseMutableResourceService
    @Mock
    private BaseRepository<AclEntryMapEntity, Long> entityRepository;
    @Mock
    private ResourceEntityMappingHelper resourceEntityMappingHelper;
    @Mock
    private ResourceReferenceToEntityHelper resourceReferenceToEntityHelper;
    @Mock
    private ObjectMappingHelper objectMappingHelper;

    private AclEntryServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new AclEntryServiceImpl(aclEntryMapRepository, mutableAclService);
        // Inject BaseReadonly/BaseMutable dependencies via reflection
        inject(service, "entityRepository", entityRepository);
        inject(service, "resourceEntityMappingHelper", resourceEntityMappingHelper);
        inject(service, "resourceReferenceToEntityHelper", resourceReferenceToEntityHelper);
        inject(service, "objectMappingHelper", objectMappingHelper);
    }

    @Test
    void create_shouldPersistMapping_andSyncSpringAcl() {
        // Given a new ACL entry resource
        AclEntry resource = buildResource(SubjectType.USER, "john", ResourceType.ENTORN_APP, 100L, AclAction.WRITE, AclEffect.ALLOW);

        // Prepare mapping entity returned from helper and repository
        AclEntryMapEntity toSave = buildEntity(resource);

        when(resourceReferenceToEntityHelper.getReferencedEntitiesForResource(any(), any())).thenReturn(Collections.emptyMap());
        when(resourceEntityMappingHelper.resourceToEntity(eq(resource), isNull(), any(), any())).thenReturn(toSave);
        // updateEntityWithResource: no-op
        doAnswer(inv -> null).when(resourceEntityMappingHelper).updateEntityWithResource(any(), any(), any());

        // Repository save/refresh/merge path
        when(entityRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        doAnswer(inv -> null).when(entityRepository).refresh(any());
        when(entityRepository.merge(any())).thenAnswer(inv -> inv.getArgument(0));

        // Entity to resource conversion
        when(resourceEntityMappingHelper.entityToResource(any(AclEntryMapEntity.class), eq(AclEntry.class))).thenReturn(resource);

        // Spring ACL sync path: no previous ACL → createAcl, then updateAcl
        when(mutableAclService.readAclById(any())).thenThrow(new NotFoundException("no acl"));
        MutableAcl mockMutableAcl = mock(MutableAcl.class);
        when(mutableAclService.createAcl(any())).thenReturn(mockMutableAcl);
        when(aclEntryMapRepository.findAllByResource(ResourceType.ENTORN_APP, 100L)).thenReturn(Collections.singletonList(toSave));

        // When
        AclEntry created = service.create(resource, new HashMap<String, AnswerRequiredException.AnswerValue>());

        // Then
        assertThat(created).isNotNull();
        // Verify repository persisted entity
        verify(entityRepository).saveAndFlush(any(AclEntryMapEntity.class));
        // Verify sync built ACEs and updated ACL
        verify(mutableAclService).createAcl(any());
        verify(mutableAclService).updateAcl(any());
        verify(aclEntryMapRepository).findAllByResource(ResourceType.ENTORN_APP, 100L);
    }

    @Test
    void update_shouldPersist_andSyncSpringAcl() throws Exception {
        // Existing entity in DB
        AclEntry existing = buildResource(SubjectType.ROLE, "ROLE_ADMIN", ResourceType.DASHBOARD, 55L, AclAction.ADMIN, AclEffect.DENY);
        existing.setId(10L);
        AclEntryMapEntity existingEntity = buildEntity(existing);
        existingEntity.setId(10L);

        // entity lookup (BaseReadonlyResourceService.getEntity uses Specification findOne)
        when(entityRepository.findOne((org.springframework.data.jpa.domain.Specification<AclEntryMapEntity>) any()))
                .thenReturn(Optional.of(existingEntity));

        when(resourceReferenceToEntityHelper.getReferencedEntitiesForResource(any(), any())).thenReturn(Collections.emptyMap());
        doAnswer(inv -> null).when(resourceEntityMappingHelper).updateEntityWithResource(any(), any(), any());
        when(entityRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        doAnswer(inv -> null).when(entityRepository).refresh(any());
        when(entityRepository.merge(any())).thenAnswer(inv -> inv.getArgument(0));
        when(resourceEntityMappingHelper.entityToResource(any(AclEntryMapEntity.class), eq(AclEntry.class))).thenReturn(existing);

        // Spring ACL: assume ACL exists already
        MutableAcl mockMutableAcl = mock(MutableAcl.class);
        when(mutableAclService.readAclById(any())).thenReturn(mockMutableAcl);
        when(aclEntryMapRepository.findAllByResource(ResourceType.DASHBOARD, 55L)).thenReturn(Collections.singletonList(existingEntity));

        // When
        AclEntry updated = service.update(10L, existing, Collections.emptyMap());

        // Then
        assertThat(updated).isNotNull();
        verify(entityRepository).saveAndFlush(any(AclEntryMapEntity.class));
        verify(mutableAclService, never()).createAcl(any());
        verify(mutableAclService).updateAcl(mockMutableAcl);
        verify(aclEntryMapRepository).findAllByResource(ResourceType.DASHBOARD, 55L);
    }

    @Test
    void delete_shouldRemove_andSyncSpringAclUsingPreviousEntityData() throws Exception {
        // Existing entity before delete
        AclEntryMapEntity entity = new AclEntryMapEntity();
        entity.setId(77L);
        entity.setSubjectType(SubjectType.USER);
        entity.setSubjectValue("mary");
        entity.setResourceType(ResourceType.ENTORN_APP);
        entity.setResourceId(200L);
        entity.setAction(AclAction.READ);
        entity.setEffect(AclEffect.ALLOW);

        when(entityRepository.findOne((org.springframework.data.jpa.domain.Specification<AclEntryMapEntity>) any())).thenReturn(Optional.of(entity));
        // delete path
        doAnswer(inv -> null).when(entityRepository).delete(any());

        // Spring ACL
        when(mutableAclService.readAclById(any())).thenThrow(new NotFoundException("no acl"));
        MutableAcl mockMutable = mock(MutableAcl.class);
        when(mutableAclService.createAcl(any())).thenReturn(mockMutable);
        when(aclEntryMapRepository.findAllByResource(ResourceType.ENTORN_APP, 200L)).thenReturn(Collections.emptyList());

        // When
        service.delete(77L, Collections.emptyMap());

        // Then
        verify(entityRepository).delete(any(AclEntryMapEntity.class));
        // Sync must be executed using resourceType/id captured before delete
        verify(aclEntryMapRepository).findAllByResource(ResourceType.ENTORN_APP, 200L);
        verify(mutableAclService).updateAcl(mockMutable);
    }

    // Helpers
    private static AclEntry buildResource(SubjectType subjectType, String subjectValue, ResourceType rt, Long rid, AclAction action, AclEffect effect) {
        AclEntry r = new AclEntry();
        r.setSubjectType(subjectType);
        r.setSubjectValue(subjectValue);
        r.setResourceType(rt);
        r.setResourceId(rid);
        r.setAction(action);
        r.setEffect(effect);
        return r;
        }

    private static AclEntryMapEntity buildEntity(AclEntry r) {
        AclEntryMapEntity e = new AclEntryMapEntity();
        e.setSubjectType(r.getSubjectType());
        e.setSubjectValue(r.getSubjectValue());
        e.setResourceType(r.getResourceType());
        e.setResourceId(r.getResourceId());
        e.setAction(r.getAction());
        e.setEffect(r.getEffect());
        return e;
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field f = findField(target.getClass(), fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static java.lang.reflect.Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ex) {
                c = c.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
