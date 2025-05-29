package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

/**
 * Repositori per accedir i gestionar dades de l'entitat `EstadisticaSimpleWidgetEntity`.
 *
 * Aquesta interfície hereta de `BaseRepository` per oferir operacions genèriques sobre entitats JPA i funcionalitats de consulta avançada.
 * Representa el punt de connexió entre l'aplicació i la capa de persistència per als widgets simples d'estadística.
 *
 * @author Límit Tecnologies
 */
public interface EstadisticaSimpleWidgetRepository extends BaseRepository<EstadisticaSimpleWidgetEntity, Long> {

}