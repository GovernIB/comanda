package es.caib.comanda.acl.logic.service;

import es.caib.comanda.acl.persist.entity.AclEntryMapEntity;
import es.caib.comanda.client.model.acl.AclAction;
import es.caib.comanda.client.model.acl.AclEffect;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.client.model.acl.SubjectType;
import es.caib.comanda.acl.persist.repository.AclEntryMapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AclEntryServiceImplTest {

    @Mock
    private AclEntryMapRepository aclEntryMapRepository;
    @Mock
    private MutableAclService mutableAclService;
    @Mock
    private Acl acl; // utilitzat per simular Spring ACL

    private AclEntryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AclEntryServiceImpl(aclEntryMapRepository, mutableAclService);
    }

    @Test
    void checkPermission_returnsTrue_whenSpringAclGrants() {
        // Spring ACL resol correctament i concedeix permís
        when(mutableAclService.readAclById(any(), anyList())).thenReturn(acl);
        when(acl.isGranted(anyList(), anyList(), anyBoolean())).thenReturn(true);

        boolean allowed = service.checkPermission(
                "user1",
                Arrays.asList("ROLE_A", "ROLE_B"),
                ResourceType.ENTORN_APP,
                10L,
                AclAction.READ);

        assertThat(allowed).isTrue();
        verifyNoInteractions(aclEntryMapRepository);
    }

    @Test
    void checkPermission_returnsFalse_whenSpringAclNotFound() {
        when(mutableAclService.readAclById(any(), anyList())).thenThrow(new NotFoundException("no acl"));

        boolean allowed = service.checkPermission(
                "user1",
                java.util.Collections.singletonList("ROLE_A"),
                ResourceType.DASHBOARD,
                22L,
                AclAction.READ);

        assertThat(allowed).isFalse();
        verifyNoInteractions(aclEntryMapRepository);
    }

    @Test
    void syncSpringAclForResource_buildsAcesFromRepository() throws Exception {
        // Preparem entrades al mapping per a reconstruir ACEs
        AclEntryMapEntity eUserAllowRead = new AclEntryMapEntity();
        eUserAllowRead.setSubjectType(SubjectType.USER);
        eUserAllowRead.setSubjectValue("userZ");
        eUserAllowRead.setAction(AclAction.READ);
        eUserAllowRead.setEffect(AclEffect.ALLOW);
        AclEntryMapEntity eRoleDenyWrite = new AclEntryMapEntity();
        eRoleDenyWrite.setSubjectType(SubjectType.ROLE);
        eRoleDenyWrite.setSubjectValue("ROLE_Y");
        eRoleDenyWrite.setAction(AclAction.WRITE);
        eRoleDenyWrite.setEffect(AclEffect.DENY);
        when(aclEntryMapRepository.findAllByResource(ResourceType.ENTORN_APP, 99L))
                .thenReturn(java.util.Arrays.asList(eUserAllowRead, eRoleDenyWrite));

        // Forcem que no existeixi l'ACL prèviament
        when(mutableAclService.readAclById(any())).thenThrow(new NotFoundException("no acl"));

        // Simulem un MutableAcl per comptar els insertAce
        org.springframework.security.acls.model.MutableAcl mockMutableAcl = mock(org.springframework.security.acls.model.MutableAcl.class);
        when(mutableAclService.createAcl(any())).thenReturn(mockMutableAcl);

        // Invoquem el mètode privat via reflexió
        java.lang.reflect.Method m = AclEntryServiceImpl.class.getDeclaredMethod("syncSpringAclForResource", ResourceType.class, Long.class);
        m.setAccessible(true);
        m.invoke(service, ResourceType.ENTORN_APP, 99L);

        // S'han d'inserir ACEs per READ (ALLOW) i per WRITE+ADMIN (DENY)
        verify(mockMutableAcl, atLeast(2)).insertAce(anyInt(), any(Permission.class), any(Sid.class), anyBoolean());
        verify(mutableAclService).updateAcl(mockMutableAcl);
    }
}
