package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.model.export.*;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.*;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import org.mapstruct.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Mapper per convertir un DashboardEntity a un DashboardExport.
 * També inclou la conversió de DashboardItemEntity a DashboardItemExport.
 *
 * @author Límit Tecnologies
 */
@Mapper(componentModel = "spring", uses = {EstadisticaClientHelper.class, AtributsVisualsHelper.class})
public interface DashboardExportMapper {

    /**
     * Converteix un DashboardEntity a un DashboardExport.
     * Els camps amb el mateix nom es mapegen automàticament.
     * La llista d'items es mapeja utilitzant el mètode toDashboardItemExport.
     * 
     * @param dashboardEntity El dashboard entity a convertir
     * @return El DashboardExport resultant
     */
    @Mapping(target = "entornCodi", source = "entornId", qualifiedByName = "toEntornExport")
    @Mapping(target = "appCodi", source = "appId", qualifiedByName = "toAppExport")
    DashboardExport toDashboardExport(DashboardEntity dashboardEntity,
                                      @Context EstadisticaClientHelper estadisticaClientHelper,
                                      @Context AtributsVisualsHelper atributsVisualsHelper);

    /**
     * Converteix una llista de DashboardEntity a una llista de DashboardExport.
     *
     * @param dashboardEntities La llista de dashboard entities a convertir
     * @return La llista de DashboardExport resultant
     */
    List<DashboardExport> toDashboardExport(List<DashboardEntity> dashboardEntities,
                                            @Context EstadisticaClientHelper estadisticaClientHelper,
                                            @Context AtributsVisualsHelper atributsVisualsHelper);

    @Named("toAppExport")
    default String toAppExport(Long appId, @Context EstadisticaClientHelper estadisticaClientHelper) {
        if (appId == null) return null;
        App app = estadisticaClientHelper.appFindById(appId);
        return app != null ?app.getCodi() :null;
    }
    @Named("toEntornExport")
    default String toEntornExport(Long entornId, @Context EstadisticaClientHelper estadisticaClientHelper) {
        if (entornId == null) return null;
        Entorn entorn = estadisticaClientHelper.entornById(entornId);
        return entorn != null ?entorn.getCodi() :null;
    }

    @Mapping(target = "plantilla", source = "plantilla")
    DashboardTitolEntity toDashboardTitolEntity(DashboardTitolExport export);

    PlantillaExport toPlantillaExport(PlantillaEntity plantillaEntity);
    @Mapping(target = "ordre", source = "ordre")
    PlantillaGrupPaletesExport toPlantillaGrupPaletesExport(PlantillaGrupPaletesEntity plantillaEntity);
    @Mapping(target = "colors", source = "colors", qualifiedByName = "toColorsExport")
    PaletaExport toPaletaExport(PaletaEntity plantillaEntity);
    WidgetStylePropertyExport toWidgetStylePropertyExport(WidgetStylePropertyEntity widgetStylePropertyEntity);


    @Named("toColorsExport")
    default String toColorsExport(List<PaletaColorEntity> colors) {
        if (colors == null || colors.isEmpty()) return null;
//        colors.sort(e -> e.getPosicio());
        return colors.stream()
                .map(PaletaColorEntity::getValor)
                .collect(Collectors.joining(","));
    }
    
    /**
     * Converteix un DashboardItemEntity a un DashboardItemExport.
     * Els camps amb el mateix nom es mapegen automàticament.
     * 
     * @param dashboardItemEntity El DashboardItemEntity a convertir
     * @return El DashboardItemExport resultant
     */
    @Mapping(target = "entornCodi", source = "entornId", qualifiedByName = "toEntornExport")
    @Mapping(target = "appCodi", source = "widget.appId", qualifiedByName = "toAppExport")
    @Mapping(target = "widget", source = "widget", qualifiedByName = "toWidgetExport")
    @Mapping(target = "atributsVisuals", expression = "java(atributsVisualsHelper.getAtributsVisuals(dashboardItemEntity))")
    @Mapping(target = "plantilla", source = "plantilla")
    DashboardItemExport toDashboardItemExport(DashboardItemEntity dashboardItemEntity,
                                              @Context EstadisticaClientHelper estadisticaClientHelper,
                                              @Context AtributsVisualsHelper atributsVisualsHelper);

