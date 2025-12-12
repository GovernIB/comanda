package es.caib.comanda.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * Client pel servei de estadística.
 *
 * @author Límit Tecnologies
 */
@FeignClient(value = "estadistica", url = "${es.caib.comanda.client.base.url}/fets")
public interface EstadisticaServiceClient {
}
