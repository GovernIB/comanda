package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaWidget;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lògica comuna per a obtenir i consultar informació per als widgets estadístics.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstadisticaWidgetHelper {

    private final ResourceEntityMappingHelper resourceEntityMappingHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final DimensioValorRepository dimensioValorRepository;
    private final DashboardItemRepository dashboardItemRepository;
    private final CacheManager cacheManager;

    /** Sincronitza els valors de dimensió d'un widget estadístic entre el recurs rebut i l'entitat persistida.
        Afegeix noves associacions i elimina les que ja no hi són presents, modificant la relació. */
    public <E extends EstadisticaWidgetEntity, R extends EstadisticaWidget> void upsertDimensionsValors(E entity, R resource) {
        List<DimensioValorEntity> dimensionsValorsEntities = entity.getDimensionsValor();
        List<ResourceReference<DimensioValor, Long>> dimensionsValors = resource.getDimensionsValor();
        if (dimensionsValors == null) {
            dimensionsValors = Collections.emptyList();
        }
        if (dimensionsValorsEntities == null) {
            dimensionsValorsEntities = new ArrayList<>();
            entity.setDimensionsValor(dimensionsValorsEntities);
        }
        List<DimensioValorEntity> persistValues = dimensionsValors.stream()
            .map(ResourceReference::getId)
            .filter(Objects::nonNull)
            .map(dimensioValorRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
        dimensionsValorsEntities.clear();
        dimensionsValorsEntities.addAll(persistValues);
    }

    /** Assigna el nom de l'aplicació a partir de l'appId **/
    public <E extends EstadisticaWidgetEntity, R extends EstadisticaWidget> void afterConversionGetAppNom(E entity, R resource) {
        try {
            App app = estadisticaClientHelper.appFindById(entity.getAppId());
            if (app != null) {
                resource.setAplicacio(ResourceReference.toResourceReference(app.getId(), app.getNom()));
            }
        } catch (Exception e) {
            log.error("Error obtenint el nom de l'aplicació amb id=" + entity.getAppId(), e);
        }
    }

    /** Assigna la llista de referències a {@link DimensioValor} dins del {@link EstadisticaWidget} **/
    public <E extends EstadisticaWidgetEntity, R extends EstadisticaWidget> void afterConversionGetDimensions(E entity, R resource) {
        List<DimensioValorEntity> dimensionsValorsEntities = entity.getDimensionsValor();
        List<ResourceReference<DimensioValor, Long>> dimensionsValors = new ArrayList<>();
        for (DimensioValorEntity dimensioValorEntity : dimensionsValorsEntities) {
            DimensioValor dimensioValor = resourceEntityMappingHelper.entityToResource(dimensioValorEntity, DimensioValor.class);
            ResourceReference<DimensioValor, Long> resourceReference = ResourceReference.toResourceReference(dimensioValorEntity.getId(), dimensioValor.getDesc());
            dimensionsValors.add(resourceReference);
        }
        resource.setDimensionsValor(dimensionsValors);
    }

    public void clearDashboardWidgetCache(Long widgetId) {
        Cache cache = cacheManager.getCache("dashboardWidgetCache");
        if (cache == null) {
            return;
        }
        try {
            cache.evict(widgetId + "_" + java.time.LocalDate.now()); // Esborra l'entrada concreta
        } catch (Exception e) {
            cache.clear();
        }
    }

}