    @SubclassMapping(source = EstadisticaSimpleWidgetEntity.class, target = EstadisticaSimpleWidgetExport.class)
    @SubclassMapping(source = EstadisticaGraficWidgetEntity.class, target = EstadisticaGraficWidgetExport.class)
    @SubclassMapping(source = EstadisticaTaulaWidgetEntity.class, target = EstadisticaTaulaWidgetExport.class)
    EstadisticaWidgetExport toWidgetExport(EstadisticaWidgetEntity widget);
    
    /**
     * Converteix un EstadisticaWidgetEntity a un EstadisticaWidgetExport i emplena els atributsVisuals.
     * 
     * @param widget El widget entity a convertir
     * @param atributsVisualsHelper Helper per obtenir els atributs visuals
     * @return El EstadisticaWidgetExport resultant amb els atributs visuals emplenats
     */
    @Named("toWidgetExport")
    default EstadisticaWidgetExport toWidgetExport(EstadisticaWidgetEntity widget, @Context AtributsVisualsHelper atributsVisualsHelper) {
        if (widget == null) {
            return null;
        }
        
        EstadisticaWidgetExport result = toWidgetExport(widget);
        
        if (result instanceof EstadisticaSimpleWidgetExport) {
            ((EstadisticaSimpleWidgetExport) result).setAtributsVisuals(
                (es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple) atributsVisualsHelper.getAtributsVisuals(widget)
            );
            result.setTipus(WidgetTipus.SIMPLE);
        } else if (result instanceof EstadisticaGraficWidgetExport) {
            // TODO: afegir mapeig de unitatAgregacio?
            ((EstadisticaGraficWidgetExport) result).setAtributsVisuals(
                (es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic) atributsVisualsHelper.getAtributsVisuals(widget)
            );
            EstadisticaGraficWidgetEntity grafic = (EstadisticaGraficWidgetEntity) widget;
            if (grafic.getDescomposicioDimensio() != null) {
                ((EstadisticaGraficWidgetExport) result).setDescomposicioDimensioCodi(grafic.getDescomposicioDimensio().getCodi());
            }
            result.setTipus(WidgetTipus.GRAFIC);
        } else if (result instanceof EstadisticaTaulaWidgetExport) {
            ((EstadisticaTaulaWidgetExport) result).setAtributsVisuals(
                (es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula) atributsVisualsHelper.getAtributsVisuals(widget)
            );
            ((EstadisticaTaulaWidgetExport) result).setDimensioAgrupacioCodi(
                    ((EstadisticaTaulaWidgetEntity) widget).getDimensioAgrupacio().getCodi());
            result.setTipus(WidgetTipus.TAULA);
        }
        
        return result;
    }

    @Mapping(target = "dimensioCodi", source = "dimensio.codi")
    DimensioValorExport toDimensioValorExport(DimensioValorEntity dimensioValorEntity);

    @Mapping(target = "indicadorCodi", source = "indicador.codi")
    IndicadorTaulaExport toIndicadorTaulaExport(IndicadorTaulaEntity indicadorTaulaEntity);


    // ============================================================================
    // CONVERSIÓ INVERSA: Export → Entity
    // ============================================================================

    /**
     * Converteix un DashboardExport a un DashboardEntity.
     * Nota: Requereix repositories/helpers per fer lookup de codi → ID
     */
    @Mapping(target = "entornId", source = "entornCodi", qualifiedByName = "toEntornId")
    @Mapping(target = "appId", source = "appCodi", qualifiedByName = "toAppId")
    DashboardEntity toDashboardEntity(DashboardExport dashboardExport,
                                      @Context EstadisticaClientHelper estadisticaClientHelper,
                                      @Context AtributsVisualsHelper atributsVisualsHelper,
                                      @Context IndicadorRepository indicadorRepository,
                                      @Context DimensioRepository dimensioRepository,
                                      @Context DimensioValorRepository dimensioValorRepository);

    List<DashboardEntity> toDashboardEntity(List<DashboardExport> dashboardEntities,
                                            @Context EstadisticaClientHelper estadisticaClientHelper,
                                            @Context AtributsVisualsHelper atributsVisualsHelper,
                                            @Context IndicadorRepository indicadorRepository,
                                            @Context DimensioRepository dimensioRepository,
                                            @Context DimensioValorRepository dimensioValorRepository);

