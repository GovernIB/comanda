package es.caib.comanda.permisos;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;

import static org.assertj.core.api.Assertions.assertThat;

class ComandaPermisosAppTest {

    @Test
    void comandaPermisosApp_declaraElsPaquetsBaseDelMicroservei() {
        // Verifica que el punt d'entrada declara els paquets base que ha d'escanejar el microservei.
        ComponentScan componentScan = ComandaPermisosApp.class.getAnnotation(ComponentScan.class);

        assertThat(componentScan.value()).contains("es.caib.comanda.permisos");
        assertThat(componentScan.value()).contains("es.caib.comanda.ms", "es.caib.comanda.client");
    }
}
