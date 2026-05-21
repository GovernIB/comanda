package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.paleta.WidgetStylePropertyEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

public interface WidgetStylePropertyRepository extends BaseRepository<WidgetStylePropertyEntity, Long> {

    List<WidgetStylePropertyEntity> findByPlantillaId(Long plantillaId);
}
