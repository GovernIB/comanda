package es.caib.comanda.acl.logic.helper;

import es.caib.comanda.acl.logic.config.AclConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

import static es.caib.comanda.ms.logic.intf.permission.PermissionEnum.READ;
import static es.caib.comanda.ms.logic.intf.permission.PermissionEnum.WRITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AclHelperTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserSids_retornaBuitQuanNoHiHaAutenticacio() {
        // Exercita el ramal on no hi ha context de seguretat actiu.
        AclHelper helper = new AclHelper(mock(AclConfig.class), mock(DataSource.class), mock(MutableAclService.class));

        assertThat(helper.getCurrentUserSids()).isEmpty();
    }

    @Test
    void getCurrentUserSids_inclouSidDePrincipalIRolQuanHiHaAutenticacio() {
        // Comprova que els SIDs actuals inclouen tant l'usuari autenticat com els rols.
        AclHelper helper = new AclHelper(mock(AclConfig.class), mock(DataSource.class), mock(MutableAclService.class));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("acl.user", "pwd", "ROLE_ADMIN"));

        List<Sid> sids = helper.getCurrentUserSids();

        assertThat(sids).hasSize(2);
        assertThat(sids.get(0)).isInstanceOf(PrincipalSid.class);
        assertThat(sids.get(1)).isInstanceOf(GrantedAuthoritySid.class);
    }

    @Test
    void anyPermissionGranted_retornaFalseQuanNoExisteixAcl() {
        // Verifica que la comprovació de permisos falla de forma segura si l'ACL no existeix.
        MutableAclService mutableAclService = mock(MutableAclService.class);
        when(mutableAclService.readAclById(any(ObjectIdentityImpl.class), any(List.class))).thenThrow(new NotFoundException("missing"));
        AclHelper helper = new AclHelper(mock(AclConfig.class), mock(DataSource.class), mutableAclService);

        boolean granted = helper.anyPermissionGranted(String.class, 5L, List.<Permission>of(), new PrincipalSid("acl.user"));

        assertThat(granted).isFalse();
    }

    @Test
    void anyPermissionGranted_retornaTrueQuanAclConcedeixElPermis() {
        // Comprova el camí feliç de lectura d'ACL i concessió d'algun permís sobre el recurs.
        MutableAclService mutableAclService = mock(MutableAclService.class);
        MutableAcl acl = mock(MutableAcl.class);
        AclHelper helper = new AclHelper(mock(AclConfig.class), mock(DataSource.class), mutableAclService);
        Sid sid = new PrincipalSid("user1");
        when(mutableAclService.readAclById(any(ObjectIdentityImpl.class), any(List.class))).thenReturn(acl);
        when(acl.isGranted(any(List.class), any(List.class), eq(true))).thenReturn(true);

        boolean granted = helper.anyPermissionGranted(String.class, 12L, List.of(BasePermission.READ), sid);

        assertThat(granted).isTrue();
    }

    @Test
    void findIdsWithAnyPermission_retornaIdsQuanHiHaAutenticacioIPermisos() {
        // Comprova la consulta d'ids ACL quan hi ha usuari autenticat i permisos explícits.
        AclConfig aclConfig = mock(AclConfig.class);
        AclHelper helper = new AclHelper(aclConfig, mock(DataSource.class), mock(MutableAclService.class));
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        ReflectionTestUtils.setField(helper, "jdbcTemplate", jdbcTemplate);
        when(aclConfig.getIdsWithPermissionQuery(true, true, true)).thenReturn("select 1");
        when(jdbcTemplate.query(anyString(), anyMap(), any(org.springframework.jdbc.core.ResultSetExtractor.class))).thenReturn(Set.of(9L, 10L));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("acl.user", "pwd", "ROLE_ADMIN"));

        Set<java.io.Serializable> ids = helper.findIdsWithAnyPermission(String.class, List.of(mock(Permission.class)));

        assertThat(ids).containsExactlyInAnyOrder(9L, 10L);
    }

    @Test
    void findIdsWithAnyPermission_retornaBuitQuanNoHiHaAuthNiPermisos() {
        // Exercita el ramal on la query ACL es construeix sense autenticació ni permisos explícits.
        AclConfig aclConfig = mock(AclConfig.class);
        AclHelper helper = new AclHelper(aclConfig, mock(DataSource.class), mock(MutableAclService.class));
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);
        ReflectionTestUtils.setField(helper, "jdbcTemplate", jdbcTemplate);
        when(aclConfig.getIdsWithPermissionQuery(false, false, false)).thenReturn("select 1");
        when(jdbcTemplate.query(anyString(), anyMap(), any(org.springframework.jdbc.core.ResultSetExtractor.class))).thenReturn(Set.of());

        Set<java.io.Serializable> ids = helper.findIdsWithAnyPermission(String.class, null);

        assertThat(ids).isEmpty();
    }

    @Test
    void get_retornaNullQuanAclNoExisteixISenseAutocreacio() {
        // Verifica que el helper retorna null quan no hi ha ACL per al recurs i no s'ha de crear.
        MutableAclService mutableAclService = mock(MutableAclService.class);
        when(mutableAclService.readAclById(any(ObjectIdentityImpl.class))).thenThrow(new NotFoundException("missing"));
        AclHelper helper = new AclHelper(mock(AclConfig.class), mock(DataSource.class), mutableAclService);

        MutableAcl acl = (MutableAcl) helper.get(String.class, 8L, null);

        assertThat(acl).isNull();
    }

    @Test
    void get_retornaAclQuanExisteixElRecursProtegit() {
        // Comprova que el helper recupera l'ACL existent quan el recurs està protegit.
        MutableAclService mutableAclService = mock(MutableAclService.class);
        MutableAcl acl = mock(MutableAcl.class);
        when(mutableAclService.readAclById(any(ObjectIdentityImpl.class), any(List.class))).thenReturn(acl);
        AclHelper helper = new AclHelper(mock(AclConfig.class), mock(DataSource.class), mutableAclService);

        org.springframework.security.acls.model.Acl found = helper.get(String.class, 12L, List.of(new PrincipalSid("user1")));

        assertThat(found).isSameAs(acl);
    }

    @Test
    void set_creaAclINouAceQuanNoHiHaCapEntradaPrèvia() {
        // Exercita la creació d'una ACL nova i l'alta de nous permisos per a un rol.
        MutableAclService mutableAclService = mock(MutableAclService.class);
        MutableAcl acl = mock(MutableAcl.class);
        when(acl.getEntries()).thenReturn(new java.util.ArrayList<>());
        when(mutableAclService.readAclById(any(ObjectIdentityImpl.class), any(List.class))).thenThrow(new NotFoundException("missing"));
        when(mutableAclService.createAcl(any(ObjectIdentityImpl.class))).thenReturn(acl);
        AclHelper helper = new AclHelper(mock(AclConfig.class), mock(DataSource.class), mutableAclService);

        helper.set(String.class, 44L, "ROLE_ADMIN", true, List.of(READ, WRITE));

        verify(acl).insertAce(any(Integer.class), eq(BasePermission.READ), eq(new GrantedAuthoritySid("ROLE_ADMIN")), eq(true));
        verify(acl).insertAce(any(Integer.class), eq(BasePermission.WRITE), eq(new GrantedAuthoritySid("ROLE_ADMIN")), eq(true));
        verify(mutableAclService, atLeastOnce()).updateAcl(acl);
    }

    @Test
    void delete_esborraElsAceExistentsDelSidIndicats() {
        // Comprova que el helper elimina les entrades ACL existents del SID objectiu.
        MutableAclService mutableAclService = mock(MutableAclService.class);
        MutableAcl acl = mock(MutableAcl.class);
        AccessControlEntry existingAce = mock(AccessControlEntry.class);
        GrantedAuthoritySid roleSid = new GrantedAuthoritySid("ROLE_ADMIN");
        when(existingAce.getSid()).thenReturn(roleSid);
        when(existingAce.getPermission()).thenReturn(BasePermission.READ);
        when(acl.getEntries()).thenReturn(new java.util.ArrayList<>(List.of(existingAce)));
        when(mutableAclService.readAclById(any(ObjectIdentityImpl.class), any(List.class))).thenReturn(acl);
        AclHelper helper = new AclHelper(mock(AclConfig.class), mock(DataSource.class), mutableAclService);

        helper.delete(String.class, 44L, "ROLE_ADMIN", true);

        verify(acl, atLeastOnce()).deleteAce(any(Integer.class));
        verify(mutableAclService, atLeastOnce()).updateAcl(acl);
    }
}
