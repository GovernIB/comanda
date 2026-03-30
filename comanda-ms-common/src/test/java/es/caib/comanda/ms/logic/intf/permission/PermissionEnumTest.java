package es.caib.comanda.ms.logic.intf.permission;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionEnumTest {

    @Test
    void toPermissionAndFromPermission_coverMappings() {
        // Comprova la conversió bidireccional entre PermissionEnum i els permisos Spring ACL.
        for (PermissionEnum permissionEnum : PermissionEnum.values()) {
            var permission = PermissionEnum.toPermission(permissionEnum);
            if (permissionEnum == PermissionEnum.SYNCHRONIZATION || permissionEnum == PermissionEnum.NULL) {
                assertThat(permission).isNull();
            } else {
                assertThat(PermissionEnum.fromPermission(permission)).isEqualTo(permissionEnum);
            }
        }

        assertThat(PermissionEnum.fromPermission(null)).isNull();
    }
}
