package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationPermissionHelperTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticationHelper_retornaUsuariIRols() {
        // Comprova l'obtenció de l'usuari autenticat i dels rols disponibles.
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john");
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        AuthenticationHelper helper = new AuthenticationHelper();
        assertThat(helper.getCurrentUserName()).isEqualTo("john");
        assertThat(helper.getCurrentUserRoles()).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
        assertThat(helper.isCurrentUserInRole("ROLE_ADMIN")).isTrue();
    }

    @Test
    void basePermissionHelper_checkResourcePermission_iArtifact() {
        // Verifica la comprovació de permisos sobre recursos i artefactes amb l'helper base.
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getAuthorities()).thenReturn((Collection)List.of(new SimpleGrantedAuthority("ROLE_EDITOR")));

        TestPermissionHelper helper = new TestPermissionHelper();
        ReflectionTestUtils.setField(helper, "authenticationHelper", new AuthenticationHelper());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(helper.checkResourcePermission(auth, null, AnnotatedResource.class.getName(), (BasePermission)BasePermission.READ)).isTrue();
        assertThat(helper.checkResourcePermission(auth, null, "not.found.Clazz", (BasePermission)BasePermission.READ)).isFalse();
        assertThat(helper.checkResourceArtifactPermission(AnnotatedResource.class, ResourceArtifactType.ACTION, "RUN")).isTrue();
        assertThat(helper.checkResourceArtifactPermission(AnnotatedResource.class, ResourceArtifactType.ACTION, "MISS")).isFalse();
    }

    @Test
    void permissionHelper_customSempreFalse() {
        // Valida el comportament per defecte del helper concret quan no s'ha sobreescrit.
        PermissionHelper helper = new PermissionHelper();
        Authentication auth = mock(Authentication.class);
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(helper, "checkCustomResourceAccessConstraint", auth, AnnotatedResource.class, null, (BasePermission)BasePermission.READ)).isEqualTo(false);
        assertThat((Boolean) ReflectionTestUtils.invokeMethod(helper, "checkCustomResourceArtifactAccessConstraint", auth, AnnotatedResource.class, ResourceArtifactType.ACTION, "X", null)).isEqualTo(false);
    }

    @ResourceConfig(
            name = "annotated",
            accessConstraints = {
                    @ResourceAccessConstraint(
                            type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                            roles = {"ROLE_EDITOR"},
                            grantedPermissions = {PermissionEnum.READ, PermissionEnum.WRITE})
            },
            artifacts = {
                    @ResourceArtifact(
                            type = ResourceArtifactType.ACTION,
                            code = "RUN",
                            accessConstraints = {
                                    @ResourceAccessConstraint(type = ResourceAccessConstraint.ResourceAccessConstraintType.PERMIT_ALL)
                            })
            }
    )
    static class AnnotatedResource extends BaseResource<Long> {
    }

    static class TestPermissionHelper extends BasePermissionHelper {
        @Override
        protected boolean checkCustomResourceAccessConstraint(Authentication auth, Class<?> resourceClass, ResourceAccessConstraint resourceAccessConstraint, BasePermission permission) {
            return false;
        }

        @Override
        protected boolean checkCustomResourceArtifactAccessConstraint(Authentication auth, Class<?> resourceClass, ResourceArtifactType type, String code, ResourceAccessConstraint resourceAccessConstraint) {
            return false;
        }
    }
}
