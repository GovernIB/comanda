package es.caib.comanda.acl.back.controller;

import es.caib.comanda.acl.logic.intf.model.ResourceType;
import es.caib.comanda.acl.logic.intf.service.AclEntryService;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AclEntryControllerTest {

    @Test
    void anyPermissionGranted_delegaLaComprovacioAlServei() {
        // Verifica que el controller delega la comprovació de permisos al servei ACL.
        AclEntryService service = mock(AclEntryService.class);
        AclEntryController controller = new AclEntryController(service);
        when(service.anyPermissionGranted(ResourceType.ENTORN_APP, 7L, List.of(PermissionEnum.READ), "anna", List.of("COM_USER"))).thenReturn(true);

        var response = controller.anyPermissionGranted(ResourceType.ENTORN_APP, 7L, List.of(PermissionEnum.READ), "anna", List.of("COM_USER"));

        assertThat(response.getBody()).isTrue();
    }

    @Test
    void findIdsWithAnyPermission_delegaLaCercaDIdentificadorsAlServei() {
        // Comprova que el controller retorna els ids resolts pel servei ACL.
        AclEntryService service = mock(AclEntryService.class);
        AclEntryController controller = new AclEntryController(service);
        when(service.findIdsWithAnyPermission(ResourceType.DASHBOARD, List.of(PermissionEnum.WRITE), "anna", List.of("COM_USER"))).thenReturn(Set.of(3L));

        var response = controller.findIdsWithAnyPermission(ResourceType.DASHBOARD, List.of(PermissionEnum.WRITE), "anna", List.of("COM_USER"));

        assertThat(response.getBody()).containsExactly(3L);
    }
}
