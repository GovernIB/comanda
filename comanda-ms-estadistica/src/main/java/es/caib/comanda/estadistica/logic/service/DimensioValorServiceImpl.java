package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.estadistica.logic.intf.service.DimensioValorService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat DimensioValor.
 *
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives als valors de dimensions,
 * i s'estén de BaseReadonlyResourceService per proporcionar operacions bàsiques en mode només lectura.
 *
 * Les accions específiques d’aquesta implementació estan alineades amb la interfície `DimensioValorService`
 * i gestionen l'accés a les dades mitjançant l'entitat DimensioValorEntity.
 *
 * La classe utilitza el framework Spring per a la gestió de dependències (@Service), i l’anotació @Slf4j per
 * registrar informació de diagnòstic i seguiment.
 *
 * Aquesta implementació pot ser utilitzada per altres components del sistema per oferir serveis relacionats amb
 * els valors associats a dimensions dins del model d'aplicació.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
public class DimensioValorServiceImpl extends BaseReadonlyResourceService<DimensioValor, Long, DimensioValorEntity> implements DimensioValorService {

}
