package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.DashboardItemEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

/**
 * Repositori encarregat de gestionar operacions sobre l'entitat DashboardItemEntity en el context de persistència.
 *
 * Aquesta interfície defineix les operacions genèriques heretades de BaseRepository per manipular entitats DashboardItemEntity,
 * incloent-hi operacions CRUD, consultes personalitzades, i altres funcionalitats relacionades amb l'entitat associada.
 *
 * Autor: Límit Tecnologies
 */
public interface DashboardItemRepository extends BaseRepository<DashboardItemEntity, Long> {

}
