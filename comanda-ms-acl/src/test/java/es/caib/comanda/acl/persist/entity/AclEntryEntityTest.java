package es.caib.comanda.acl.persist.entity;

import es.caib.comanda.acl.logic.intf.model.AclEntry;
import es.caib.comanda.acl.logic.intf.model.SubjectType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AclEntryEntityTest {

    @Test
    void isNew_retornaTrueQuanNoHiHaIdPersistida() {
        // Verifica que l'entitat ACL es considera nova si encara no té id.
        AclEntry resource = new AclEntry();
        AclEntryEntity entity = AclEntryEntity.builder().id(null).resource(resource).build();

        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void accessorsDelegats_llegeixenElsValorsDelResourceAssociat() {
        // Comprova que l'entitat delega els camps de subjecte al resource intern.
        AclEntry resource = new AclEntry();
        resource.setSubjectType(SubjectType.USER);
        resource.setSubjectValue("u1");
        AclEntryEntity entity = AclEntryEntity.builder().id("pk").resource(resource).build();

        assertThat(entity.getSubjectType()).isEqualTo(SubjectType.USER);
        assertThat(entity.getSubjectValue()).isEqualTo("u1");
        assertThat(entity.isNew()).isFalse();
    }
}
