package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.model.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaSimpleWidgetService;
import es.caib.comanda.estadistica.persist.entity.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei per gestionar widgets d'estadística simple.
 * Aquesta classe proporciona funcionalitats de només lectura per interactuar amb dades de widgets d'estadística simple.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class EstadisticaSimpleWidgetServiceImpl extends BaseReadonlyResourceService<EstadisticaSimpleWidget, Long, EstadisticaSimpleWidgetEntity> implements EstadisticaSimpleWidgetService {

    @Autowired
    EstadisticaClientHelper estadisticaClientHelper;

    @Override
    protected void afterConversion(EstadisticaSimpleWidgetEntity entity, EstadisticaSimpleWidget resource) {
        super.afterConversion(entity, resource);
        try {
            var entornApp = estadisticaClientHelper.entornAppFindById(entity.getEntornAppId());
            if (entornApp != null) {
                resource.setEntornNom(entornApp.getEntorn().getNom());
                resource.setAplicacioNom(entornApp.getApp().getNom());
            }
        } catch (Exception e) {
            log.error("Error obtenint noms d'aplicació i entorn de l'entornApp amb id=" + entity.getEntornAppId(), e);
        }
    }

}
