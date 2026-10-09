package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

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

    /**
     * Cerca un dashboard pel seu títol.
     * 
     * @param titol Títol del dashboard
     * @return L'entitat del dashboard o null si no existeix
     */
    DashboardEntity findByTitol(String titol);

    /**
     * Cerca els dashboards associats a una aplicació i un entorn concrets.
     *
     * @param appId Identificador de l'aplicació
     * @param entornId Identificador de l'entorn
     * @return Llista de dashboards de l'aplicació i l'entorn
     */
    List<DashboardEntity> findByAppIdAndEntornId(Long appId, Long entornId);

}