    @Named("toAppId")
    default Long toAppId(String appCodi, @Context EstadisticaClientHelper helper) {
        if (appCodi == null) return null;
        App app = helper.appFindByCodi(appCodi);
        return app != null ? app.getId() : null;
    }

    @Named("toEntornId")
    default Long toEntornId(String entornCodi, @Context EstadisticaClientHelper helper) {
        if (entornCodi == null) return null;
        Entorn entorn = helper.entornByCodi(entornCodi);
        return entorn != null ? entorn.getId() : null;
    }

    @Mapping(target = "entornId", source = "entornCodi", qualifiedByName = "toEntornId")
    @Mapping(target = "widget", source = ".", qualifiedByName = "toWidgetEntity")
    @Mapping(target = "plantilla", source = "plantilla")
    DashboardItemEntity toDashboardItemEntity(DashboardItemExport dashboardItemExport,
                                              @Context EstadisticaClientHelper estadisticaClientHelper,
                                              @Context AtributsVisualsHelper atributsVisualsHelper,
                                              @Context IndicadorRepository indicadorRepository,
                                              @Context DimensioRepository dimensioRepository,
                                              @Context DimensioValorRepository dimensioValorRepository);

    EstadisticaSimpleWidgetEntity toSimpleWidgetEntity(EstadisticaSimpleWidgetExport dto,
                                                       @Context Long entornAppId,
                                                       @Context IndicadorRepository indicadorRepository,
                                                       @Context DimensioRepository dimensioRepository,
                                                       @Context DimensioValorRepository dimensioValorRepository);

    @Mapping(target = "descomposicioDimensio", source = "descomposicioDimensioCodi", qualifiedByName = "toDimensioEntity")
    EstadisticaGraficWidgetEntity toGraficWidgetEntity(EstadisticaGraficWidgetExport dto,
                                                       @Context Long entornAppId,
                                                       @Context IndicadorRepository indicadorRepository,
                                                       @Context DimensioRepository dimensioRepository,
                                                       @Context DimensioValorRepository dimensioValorRepository);

    @Mapping(target = "dimensioAgrupacio", source = "dimensioAgrupacioCodi", qualifiedByName = "toDimensioEntity")
    EstadisticaTaulaWidgetEntity toTaulaWidgetEntity(EstadisticaTaulaWidgetExport dto,
                                                     @Context Long entornAppId,
                                                     @Context IndicadorRepository indicadorRepository,
                                                     @Context DimensioRepository dimensioRepository,
                                                     @Context DimensioValorRepository dimensioValorRepository);

    @Named("toWidgetEntity")
    default EstadisticaWidgetEntity toWidgetEntity(DashboardItemExport dashboardItemExport,
                                                   @Context EstadisticaClientHelper estadisticaClientHelper,
                                                   @Context AtributsVisualsHelper atributsVisualsHelper,
                                                   @Context IndicadorRepository indicadorRepository,
                                                   @Context DimensioRepository dimensioRepository,
                                                   @Context DimensioValorRepository dimensioValorRepository) {
        if (dashboardItemExport == null) return null;
        EstadisticaWidgetExport widgetExport = dashboardItemExport.getWidget();
        if (widgetExport == null) return null;

        Long entornId = toEntornId(dashboardItemExport.getEntornCodi(), estadisticaClientHelper);
        Long appId = toAppId(dashboardItemExport.getAppCodi(), estadisticaClientHelper);
        EntornApp entornApp = (appId != null && entornId != null) ?estadisticaClientHelper.entornAppFindByAppAndEntorn(appId, entornId) :null;
        Long entornAppId = entornApp != null ?entornApp.getId() :null;

        EstadisticaWidgetEntity entity = null;
        if (widgetExport instanceof EstadisticaSimpleWidgetExport) {
            entity = toSimpleWidgetEntity((EstadisticaSimpleWidgetExport) widgetExport, entornAppId, indicadorRepository, dimensioRepository, dimensioValorRepository);
            entity.setAtributsVisualsJson(
                    atributsVisualsHelper.getAtributsVisualsJson(((EstadisticaSimpleWidgetExport) widgetExport).getAtributsVisuals())
            );
        } if (widgetExport instanceof EstadisticaGraficWidgetExport) {
            entity = toGraficWidgetEntity((EstadisticaGraficWidgetExport) widgetExport, entornAppId, indicadorRepository, dimensioRepository, dimensioValorRepository);
            entity.setAtributsVisualsJson(
                    atributsVisualsHelper.getAtributsVisualsJson(((EstadisticaGraficWidgetExport) widgetExport).getAtributsVisuals())
            );
        } if (widgetExport instanceof EstadisticaTaulaWidgetExport) {
            entity = toTaulaWidgetEntity((EstadisticaTaulaWidgetExport) widgetExport, entornAppId, indicadorRepository, dimensioRepository, dimensioValorRepository);
            entity.setAtributsVisualsJson(
                    atributsVisualsHelper.getAtributsVisualsJson(((EstadisticaTaulaWidgetExport) widgetExport).getAtributsVisuals())
            );
        }

        if (entity != null) {
            entity.setAppId(appId);
        }

        return entity;
    }

