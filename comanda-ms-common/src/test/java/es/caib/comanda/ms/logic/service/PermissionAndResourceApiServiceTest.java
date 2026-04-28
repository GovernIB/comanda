package es.caib.comanda.ms.logic.service;

import es.caib.comanda.ms.logic.helper.PermissionHelper;
import es.caib.comanda.ms.logic.intf.exception.UnknownPermissionException;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.service.PermissionEvaluatorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionAndResourceApiServiceTest {

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void permissionEvaluatorService_permisosRecursIArtefacte() {
        // Verifica l'avaluació de permisos tant sobre recursos com sobre artefactes REST.
        PermissionHelper permissionHelper = mock(PermissionHelper.class);
        PermissionEvaluatorServiceImpl service = new PermissionEvaluatorServiceImpl();
        ReflectionTestUtils.setField(service, "permissionHelper", permissionHelper);

        Authentication auth = mock(Authentication.class);
        when(permissionHelper.checkResourcePermission(auth, 1L, TestResource.class.getName(), (BasePermission)BasePermission.READ)).thenReturn(true);
        boolean allowedRead = service.hasPermission(auth, 1L, TestResource.class.getName(), PermissionEvaluatorService.RestApiOperation.GET_ONE);
        assertThat(allowedRead).isTrue();

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/test/action/CODE1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(req));
        when(permissionHelper.checkResourceArtifactPermission(TestResource.class, es.caib.comanda.ms.logic.intf.model.ResourceArtifactType.ACTION, "CODE1")).thenReturn(true);
        boolean allowedAction = service.hasPermission(auth, 1L, TestResource.class.getName(), PermissionEvaluatorService.RestApiOperation.ACTION);
        assertThat(allowedAction).isTrue();
    }

    @Test
    void permissionEvaluatorService_unknownPermission_throws() {
        // Comprova que una operació desconeguda llança l'excepció adequada.
        PermissionEvaluatorServiceImpl service = new PermissionEvaluatorServiceImpl();
        ReflectionTestUtils.setField(service, "permissionHelper", mock(PermissionHelper.class));
        assertThatThrownBy(() -> service.hasPermission(mock(Authentication.class), 1L, TestResource.class.getName(), "x"))
                .isInstanceOf(UnknownPermissionException.class);
    }

    @Test
    void resourceApiService_registerFindAllowedAndPermissions() {
        // Exercita el registre de recursos, el filtratge dels permesos i el càlcul de permisos actuals.
        PermissionHelper permissionHelper = mock(PermissionHelper.class);
        ResourceApiServiceImpl service = new ResourceApiServiceImpl();
        ReflectionTestUtils.setField(service, "permissionHelper", permissionHelper);

        service.resourceRegister(TestResource.class);
        service.resourceRegister(OtherResource.class);

        when(permissionHelper.checkResourcePermission(null, TestResource.class.getName(), null)).thenReturn(true);
        when(permissionHelper.checkResourcePermission(null, OtherResource.class.getName(), null)).thenReturn(false);
        List<Class<? extends es.caib.comanda.ms.logic.intf.model.Resource<?>>> allowed = service.resourceFindAllowed();
        assertThat(allowed).containsExactly(TestResource.class);

        when(permissionHelper.checkResourcePermission(2L, TestResource.class.getName(), (BasePermission)BasePermission.READ)).thenReturn(true);
        when(permissionHelper.checkResourcePermission(2L, TestResource.class.getName(), (BasePermission)BasePermission.WRITE)).thenReturn(false);
        when(permissionHelper.checkResourcePermission(2L, TestResource.class.getName(), (BasePermission)BasePermission.CREATE)).thenReturn(true);
        when(permissionHelper.checkResourcePermission(2L, TestResource.class.getName(), (BasePermission)BasePermission.DELETE)).thenReturn(false);

        var p = service.permissionsCurrentUser(TestResource.class, 2L);
        assertThat(p.isReadGranted()).isTrue();
        assertThat(p.isWriteGranted()).isFalse();
        assertThat(p.isCreateGranted()).isTrue();
        assertThat(p.isDeleteGranted()).isFalse();
        verify(permissionHelper, times(4)).checkResourcePermission(eq(2L), eq(TestResource.class.getName()), any());
    }

    static class TestResource extends BaseResource<Long> {}
    static class OtherResource extends BaseResource<Long> {}
}
