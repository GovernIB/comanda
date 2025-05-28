package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.EstadisticaWidgetEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

/**
 * Repositori per a accedir i gestionar entitats de tipus `EstadisticaWidgetEntity` a la base de dades.
 *
 * Aquesta interfície extén de `BaseRepository` i proporciona funcionalitats bàsiques per a persistència i consulta de dades
 * relatives als widgets d'estadística en format taula. Les operacions inclouen la creació, lectura, actualització i eliminació
 * d'entitats `EstadisticaTaulaWidgetEntity`.
 *
 * Autor: Límit Tecnologies
 */
public interface EstadisticaWidgetRepository extends BaseRepository<EstadisticaWidgetEntity, Long> {

}