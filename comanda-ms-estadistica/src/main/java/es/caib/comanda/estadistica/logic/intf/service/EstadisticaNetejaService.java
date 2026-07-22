package es.caib.comanda.estadistica.logic.intf.service;

/**
 * Servei per a la neteja de dades estadístiques associades a una aplicació-entorn.
 *
 * @author Límit Tecnologies
 */
public interface EstadisticaNetejaService {

    void netejaPerEntornApp(Long entornAppId);

}
