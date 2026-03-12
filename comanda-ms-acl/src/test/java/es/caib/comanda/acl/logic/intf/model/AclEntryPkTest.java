package es.caib.comanda.acl.logic.intf.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AclEntryPkTest {

    @Test
    void serializeToStringIParseig_recuperenElsValorsOriginals() {
        // Comprova que la PK lògica d'ACL es pot serialitzar i recuperar sense pèrdua.
        AclEntry.AclEntryPk pk = new AclEntry.AclEntryPk(ResourceType.ENTORN_APP, 15L, true, "ROLE_ADMIN");

        String serialized = pk.serializeToString();
        AclEntry.AclEntryPk restored = AclEntry.AclEntryPk.deserializeFromString(serialized);

        assertThat(restored.getResourceType()).isEqualTo(ResourceType.ENTORN_APP);
        assertThat(restored.getResourceId()).isEqualTo(15L);
        assertThat(restored.isSidPrincipal()).isFalse();
        assertThat(restored.getSidName()).isEqualTo("ROLE_ADMIN");
    }
}
