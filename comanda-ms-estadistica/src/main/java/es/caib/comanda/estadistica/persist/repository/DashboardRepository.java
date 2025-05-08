package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.DashboardEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

/**
 * Interfície que defineix el repositori per a la gestió de l'entitat DashboardEntity.
 *
 * Aquesta interfície estèn BaseRepository amb DashboardEntity com a tipus d'entitat i Long com a tipus de la clau primària.
 * Permet realitzar operacions de persistència, com l'emmagatzematge, recuperació, actualització, i eliminació de dades
 * relacionades amb l'entitat DashboardEntity a la base de dades.
 *
 * Autor: Límit Tecnologies
 */
public interface DashboardRepository extends BaseRepository<DashboardEntity, Long> {

}