    @Mapping(target = "indicador", source = ".", qualifiedByName = "toIndicadorEntity")
    IndicadorTaulaEntity toIndicadorTaulaEntity(IndicadorTaulaExport indicadorTaulaExport,
                                                @Context Long entornAppId,
                                                @Context IndicadorRepository indicadorRepository);

    @Named("toIndicadorEntity")
    public default IndicadorEntity toIndicadorEntity(IndicadorTaulaExport indicadorTaulaExport,
                                              @Context Long entornAppId,
                                              @Context IndicadorRepository indicadorRepository) {
        if (indicadorTaulaExport == null || indicadorTaulaExport.getIndicadorCodi() == null || entornAppId == null) return null;
        return indicadorRepository.findByCodiAndEntornAppId(indicadorTaulaExport.getIndicadorCodi(), entornAppId).orElse(null);
    }

    @Named("toDimensioEntity")
    public default DimensioEntity toDimensioEntity(String dimensioCodi,
                                                   @Context Long entornAppId,
                                                   @Context DimensioRepository dimensioRepository) {
        if (dimensioCodi == null || entornAppId == null) return null;
        return dimensioRepository.findByCodiAndEntornAppId(dimensioCodi, entornAppId).orElse(null);
    }

    default DimensioValorEntity toDimensioValorEntity(DimensioValorExport dimensioValorExport,
                                                      @Context Long entornAppId,
                                                      @Context DimensioRepository dimensioRepository,
                                                      @Context DimensioValorRepository dimensioValorRepository) {
        if (dimensioValorExport == null || entornAppId == null) return null;
        DimensioEntity dimensio = this.toDimensioEntity(dimensioValorExport.getDimensioCodi(), entornAppId, dimensioRepository);
        if (dimensio == null) return null;
        return dimensioValorRepository.findByDimensioAndValor(dimensio, dimensioValorExport.getValor()).orElse(null);
    }

    @Mapping(target = "plantilla", source = "plantilla")
    DashboardTitolExport toDashboardTitolEntity(DashboardTitolEntity export);

    PlantillaEntity toPlantillaEntity(PlantillaExport plantillaExport);
    @Mapping(target = "ordre", source = "ordre")
    PlantillaGrupPaletesEntity toPlantillaGrupPaletesEntity(PlantillaGrupPaletesExport plantillaGrupPaletesExport);
    @Mapping(target = "colors", source = "colors", qualifiedByName = "toColorsEntity")
    PaletaEntity toPaletaEntity(PaletaExport paletaExport);
    WidgetStylePropertyEntity toWidgetStylePropertyEntity(WidgetStylePropertyExport widgetStylePropertyExport);

    @Named("toColorsEntity")
    public default List<PaletaColorEntity> toColorsEntity(String colors) {
        if (colors == null || colors.isEmpty()) return null;
        AtomicInteger index = new AtomicInteger(0);
        return Arrays.stream(colors.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map((v) -> {
                    PaletaColorEntity p = new PaletaColorEntity();
                    p.setPosicio(index.getAndIncrement());
                    p.setValor(v);
                    return p;
                })
                .collect(Collectors.toList());
    }
}