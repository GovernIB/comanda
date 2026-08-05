package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UnitatOrganitzativa;
import es.caib.comanda.estadistica.logic.intf.service.UnitatOrganitzativaService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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

    private final AclServiceClient aclServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @PostConstruct
    public void init() {
        register(UnitatOrganitzativa.PERSP_PERMIS_NUM, new PermisPerspective());
    }

    @Override
    protected void afterConversion(UnitatOrganitzativaEntity entity, UnitatOrganitzativa resource) {
        resource.setDenominacio(entity.getDenominacio());
        resource.setCodiNom(entity.getCodiNom());
    }

    public class PermisPerspective implements PerspectiveApplicator<UnitatOrganitzativaEntity, UnitatOrganitzativa> {
        @Override
        public void applySingle(String code, UnitatOrganitzativaEntity entity, UnitatOrganitzativa resource) throws PerspectiveApplicationException {
        }

        @Override
        public boolean applyMultiple(String code, List<UnitatOrganitzativaEntity> entities, List<UnitatOrganitzativa> resources) throws PerspectiveApplicationException {
            if (entities == null || entities.isEmpty() || entities.size() != resources.size()) {
                return false;
            }

            List<String> ids = entities.stream()
                .map(b -> String.valueOf(b.getId()))
                .collect(Collectors.toList());

            Map<Serializable, Integer> counts = Optional.ofNullable(aclServiceClient.countAllSidsWithPermission(
                ResourceType.UNITAT, String.join(",", ids),
                httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody()).orElse(new HashMap<>());

            for (int i = 0; i < resources.size(); i++) {
                Serializable id = ids.get(i);
                Integer count = counts.getOrDefault(id.toString(), 0);
                resources.get(i).setNumPermisos(count);
            }
            return !counts.isEmpty();
        }
    }
}
