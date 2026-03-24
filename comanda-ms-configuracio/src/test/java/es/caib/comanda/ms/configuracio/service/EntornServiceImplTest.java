package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.configuracio.logic.service.EntornServiceImpl;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.EntornRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EntornServiceImplTest {

    @Mock
    private EntornRepository entornRepository;

    @Mock
    private EntornAppRepository entornAppRepository;

    @Mock
    private CacheHelper cacheHelper;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    @InjectMocks
    private EntornServiceImpl entornService;

    private EntornEntity entornEntity;
    private Entorn entornResource;

    @BeforeEach
    void setUp() {
        // Setup test data
        entornEntity = new EntornEntity();
        entornEntity.setId(1L);
        entornEntity.setNom("Test Entorn");
        
        entornResource = new Entorn();
        entornResource.setId(1L);
        entornResource.setNom("Test Entorn");
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer test");
        when(authenticationHelper.isCurrentUserInRole(anyString())).thenReturn(false);
        when(authenticationHelper.getCurrentUserName()).thenReturn("anna");
        when(authenticationHelper.getCurrentUserRoles()).thenReturn(new String[]{"COM_USER"});
    }

    @Test
    void testEntornServiceExists() {
        // This is a simple test to verify that the service can be instantiated
        // Since EntornServiceImpl doesn't override any methods from BaseMutableResourceService,
        // we don't need to test any specific functionality
        assertNotNull(entornService);
    }

    @Test
    void additionalSpringFilter_quanNoHiHaPermisos_retornaFiltreBuitValid() {
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(null));
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.ENTORN_APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(entornAppRepository.findByActivaTrueAndAppActivaTrue()).thenReturn(List.of());

        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(entornService, "additionalSpringFilter", null, null);

        assertEquals("id:0", result);
    }
}
