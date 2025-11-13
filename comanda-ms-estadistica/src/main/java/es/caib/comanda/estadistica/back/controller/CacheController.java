package es.caib.comanda.estadistica.back.controller;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.logic.intf.model.cache.ComandaCache;
import es.caib.comanda.ms.back.controller.BaseMutableResourceController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servei de consulta d'informació de dimensions estadístiques.
 *
 * @author Límit Tecnologies
 */
@RestController("cacheController")
@RequestMapping(BaseConfig.API_PATH + "/caches")
@Tag(name = "17. Cache", description = "Servei de gestió de caché")
public class CacheController extends BaseMutableResourceController<ComandaCache, String> {

}
