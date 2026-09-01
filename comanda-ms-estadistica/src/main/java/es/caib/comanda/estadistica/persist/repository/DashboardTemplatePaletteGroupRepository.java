package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaGrupPaletesEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;

import java.util.List;

public interface DashboardTemplatePaletteGroupRepository extends BaseRepository<PlantillaGrupPaletesEntity, Long> {

    List<PlantillaGrupPaletesEntity> findByPlantillaId(Long PlantillaId);

    /** Grups (de qualsevol plantilla) que utilitzen la paleta indicada, com a paleta de widget o de gràfic. */
    List<PlantillaGrupPaletesEntity> findByWidgetPaletteIdOrChartPaletteId(Long widgetPaletteId, Long chartPaletteId);
}
