package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.Dimensio;
import es.caib.comanda.estadistica.logic.intf.service.DimensioService;
import es.caib.comanda.estadistica.persist.entity.DimensioEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat Dimensio.
 *
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives a Dimensions,
 * i s'estén de BaseReadonlyResourceService per oferir operacions bàsiques de lògica empresarial en mode només lectura.
 *
 * Les accions específiques que es realitzen en aquesta classe estan directament relacionades amb la interfície DimensioService
 * i amb l'accés a les dades mitjançant l'entitat DimensioEntity.
 *
 * La classe utilitza el framework Spring per gestionar la injecció de dependències i s'anota com a servei (@Service),
 * a més d'utilitzar l'anotació @Slf4j per registrar informació de diagnòstic.
 *
 * Aquesta implementació pot ser utilitzada per altres components del sistema per proporcionar funcionalitats específiques relacionades
 * amb l'entitat Dimensio.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class DimensioServiceImpl  extends BaseReadonlyResourceService<Dimensio, Long, DimensioEntity> implements DimensioService {

}
