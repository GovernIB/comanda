package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.Indicador;
import es.caib.comanda.estadistica.logic.intf.service.IndicadorService;
import es.caib.comanda.estadistica.persist.entity.IndicadorEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Classe de servei que implementa la interfície IndicadorService. Aquesta classe proporciona operacions específiques per gestionar
 * els recursos d'Indicador.
 *
 * Estén la classe BaseReadonlyResourceService per heretar funcionalitats comunes de gestió de recursos de només lectura, com ara la
 * recuperació d'informació d'Indicadors des de la base de dades.
 *
 * Utilitza l'annotació @Service, indicant que és un component de servei en el context de Spring.
 * També utilitza @Slf4j per habilitar el registre de logs en aquesta classe.
 *
 * La classe treballa amb entitats d'Indicador (model de negoci), identificadors de tipus Long, i entitats persistents IndicadorEntity.
 * Forma part del mòdul d'estadística dins de l'aplicació comanda.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class IndicadorServiceImpl extends BaseReadonlyResourceService<Indicador, Long, IndicadorEntity> implements IndicadorService {
    
}
