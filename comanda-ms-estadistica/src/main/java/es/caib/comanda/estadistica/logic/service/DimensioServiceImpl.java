package es.caib.comanda.estadistica.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.estadistica.logic.dir3.UnitatsOrganitzativesRestClient;
import es.caib.comanda.estadistica.logic.helper.EntitatResolverHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.logic.intf.service.DimensioService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat Dimensio.
 * <p>
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives a Dimensions,
 * i s'estén de BaseReadonlyResourceService per oferir operacions bàsiques de lògica empresarial en mode només lectura.
 * <p>
 * Les accions específiques que es realitzen en aquesta classe estan directament relacionades amb la interfície DimensioService
 * i amb l'accés a les dades mitjançant l'entitat DimensioEntity.
 * <p>
 * La classe utilitza el framework Spring per gestionar la injecció de dependències i s'anota com a servei (@Service),
 * a més d'utilitzar l'anotació @Slf4j per registrar informació de diagnòstic.
 * <p>
 * Aquesta implementació pot ser utilitzada per altres components del sistema per proporcionar funcionalitats específiques relacionades
 * amb l'entitat Dimensio.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DimensioServiceImpl extends BaseMutableResourceService<Dimensio, Long, DimensioEntity> implements DimensioService {
    private final SpringFilterHelper springFilterHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final FetRepository fetRepository;
    private final DimensioRepository dimensioRepository;
    private final DimensioValorRepository dimensioValorRepository;
    private final EntitatResolverHelper entitatResolverHelper;

    @Value("${es.caib.comanda.estadistica.dir3.govern.codi.arrel:" + UnitatsOrganitzativesRestClient.CODI_ARREL_PER_DEFECTE + "}")
    private String codiArrel;

    @PostConstruct
    public void init() {
        register(Dimensio.ACTION_CHANGE_TIPUS, new ChangeTipusActionExecutor());
        register(Dimensio.ACTION_FET_CONS, new FetConsActionExecutor());
        register(Dimensio.ACTION_UPDATE_ENTITATS, new UpdateEntitatsActionExecutor());
    }

    @Override
    protected Specification<DimensioEntity> namedFilterToSpecification(String name) {
        if (name != null && name.startsWith(Dimensio.NAMED_FILTER_BY_APP_GROUP_BY_NOM)) {
            List<Long> idsEntornApp = null;
            String[] parts = name.split(":", 2);
            if (parts.length == 2 && !parts[1].isBlank()) {
                idsEntornApp = estadisticaClientHelper.getEntornAppsIdByAppId(Long.valueOf(parts[1]));
            }
            return uniqueNomByMinEntornAppId(idsEntornApp);
        }
        return null;
    }

    /**
     * Filtro para solo mostrar un resultado por nombre en la aplicación. Aprovechando el UK de entornAppId y nom.
     * Se requiere aplicar un filtro de entornApps, ya que si no se devolvería un resultado erróneo.
     **/
    private static Specification<DimensioEntity> uniqueNomByMinEntornAppId(List<Long> idsEntornApp) {
        if (idsEntornApp == null || idsEntornApp.isEmpty()) { //Si no hay resultados en la lista no devolveremos dimensions
            return (root, query, cb) -> cb.disjunction();
        }
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<DimensioEntity> subRoot = subquery.from(DimensioEntity.class);
            subquery.select(cb.min(subRoot.get("entornAppId")));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(subRoot.get("nom"), root.get("nom")));
            predicates.add(cb.equal(subRoot.get("codi"), root.get("codi")));
            predicates.add(subRoot.get("entornAppId").in(idsEntornApp));
            subquery.where(predicates.toArray(new Predicate[0]));
            return cb.equal(root.get("entornAppId"), subquery);
        };
    }

    @Override
    protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries) {
        List<Filter> filters = new ArrayList<>();
        if (currentSpringFilter != null && !currentSpringFilter.isEmpty()) {
            filters.add(Filter.parse(currentSpringFilter));
        }
        if (namedQueries != null) {
            for (String namedQuery : namedQueries) {
                if (namedQuery.contains(Dimensio.FILTER_BY_APP_NAMEDFILTER)) {
                    long appId = Long.parseLong(namedQuery.split(":")[1]);
                    filters.add(springFilterHelper.filterByApp(appId, Dimensio.Fields.entornAppId));
                }
            }
        }
        List<Filter> result = filters.stream().
            filter(f -> f != null && !String.valueOf(f).isEmpty()).
            collect(Collectors.toList());
        return result.isEmpty() ? null : FilterBuilder.and(result).generate();
    }

    public class ChangeTipusActionExecutor implements ActionExecutor<DimensioEntity, Dimensio.ChangeTipusActionForm, Dimensio> {
        @Override
        public Dimensio exec(String code,
                             DimensioEntity entity,
                             Dimensio.ChangeTipusActionForm params) throws ActionExecutionException {
            try {
                entity.setTipus(params.getTipus());
                entity.setEntitatValorTipus(
                    TipusDimensioEnum.ENTITAT.equals(params.getTipus()) ? params.getEntitatValorTipus() : null);
                if (TipusDimensioEnum.ENTITAT.equals(params.getTipus())) {
                    // Tant si es configura ENTITAT per primer cop com si només se n'edita el camp de mapeig,
                    // enllaçam/creem les Entitat corresponents als valors ja existents de la dimensió.
                    actualitzaEntitats(entity);
                }
                return resourceEntityMappingHelper.entityToResource(entity, Dimensio.class);
            } catch (ActionExecutionException a) {
                throw a;
            } catch (Exception e) {
                throw new ActionExecutionException(
                    Dimensio.class,
                    null,
                    code,
                    e.getMessage());
            }
        }

        @Override
        public void onChange(Serializable id,
                             Dimensio.ChangeTipusActionForm previous,
                             String fieldName,
                             Object fieldValue,
                             Map<String, AnswerRequiredException.AnswerValue> answers,
                             String[] previousFieldNames,
                             Dimensio.ChangeTipusActionForm target) {

        }
    }

    public class FetConsActionExecutor implements ActionExecutor<DimensioEntity, Serializable, Dimensio> {

        @Override
        public Dimensio exec(String code, DimensioEntity entity, Serializable params) throws ActionExecutionException {
            try {
                if (TipusDimensioEnum.ORGAN_GESTOR.equals(entity.getTipus())) {
                    // Crear dimensió conselleria (si no existeix)
                    List<DimensioEntity> dimensioEntityList = dimensioRepository.findByEntornAppId(entity.getEntornAppId());
                    if (dimensioEntityList.stream().noneMatch(c -> c.getTipus() == TipusDimensioEnum.CONSELLERIA)) {
                        DimensioEntity dEntity = new DimensioEntity();
                        dEntity.setCodi("CONS");
                        dEntity.setNom("Conselleria");
                        dEntity.setEntornAppId(entity.getEntornAppId());
                        dEntity.setTipus(TipusDimensioEnum.CONSELLERIA);
                        dimensioRepository.save(dEntity);
                    }

                    // Cambiar només els que no tenen "CONS"
//                    List<FetEntity> fetEntityList = fetRepository.findByEntornAppIdAddCons(entity.getEntornAppId(), entity.getCodi(), "CONS", codiArrel);
//                    fetEntityList = fetEntityList.stream()
//                        .peek(f -> {
//                            String c = unitatsOrganitzativesPlugin.getConsergeria(f.getDimensionsJson().get(entity.getCodi()));
//                            if (c != null) f.getDimensionsJson().put("CONS", c);
//                        })
//                        .filter(f -> f.getDimensionsJson().containsKey("CONS"))
//                        .collect(Collectors.toList());
//                    if (!fetEntityList.isEmpty())
//                        fetRepository.saveAll(fetEntityList);

                    // Actualitzar tots els valors "CONS", tenint en compte l'entitat de cada fet (si en té)
                    List<FetEntity> fetEntityList = fetRepository.findByEntornAppIdAddCons(entity.getEntornAppId(), entity.getCodi(), codiArrel);
                    fetEntityList = fetEntityList.stream()
                        .peek(f -> {
                            String organValor = f.getDimensionsJson().get(entity.getCodi());
                            String c = entitatResolverHelper.resolveConselleria(entity.getEntornAppId(), organValor, f.getDimensionsJson());
                            if (c != null) {
                                f.getDimensionsJson().put("CONS", c);
                            } else {
                                f.getDimensionsJson().remove("CONS");
                            }
                        })
                        .collect(Collectors.toList());
                    if (!fetEntityList.isEmpty())
                        fetRepository.saveAll(fetEntityList);
                }
                return resourceEntityMappingHelper.entityToResource(entity, Dimensio.class);
            } catch (ActionExecutionException a) {
                throw a;
            } catch (Exception e) {
                throw new ActionExecutionException(
                    Dimensio.class,
                    null,
                    code,
                    e.getMessage());
            }
        }

        @Override
        public void onChange(Serializable id,
                             Serializable previous,
                             String fieldName,
                             Object fieldValue,
                             Map<String, AnswerRequiredException.AnswerValue> answers,
                             String[] previousFieldNames,
                             Serializable target) {

        }
    }

    /**
     * Recorre tots els valors d'una dimensió de tipus ENTITAT i crea/enllaça les Entitat corresponents (backfill de
     * valors històrics que van arribar abans que existís l'auto-creació a la ingesta, o que encara no s'havien
     * pogut enllaçar perquè la dimensió no tenia tipus ENTITAT). Reutilitza
     * {@link EntitatResolverHelper#resolveOrCreateEntitat}, que ja no fa res si l'entitat ja existeix. Es crida tant
     * des de {@link UpdateEntitatsActionExecutor} (backfill manual) com des de {@link ChangeTipusActionExecutor}
     * (en configurar/editar el tipus ENTITAT d'una dimensió).
     */
    private void actualitzaEntitats(DimensioEntity entity) {
        for (DimensioValorEntity valor : dimensioValorRepository.findByDimensio(entity)) {
            if (valor.getValor() == null || valor.getValor().isBlank()) continue;
            try {
                entitatResolverHelper.resolveOrCreateEntitat(entity, valor.getValor());
            } catch (Exception e) {
                log.warn("No s'ha pogut crear/resoldre automàticament l'Entitat pel valor '{}' de la dimensió {}: {}",
                    valor.getValor(), entity.getCodi(), e.getMessage());
            }
        }
    }

    public class UpdateEntitatsActionExecutor implements ActionExecutor<DimensioEntity, Serializable, Dimensio> {

        @Override
        public Dimensio exec(String code, DimensioEntity entity, Serializable params) throws ActionExecutionException {
            if (!TipusDimensioEnum.ENTITAT.equals(entity.getTipus())) {
                throw new ActionExecutionException(
                    Dimensio.class,
                    null,
                    code,
                    es.caib.comanda.ms.logic.intf.util.I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.logic.service.DimensioServiceImpl.UpdateEntitatsActionExecutor.nomesEntitat"));
            }
            try {
                actualitzaEntitats(entity);
                return resourceEntityMappingHelper.entityToResource(entity, Dimensio.class);
            } catch (ActionExecutionException a) {
                throw a;
            } catch (Exception e) {
                throw new ActionExecutionException(
                    Dimensio.class,
                    null,
                    code,
                    e.getMessage());
            }
        }

        @Override
        public void onChange(Serializable id,
                             Serializable previous,
                             String fieldName,
                             Object fieldValue,
                             Map<String, AnswerRequiredException.AnswerValue> answers,
                             String[] previousFieldNames,
                             Serializable target) {

        }
    }
}
