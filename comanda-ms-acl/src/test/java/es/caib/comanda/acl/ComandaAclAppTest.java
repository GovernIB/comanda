package es.caib.comanda.acl;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;

import static org.assertj.core.api.Assertions.assertThat;

class ComandaAclAppTest {

    @Test
    void comandaAclApp_declaraElsPaquetsBaseDelMicroservei() {
        // Comprova que l'aplicació ACL escaneja els paquets base esperats del microservei.
        ComponentScan componentScan = ComandaAclApp.class.getAnnotation(ComponentScan.class);

        assertThat(componentScan.value()).contains("es.caib.comanda.ms", "es.caib.comanda.acl", "es.caib.comanda.client");
    }
}
