package es.caib.comanda.estadistica.logic.service;

import com.turkraft.springfilter.FilterBuilder;
import com.turkraft.springfilter.parser.Filter;
import es.caib.comanda.estadistica.logic.dir3.SistemaExternException;
import es.caib.comanda.estadistica.logic.helper.DashboardSeguretatHelper;
import es.caib.comanda.estadistica.logic.helper.EntitatResolverHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.helper.UnitatOrganitzativaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UnitatOrganitzativa;
import es.caib.comanda.estadistica.logic.intf.service.DimensioValorService;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Classe d'implementació del servei per a la gestió de la lògica de negoci relacionada amb l'entitat DimensioValor.
 * <p>
 * Aquesta classe ofereix funcionalitats per a la manipulació i consulta de dades relatives als valors de dimensions,
 * i s'estén de BaseReadonlyResourceService per proporcionar operacions bàsiques en mode només lectura.
 * <p>
 * Les accions específiques d’aquesta implementació estan alineades amb la interfície `DimensioValorService`
 * i gestionen l'accés a les dades mitjançant l'entitat DimensioValorEntity.
 * <p>
 * La classe utilitza el framework Spring per a la gestió de dependències (@Service), i l’anotació @Slf4j per
 * registrar informació de diagnòstic i seguiment.
 * <p>
 * Aquesta implementació pot ser utilitzada per altres components del sistema per oferir serveis relacionats amb
 * els valors associats a dimensions dins del model d'aplicació.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DimensioValorServiceImpl extends BaseMutableResourceService<DimensioValor, Long, DimensioValorEntity> implements DimensioValorService {

    private static final int CODI_IN_QUERY_BATCH_SIZE = 900;

    private final SpringFilterHelper springFilterHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final UnitatOrganitzativaRepository unitatOrganitzativaRepository;
    private final UnitatOrganitzativaHelper unitatOrganitzativaHelper;
    private final EntitatResolverHelper entitatResolverHelper;
    private final DashboardSeguretatHelper dashboardSeguretatHelper;

    @PostConstruct
    public void init() {
        register(DimensioValor.ACTION_UO, new UOActionExecutor());
    }

    @Override
    protected Specification<DimensioValorEntity> namedFilterToSpecification(String name) {
        if (name != null && name.startsWith(DimensioValor.NAMED_FILTER_BY_APP_GROUP_BY_VALOR)) {
            List<Long> idsEntornApp = null;
            String[] parts = name.split(":", 2);
            if (parts.length == 2 && !parts[1].isBlank()) {
                idsEntornApp = estadisticaClientHelper.getEntornAppsIdByAppId(Long.valueOf(parts[1]));
            }
            return uniqueValorByMinEntornAppId(idsEntornApp);
        }
        if (name != null && name.startsWith(DimensioValor.NAMED_FILTER_BY_UO_NOM)) {
            String searchTerm = null;
            String[] parts = name.split(":", 2);
            if (parts.length == 2 && !parts[1].isBlank()) {
                searchTerm = parts[1];
            }
            return filterByUnitatOrganitzativaNom(searchTerm);
        }
        return null;
    }

    /**
     * Filtro para solo mostrar un resultado por valor en la aplicación.
     * Se requiere aplicar un filtro de entornApps, ya que si no se devolvería un resultado erróneo.
     **/
    private Specification<DimensioValorEntity> uniqueValorByMinEntornAppId(List<Long> idsEntornApp) {
        if (idsEntornApp == null || idsEntornApp.isEmpty()) {
            return (root, query, cb) -> cb.disjunction();
        }
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<DimensioValorEntity> subRoot = subquery.from(DimensioValorEntity.class);
            subquery.select(cb.min(subRoot.get("dimensio").get("entornAppId")));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(subRoot.get("valor"), root.get("valor")));
            predicates.add(cb.equal(subRoot.get("dimensio").get("nom"), root.get("dimensio").get("nom")));
            predicates.add(cb.equal(subRoot.get("dimensio").get("codi"), root.get("dimensio").get("codi")));
            predicates.add(subRoot.get("dimensio").get("entornAppId").in(idsEntornApp));
            subquery.where(predicates.toArray(new Predicate[0]));
            return cb.equal(root.get("dimensio").get("entornAppId"), subquery);
        };
    }

    /**
     * Filtro para DimensioValor por nombre de UnitatOrganitzativa.
     * Solo aplica cuando la dimensión es de tipo ORGAN_GESTOR (codi = "ORG"),
     * ya que en ese caso el campo 'valor' contiene el 'codi' de la unidad.
     */
    private static Specification<DimensioValorEntity> filterByUnitatOrganitzativaNom(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";

            Subquery<UnitatOrganitzativaEntity> subquery = query.subquery(UnitatOrganitzativaEntity.class);
            Root<UnitatOrganitzativaEntity> uoRoot = subquery.from(UnitatOrganitzativaEntity.class);
            subquery.select(uoRoot);

            Predicate nomCa = cb.like(cb.lower(uoRoot.get("denominacioCa")), pattern);
            Predicate nomEs = cb.like(cb.lower(uoRoot.get("denominacioEs")), pattern);
            Predicate valorMatch = cb.equal(root.get("valor"), uoRoot.get("codi"));

            subquery.where(cb.and(valorMatch, cb.or(nomCa, nomEs)));

            Predicate matchByUnitName = cb.exists(subquery);
            Predicate matchByDirectValor = cb.like(cb.lower(root.get("valor")), pattern);

            return cb.or(matchByUnitName, matchByDirectValor);
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
                if (namedQuery.contains(DimensioValor.FILTER_BY_APP_NAMEDFILTER)) {
                    long appId = Long.parseLong(namedQuery.split(":")[1]);
                    filters.add(springFilterHelper.filterByApp(appId, DimensioValor.Fields.dimensio + "." + Dimensio.Fields.entornAppId));
                }
            }
        }
        List<Filter> result = filters.stream().
            filter(f -> f != null && !String.valueOf(f).isEmpty()).
            collect(Collectors.toList());
        return result.isEmpty() ? null : FilterBuilder.and(result).generate();
    }

    /**
     * Restringeix les opcions de valors de dimensions de tipus ENTITAT a les entitats sobre les que l'usuari
     * actual té permís (vegeu DashboardSeguretatHelper) - imprescindible perquè el filtre d'entitat d'un dashboard
     * només mostri, com a opcions seleccionables, les entitats visibles per l'usuari. No afecta la resta de tipus
     * de dimensió. Contempla tant la coincidència directa per codi/codiDir3 com la sobreescriptura manual
     * (entitatMapejada), igual que EntitatResolverHelper.
     */
    @Override
    protected Specification<DimensioValorEntity> additionalSpecification(String[] namedQueries) {
        List<EntitatEntity> entitatsPermeses = dashboardSeguretatHelper.resoldreEntitatsPermeses();
        if (entitatsPermeses == null) {
            return null;
        }
        List<String> valorsPermesos = new ArrayList<>();
        List<Long> idsPermesos = new ArrayList<>();
        for (EntitatEntity entitat : entitatsPermeses) {
            if (entitat.getCodi() != null) {
                valorsPermesos.add(entitat.getCodi());
            }
            if (entitat.getCodiDir3() != null) {
                valorsPermesos.add(entitat.getCodiDir3());
            }
            idsPermesos.add(entitat.getId());
        }
        return (root, query, cb) -> {
            List<Predicate> orPredicates = new ArrayList<>();
            orPredicates.add(cb.notEqual(root.get("dimensio").get("tipus"), TipusDimensioEnum.ENTITAT));
            if (!valorsPermesos.isEmpty()) {
                orPredicates.add(root.get("valor").in(valorsPermesos));
            }
            if (!idsPermesos.isEmpty()) {
                orPredicates.add(root.get("entitatMapejada").get("id").in(idsPermesos));
            }
            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }

    @Override
    protected void afterConversion(List<DimensioValorEntity> entities, List<DimensioValor> resources) {
        Map<String, Set<String>> codisMap = entities.stream()
            .filter(e -> e.getDimensio() != null && e.getDimensio().getTipus() != null && e.getValor() != null)
            .collect(Collectors.groupingBy(
                e -> e.getDimensio().getTipus().name(),
                Collectors.mapping(
                    DimensioValorEntity::getValor,
                    Collectors.toSet()
                )
            ));

        Set<String> codis = new HashSet<>();
        Optional.ofNullable(codisMap.get(TipusDimensioEnum.ORGAN_GESTOR.name()))
            .ifPresent(codis::addAll);
        Optional.ofNullable(codisMap.get(TipusDimensioEnum.CONSELLERIA.name()))
            .ifPresent(codis::addAll);
        Map<String, UnitatOrganitzativaEntity> uoMap = findUnitatsOrganitzativesByCodiInBatches(codis);

        IntStream.range(0, entities.size()).forEach(i -> {
            var e = entities.get(i);
            var r = resources.get(i);

            if (TipusDimensioEnum.TIPUS_AMB_UNITAT_ORG.contains(e.getDimensio().getTipus())) {
                Optional.ofNullable(uoMap.get(e.getValor()))
                    .ifPresent(uo -> r.setUnitatOrganitzativa(
                        ResourceReference.toResourceReference(uo.getId(), uo.getDenominacio())
                    ));
            }

            if (TipusDimensioEnum.ENTITAT.equals(e.getDimensio().getTipus())) {
                // Fa servir el mateix resolutor que la conselleria a la ingesta (sobreescriptura manual, o
                // coincidència per codi/codiDir3 segons dimensio.entitatValorTipus), per mostrar sempre l'entitat
                // que realment s'utilitzarà en calcular la conselleria - no només una coincidència directa per codi.
                entitatResolverHelper.resolveEntitat(e.getDimensio(), e.getValor())
                    .ifPresent(entitat -> r.setEntitat(
                        ResourceReference.toResourceReference(entitat.getId(), entitat.getNom())
                    ));
            }
        });
    }

    public class UOActionExecutor implements ActionExecutor<DimensioValorEntity, Serializable, Serializable> {
        @Override
        public Serializable exec(String code,
                                 DimensioValorEntity entity,
                                 Serializable params) throws ActionExecutionException {
            if (entity.getDimensio().getTipus() == null) return null;
            try {
                switch (entity.getDimensio().getTipus()) {
                    case CONSELLERIA:
                    case ORGAN_GESTOR:
                        UnitatOrganitzativaEntity uo = unitatOrganitzativaHelper.updateByCodi(entity.getValor());
                        return resourceEntityMappingHelper.entityToResource(uo, UnitatOrganitzativa.class);
                    default:
                        throw new SistemaExternException("Tipus de dimensió no trabada");
                }
            } catch (Exception e) {
                throw new ActionExecutionException(
                    DimensioValor.class,
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

    // Evitar error si hi ha més de 1000 unitats organitzatives
    private Map<String, UnitatOrganitzativaEntity> findUnitatsOrganitzativesByCodiInBatches(Set<String> codis) {
        if (codis == null || codis.isEmpty()) {
            return Map.of();
        }

        List<String> codiList = new ArrayList<>(codis);
        Map<String, UnitatOrganitzativaEntity> result = new HashMap<>();

        for (int fromIndex = 0; fromIndex < codiList.size(); fromIndex += CODI_IN_QUERY_BATCH_SIZE) {
            int toIndex = Math.min(fromIndex + CODI_IN_QUERY_BATCH_SIZE, codiList.size());
            List<String> batch = codiList.subList(fromIndex, toIndex);

            unitatOrganitzativaRepository.findByCodiIn(batch).forEach(uo -> {
                if (uo.getCodi() != null) {
                    result.putIfAbsent(uo.getCodi(), uo);
                }
            });
        }

        return result;
    }

}
