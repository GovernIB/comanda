package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UnitatOrganitzativa;
import es.caib.comanda.estadistica.logic.intf.service.UnitatOrganitzativaService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

/**
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat UnitatOrganitzativa.
 *
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives a UnitatOrganitzativans,
 * i s'estén de BaseMutableResourceService per oferir operacions bàsiques de lògica empresarial.
 *
 * Les accions específiques que es realitzen en aquesta classe estan directament relacionades amb la interfície UnitatOrganitzativaService
 * i amb l'accés a les dades mitjançant l'entitat UnitatOrganitzativaEntity.
 *
 * La classe utilitza el framework Spring per gestionar la injecció de dependències i s'anota com a servei (@Service),
 * a més d'utilitzar l'anotació @Slf4j per registrar informació de diagnòstic.
 *
 * Aquesta implementació pot ser utilitzada per altres components del sistema per proporcionar funcionalitats específiques relacionades
 * amb l'entitat UnitatOrganitzativa.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UnitatOrganitzativaServiceImpl extends BaseMutableResourceService<UnitatOrganitzativa, Long, UnitatOrganitzativaEntity> implements UnitatOrganitzativaService {

    @Override
    protected void afterConversion(UnitatOrganitzativaEntity entity, UnitatOrganitzativa resource) {
        resource.setDenominacio(entity.getDenominacio());
        resource.setCodiNom(entity.getCodiNom());
    }
}
