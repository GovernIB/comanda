package es.caib.comanda.permisos.logic.service;

import es.caib.comanda.permisos.logic.intf.model.Objecte;
import es.caib.comanda.permisos.persist.entity.ObjecteEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ObjecteServiceImplTest {

    @Test
    void getResourceClass_retornaElTipusDeResourceObjecte() {
        // Verifica la resolució del tipus genèric de resource del servei d'objectes.
        ObjecteServiceImpl service = new ObjecteServiceImpl();

        Class<?> resourceClass = ReflectionTestUtils.invokeMethod(service, "getResourceClass");

        assertThat(resourceClass).isEqualTo(Objecte.class);
    }

    @Test
    void getEntityClass_retornaElTipusDentitatObjecte() {
        // Comprova la resolució del tipus genèric d'entitat del servei d'objectes.
        ObjecteServiceImpl service = new ObjecteServiceImpl();

        Class<?> entityClass = ReflectionTestUtils.invokeMethod(service, "getEntityClass");

        assertThat(entityClass).isEqualTo(ObjecteEntity.class);
    }
}
