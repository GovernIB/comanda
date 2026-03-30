package es.caib.comanda.permisos.logic.service;

import es.caib.comanda.permisos.logic.intf.model.Permis;
import es.caib.comanda.permisos.persist.entity.PermisEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.jms.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PermisServiceImplTest {

    @Test
    void getResourceClass_retornaElTipusDeResourcePermis() {
        // Verifica la resolució del tipus genèric de resource del servei de permisos.
        PermisServiceImpl service = new PermisServiceImpl();

        Class<?> resourceClass = ReflectionTestUtils.invokeMethod(service, "getResourceClass");

        assertThat(resourceClass).isEqualTo(Permis.class);
    }

    @Test
    void getEntityClass_retornaElTipusDentitatPermis() {
        // Comprova la resolució del tipus genèric d'entitat del servei de permisos.
        PermisServiceImpl service = new PermisServiceImpl();

        Class<?> entityClass = ReflectionTestUtils.invokeMethod(service, "getEntityClass");

        assertThat(entityClass).isEqualTo(PermisEntity.class);
    }

    @Test
    void receiveMessage_faAcknowledgeDelMissatgeJms() throws Exception {
        // Verifica que el listener JMS confirma el missatge rebut després de processar-lo.
        PermisServiceImpl service = new PermisServiceImpl();
        Message message = mock(Message.class);

        service.receiveMessage(mock(es.caib.comanda.model.v1.permis.Permis.class), message);

        verify(message).acknowledge();
    }
}
