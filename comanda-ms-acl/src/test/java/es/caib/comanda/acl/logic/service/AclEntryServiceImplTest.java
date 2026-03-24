package es.caib.comanda.acl.logic.service;

import es.caib.comanda.acl.logic.helper.AclHelper;
import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.logic.intf.model.ResourceType;
import es.caib.comanda.acl.logic.intf.model.SubjectType;
import es.caib.comanda.acl.persist.entity.AclEntryEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AclEntryServiceImplTest {

    @Test
    void anyPermissionGranted_converteixPermisosIConsultaElHelper() {
        // Comprova que el servei transforma PermissionEnum i construeix els SIDs a partir de l'usuari i els rols.
        AclHelper helper = mock(AclHelper.class);
        AclEntryServiceImpl service = new AclEntryServiceImpl(helper);
        when(helper.anyPermissionGranted(any(), eq(30L), anyList(), Mockito.<Sid[]>any())).thenReturn(false);

        boolean granted = service.anyPermissionGranted(ResourceType.ENTORN_APP, 30L, null, "anna", List.of("COM_USER", "COM_ADMIN"));

        assertThat(granted).isFalse();
        verify(helper).anyPermissionGranted(
                any(),
                eq(30L),
                eq(List.of()),
                Mockito.argThat((Sid[] sids) -> sids.length == 3
                        && sids[0] instanceof PrincipalSid
                        && "anna".equals(((PrincipalSid) sids[0]).getPrincipal())
                        && sids[1] instanceof GrantedAuthoritySid
                        && "COM_USER".equals(((GrantedAuthoritySid) sids[1]).getGrantedAuthority())
                        && sids[2] instanceof GrantedAuthoritySid
                        && "COM_ADMIN".equals(((GrantedAuthoritySid) sids[2]).getGrantedAuthority())));
    }

    @Test
    void findIdsWithAnyPermission_converteixPermisosINormalitzaNulls() {
        // Verifica que el servei delega la cerca d'ids amb els permisos convertits i els SIDs construïts.
        AclHelper helper = mock(AclHelper.class);
        AclEntryServiceImpl service = new AclEntryServiceImpl(helper);
        when(helper.findIdsWithAnyPermission(any(), eq(List.of()), Mockito.<Sid[]>any())).thenReturn(Set.of());

        Set<Serializable> ids = service.findIdsWithAnyPermission(ResourceType.ENTORN_APP, null, "anna", List.of("COM_USER"));

        assertThat(ids).isEmpty();
        verify(helper).findIdsWithAnyPermission(
                any(),
                eq(List.of()),
                Mockito.argThat((Sid[] sids) -> sids.length == 2
                        && sids[0] instanceof PrincipalSid
                        && "anna".equals(((PrincipalSid) sids[0]).getPrincipal())
                        && sids[1] instanceof GrantedAuthoritySid
                        && "COM_USER".equals(((GrantedAuthoritySid) sids[1]).getGrantedAuthority())));
    }

    @Test
    void entityRepositoryFindOne_retornaBuitQuanNoHiHaAclPelRecurs() {
        // Cobreix la cerca per PK quan el helper ACL no troba cap recurs protegit.
        AclHelper helper = mock(AclHelper.class);
        AclEntryServiceImpl service = new AclEntryServiceImpl(helper);
        String id = new AclEntry.AclEntryPk(ResourceType.ENTORN_APP, 22L, true, "user1").serializeToString();
        when(helper.get(any(), eq(22L), Mockito.isNull())).thenReturn(null);

        Optional<?> result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "entityRepositoryFindOne", id);

        assertThat(result).isEmpty();
    }

    @Test
    void entityRepositoryFindOne_retornaEntradaQuanElSidCoincideix() {
        // Verifica que la cerca per PK recupera l'entrada ACL corresponent al SID indicat.
        AclHelper helper = mock(AclHelper.class);
        AclEntryServiceImpl service = new AclEntryServiceImpl(helper);
        String id = new AclEntry.AclEntryPk(ResourceType.ENTORN_APP, 20L, true, "anna").serializeToString();
        Acl acl = mock(Acl.class);
        AccessControlEntry ace = mock(AccessControlEntry.class);
        when(helper.get(any(), eq(20L), Mockito.isNull())).thenReturn(acl);
        when(acl.getEntries()).thenReturn(List.of(ace));
        when(acl.getObjectIdentity()).thenReturn(new ObjectIdentityImpl("es.caib.comanda.client.model.EntornApp", 20L));
        when(ace.getSid()).thenReturn(new PrincipalSid("anna"));
        when(ace.getPermission()).thenReturn(PermissionEnum.toPermission(PermissionEnum.READ));

        Optional<AclEntryEntity> result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "entityRepositoryFindOne", id);

        assertThat(result).isPresent();
        assertThat(result.get().getResource().getSubjectValue()).isEqualTo("anna");
    }

    @Test
    void entityRepositoryFindEntities_retornaPaginaOrdenadaQuanElFiltreEsValid() {
        // Exercita la cerca filtrada d'entrades ACL i l'ordenació pel camp de subjecte.
        AclHelper helper = mock(AclHelper.class);
        AclEntryServiceImpl service = new AclEntryServiceImpl(helper);
        Acl acl = mock(Acl.class);
        AccessControlEntry ace = mock(AccessControlEntry.class);
        when(helper.get(any(), eq(20L), Mockito.isNull())).thenReturn(acl);
        when(acl.getEntries()).thenReturn(List.of(ace));
        when(acl.getObjectIdentity()).thenReturn(new ObjectIdentityImpl("es.caib.comanda.client.model.EntornApp", 20L));
        when(ace.getSid()).thenReturn(new PrincipalSid("anna"));
        when(ace.getPermission()).thenReturn(PermissionEnum.toPermission(PermissionEnum.READ));

        Page<AclEntryEntity> page = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "entityRepositoryFindEntities",
                null,
                "resourceType:ENTORN_APP and resourceId:20",
                null,
                PageRequest.of(0, 10, Sort.by("subjectValue")));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getResource().getSubjectValue()).isEqualTo("anna");
    }

    @Test
    void entityRepositoryFindEntities_retornaPaginaBuidaQuanAclNoExisteix() {
        // Comprova el ramal on el filtre és vàlid però no hi ha ACL per al recurs indicat.
        AclHelper helper = mock(AclHelper.class);
        AclEntryServiceImpl service = new AclEntryServiceImpl(helper);
        when(helper.get(any(), eq(30L), Mockito.isNull())).thenReturn(null);

        Page<AclEntryEntity> page = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "entityRepositoryFindEntities",
                null,
                "resourceType:ENTORN_APP and resourceId:30",
                null,
                PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    void entityRepositoryFindEntities_fallaQuanElFiltreNoEsSuportat() {
        // Verifica que el servei rebutja filtres amb OR perquè no formen part del contracte suportat.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));

        assertThatThrownBy(() ->
                org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                        service,
                        "entityRepositoryFindEntities",
                        null,
                        "resourceType:ENTORN_APP or resourceId:22",
                        null,
                        PageRequest.of(0, 10)))
                .hasMessageContaining("Filtre no suportat");
    }

    @Test
    void entitySaveFlushAndRefresh_activaElsPermisosMarcatsAlResource() {
        // Exercita la persistència lògica dels permisos marcats dins el resource ACL.
        AclHelper helper = mock(AclHelper.class);
        AclEntryServiceImpl service = new AclEntryServiceImpl(helper);
        AclEntry resource = resource(SubjectType.ROLE, "ROLE_ADMIN");
        resource.setReadAllowed(true);
        resource.setAdminAllowed(true);
        resource.setPerm0Allowed(true);
        AclEntryEntity entity = AclEntryEntity.builder().id("pk").resource(resource).build();

        AclEntryEntity saved = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "entitySaveFlushAndRefresh", entity);

        assertThat(saved).isSameAs(entity);
        verify(helper).set(any(), eq(1L), eq("ROLE_ADMIN"), eq(true), Mockito.argThat(list ->
                list.contains(PermissionEnum.READ) && list.contains(PermissionEnum.ADMINISTRATION) && list.contains(PermissionEnum.PERM0)));
    }

    @Test
    void entitySaveFlushAndRefresh_quanCanviaElSubject_esborraLentradaAnteriorIActualitzaLaNova() {
        // Verifica que una edició que canvia la clau lògica del permís no deixa una ACL antiga duplicada.
        AclHelper helper = mock(AclHelper.class);
        AclEntryServiceImpl service = new AclEntryServiceImpl(helper);
        AclEntry resource = resource(SubjectType.ROLE, "COM_USER");
        resource.setReadAllowed(true);
        String previousId = new AclEntry.AclEntryPk(ResourceType.ENTORN_APP, 1L, true, "anna").serializeToString();
        String newId = new AclEntry.AclEntryPk(ResourceType.ENTORN_APP, 1L, false, "COM_USER").serializeToString();
        AclEntryEntity entity = AclEntryEntity.builder().id(previousId).resource(resource).build();

        AclEntryEntity saved = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "entitySaveFlushAndRefresh", entity);

        assertThat(saved.getId()).isEqualTo(newId);
        assertThat(saved.getResource().getId()).isEqualTo(newId);
        verify(helper).delete(any(), eq(1L), eq("anna"), eq(false));
        verify(helper).set(any(), eq(1L), eq("COM_USER"), eq(true), Mockito.argThat(list ->
                list.contains(PermissionEnum.READ) && list.size() == 1));
    }

    @Test
    void entityRepositoryDelete_delegaLEsborratAlHelperAcl() {
        // Comprova que l'esborrat de l'entitat es tradueix a una eliminació ACL del mateix subjecte.
        AclHelper helper = mock(AclHelper.class);
        AclEntryServiceImpl service = new AclEntryServiceImpl(helper);
        AclEntryEntity entity = AclEntryEntity.builder().id("pk").resource(resource(SubjectType.ROLE, "ROLE_ADMIN")).build();

        org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "entityRepositoryDelete", entity);

        verify(helper).delete(any(), eq(1L), eq("ROLE_ADMIN"), eq(true));
    }

    @Test
    void entityDetachConvertAndMerge_retornaElResourceDeLentitat() {
        // Verifica que el servei retorna el resource original després de la conversió i merge lògic.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));
        AclEntry resource = resource(SubjectType.USER, "anna");
        AclEntryEntity entity = AclEntryEntity.builder().id("pk").resource(resource).build();

        AclEntry detached = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "entityDetachConvertAndMerge",
                entity,
                Map.<String, AnswerRequiredException.AnswerValue>of(),
                true);

        assertThat(detached).isSameAs(resource);
    }

    @Test
    void createGetterBasedComparator_ordenaPerSubjectValue() {
        // Exercita el comparador reflectiu quan s'ordena per una propietat coneguda del resource.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));
        Comparator<AclEntryEntity> comparator = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "createGetterBasedComparator",
                Sort.by(Sort.Order.asc("subjectValue")));
        AclEntryEntity a = AclEntryEntity.builder().resource(resource(SubjectType.USER, "anna")).build();
        AclEntryEntity b = AclEntryEntity.builder().resource(resource(SubjectType.ROLE, "zeta")).build();

        assertThat(comparator.compare(a, b)).isLessThan(0);
    }

    @Test
    void createGetterBasedComparator_retornaZeroPerPropietatDesconeguda() {
        // Comprova el comportament defensiu del comparador quan la propietat no existeix.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));
        Comparator<AclEntryEntity> comparator = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "createGetterBasedComparator",
                Sort.by(Sort.Order.asc("unknownProperty")));
        AclEntryEntity a = AclEntryEntity.builder().resource(resource(SubjectType.USER, "anna")).build();
        AclEntryEntity b = AclEntryEntity.builder().resource(resource(SubjectType.ROLE, "zeta")).build();

        assertThat(comparator.compare(a, b)).isZero();
    }

    @Test
    void getClassFromResourceType_resolEntornApp() {
        // Verifica que el servei resol el tipus de recurs ENTORN_APP a la classe Java corresponent.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));

        Class<?> resourceClass = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "getClassFromResourceType", ResourceType.ENTORN_APP);

        assertThat(resourceClass.getName()).isEqualTo("es.caib.comanda.client.model.EntornApp");
    }

    @Test
    void getClassFromResourceType_resolApp() {
        // Comprova que el servei també resol ACLs a nivell d'aplicació.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));

        Class<?> resourceClass = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "getClassFromResourceType", ResourceType.APP);

        assertThat(resourceClass.getName()).isEqualTo("es.caib.comanda.client.model.App");
    }

    @Test
    void getClassFromResourceType_retornaNullQuanLaClasseNoEsPotResoldre() {
        // Cobreix el mapping defensiu del tipus DASHBOARD quan la classe no està disponible al classpath.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));

        Class<?> dashboardClass = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "getClassFromResourceType", ResourceType.DASHBOARD);

        assertThat(dashboardClass).isNull();
    }

    @Test
    void getResourceTypeFromClassName_resolEntornApp() {
        // Comprova el mapping invers des del nom de classe fins al ResourceType.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));

        ResourceType resourceType = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "getResourceTypeFromClassName", "es.caib.comanda.client.model.EntornApp");

        assertThat(resourceType).isEqualTo(ResourceType.ENTORN_APP);
    }

    @Test
    void getResourceTypeFromClassName_resolApp() {
        // Verifica el mapping invers del model App cap al ResourceType APP.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));

        ResourceType resourceType = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "getResourceTypeFromClassName", "es.caib.comanda.client.model.App");

        assertThat(resourceType).isEqualTo(ResourceType.APP);
    }

    @Test
    void getResourceTypeFromClassName_resolDashboardIClasseDesconeguda() {
        // Exercita tant el mapping de DASHBOARD com el cas d'una classe desconeguda.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));

        ResourceType dashboardType = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "getResourceTypeFromClassName", "es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard");
        ResourceType unknownType = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "getResourceTypeFromClassName", "unknown.Class");

        assertThat(dashboardType).isEqualTo(ResourceType.DASHBOARD);
        assertThat(unknownType).isNull();
    }

    @Test
    void extractFilterTriplets_extreuElsTripletsDelFiltreSuportat() {
        // Verifica el parseig de triplets camp-operador-valor del filtre simplificat.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));

        @SuppressWarnings("unchecked")
        List<String[]> triplets = (List<String[]>) org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "extractFilterTriplets",
                "resourceType:ENTORN_APP and resourceId:15 and subjectValue:'admin'");

        assertThat(triplets).hasSize(3);
        assertThat(triplets.get(0)).containsExactly("resourceType", ":", "ENTORN_APP");
    }

    @Test
    void formAccessControlEntryToAclEntryEntity_converteixUnPrincipalAmbPermisRead() {
        // Comprova la conversió d'una ACE de principal a una entitat ACL amb permisos llegits.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));
        AccessControlEntry userAce = mock(AccessControlEntry.class);
        when(userAce.getPermission()).thenReturn(PermissionEnum.toPermission(PermissionEnum.READ));

        AclEntryEntity converted = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "formAccessControlEntryToAclEntryEntity",
                "es.caib.comanda.client.model.EntornApp",
                15L,
                new PrincipalSid("anna"),
                List.of(userAce));

        assertThat(converted.getResource().getSubjectType()).isEqualTo(SubjectType.USER);
        assertThat(converted.getResource().isReadAllowed()).isTrue();
    }

    @Test
    void formAccessControlEntryToAclEntryEntity_converteixUnRolAmbPermisWrite() {
        // Verifica la conversió d'una ACE de rol a una entitat ACL amb permís d'escriptura.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));
        AccessControlEntry roleAce = mock(AccessControlEntry.class);
        when(roleAce.getPermission()).thenReturn(PermissionEnum.toPermission(PermissionEnum.WRITE));

        AclEntryEntity converted = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service,
                "formAccessControlEntryToAclEntryEntity",
                "es.caib.comanda.client.model.EntornApp",
                15L,
                new GrantedAuthoritySid("ROLE_ADMIN"),
                List.of(roleAce));

        assertThat(converted.getResource().getSubjectType()).isEqualTo(SubjectType.ROLE);
        assertThat(converted.getResource().isWriteAllowed()).isTrue();
    }

    @Test
    void toAclEntries_retornaBuitQuanLaclNoTeEntrades() {
        // Cobreix el ramal on una ACL existeix però no té cap ACE associada.
        AclEntryServiceImpl service = new AclEntryServiceImpl(mock(AclHelper.class));
        Acl acl = mock(Acl.class);
        when(acl.getEntries()).thenReturn(null);

        List<?> entries = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "toAclEntries", acl);

        assertThat(entries).isEmpty();
    }

    private static AclEntry resource(SubjectType subjectType, String subjectValue) {
        AclEntry resource = new AclEntry();
        resource.setSubjectType(subjectType);
        resource.setSubjectValue(subjectValue);
        resource.setResourceType(ResourceType.ENTORN_APP);
        resource.setResourceId(1L);
        return resource;
    }
}
