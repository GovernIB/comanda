package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesPlugin;
import es.caib.comanda.estadistica.logic.helper.UnitatOrganitzativaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Entitat;
import es.caib.comanda.estadistica.logic.intf.service.EntitatService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat Entitat.
 *
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives a Entitatns,
 * i s'estén de BaseMutableResourceService per oferir operacions bàsiques de lògica empresarial.
 *
 * Les accions específiques que es realitzen en aquesta classe estan directament relacionades amb la interfície EntitatService
 * i amb l'accés a les dades mitjançant l'entitat EntitatEntity.
 *
 * La classe utilitza el framework Spring per gestionar la injecció de dependències i s'anota com a servei (@Service),
 * a més d'utilitzar l'anotació @Slf4j per registrar informació de diagnòstic.
 *
 * Aquesta implementació pot ser utilitzada per altres components del sistema per proporcionar funcionalitats específiques relacionades
 * amb l'entitat Entitat.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EntitatServiceImpl extends BaseMutableResourceService<Entitat, Long, EntitatEntity> implements EntitatService {

    private final UnitatsOrganitzativesPlugin unitatsOrganitzativesPlugin;
    private final UnitatOrganitzativaHelper unitatOrganitzativaHelper;
    private final AclServiceClient aclServiceClient;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @PostConstruct
    public void init() {
        register(Entitat.ACTION_REFRESH_UO, new RefreshUOActionExecutor());
        register(Entitat.PERSP_PERMIS_NUM, new PermisPerspective());
    }

    public class PermisPerspective implements PerspectiveApplicator<EntitatEntity, Entitat> {
        @Override
        public void applySingle(String code, EntitatEntity entity, Entitat resource) throws PerspectiveApplicationException {
            resource.setNumPermisos(
                Optional.ofNullable(aclServiceClient
                        .countSidsWithPermission(ResourceType.ENTITAT, entity.getId(),
                            httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody())
                    .orElse(0));
        }
    }

    public class RefreshUOActionExecutor implements ActionExecutor<EntitatEntity, Serializable, Entitat> {
        @Override
        public Entitat exec(String code, EntitatEntity entity, Serializable params) throws ActionExecutionException {
            try {
                List<UnitatOrganitzativaEntity> uoList = unitatsOrganitzativesPlugin.findAll(entity.getCodiDir3());
                unitatOrganitzativaHelper.updateAll(uoList);
                return resourceEntityMappingHelper.entityToResource(entity, Entitat.class);
            } catch (Exception e) {
                throw new ActionExecutionException(
                    Entitat.class,
                    null,
                    code,
                    e.getMessage());
            }
        }

        @Override
        public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Serializable target) {

        }
    }
}
