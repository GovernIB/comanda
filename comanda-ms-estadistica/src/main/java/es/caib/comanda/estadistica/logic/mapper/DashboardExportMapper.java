package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardItemExport;
import es.caib.comanda.estadistica.logic.intf.model.export.DimensioValorExport;
import es.caib.comanda.estadistica.logic.intf.model.export.EstadisticaGraficWidgetExport;
import es.caib.comanda.estadistica.logic.intf.model.export.EstadisticaSimpleWidgetExport;
import es.caib.comanda.estadistica.logic.intf.model.export.EstadisticaTaulaWidgetExport;
import es.caib.comanda.estadistica.logic.intf.model.export.EstadisticaWidgetExport;
import es.caib.comanda.estadistica.logic.intf.model.export.IndicadorTaulaExport;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassMapping;

import java.util.List;

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
    @Mapping(target = "entornCodi", ignore = true)
    @Mapping(target = "appCodi", ignore = true)
    @Mapping(target = "items", expression = "java(toDashboardItemExport(dashboardEntity.getItems(), estadisticaClientHelper, atributsVisualsHelper))")
    DashboardExport toDashboardExport(DashboardEntity dashboardEntity, EstadisticaClientHelper estadisticaClientHelper, AtributsVisualsHelper atributsVisualsHelper);
    
    /**
     * Converteix una llista de DashboardEntity a una llista de DashboardExport.
     * 
     * @param dashboardEntities La llista de dashboard entities a convertir
     * @return La llista de DashboardExport resultant
     */
    default List<DashboardExport> toDashboardExport(List<DashboardEntity> dashboardEntities, EstadisticaClientHelper estadisticaClientHelper, AtributsVisualsHelper atributsVisualsHelper) {
        if (dashboardEntities == null) {
            return null;
        }
        
        List<DashboardExport> result = new java.util.ArrayList<>(dashboardEntities.size());
        for (DashboardEntity dashboardEntity : dashboardEntities) {
            result.add(toDashboardExport(dashboardEntity, estadisticaClientHelper, atributsVisualsHelper));
        }
        return result;
    }
    
    /**
     * Converteix un DashboardItemEntity a un DashboardItemExport.
     * Els camps amb el mateix nom es mapegen automàticament.
     * 
     * @param dashboardItemEntity El DashboardItemEntity a convertir
     * @return El DashboardItemExport resultant
     */
    @Mapping(target = "entornCodi", expression = "java(estadisticaClientHelper.entornById(dashboardItemEntity.getEntornId()).getCodi())")
    @Mapping(target = "appCodi", expression = "java(estadisticaClientHelper.appFindById(dashboardItemEntity.getWidget().getAppId()).getCodi())")
    @Mapping(target = "widget", expression = "java(toWidgetExport(dashboardItemEntity.getWidget(), atributsVisualsHelper))")
    @Mapping(target = "atributsVisuals", expression = "java(atributsVisualsHelper.getAtributsVisuals(dashboardItemEntity))")
    DashboardItemExport toDashboardItemExport(DashboardItemEntity dashboardItemEntity, EstadisticaClientHelper estadisticaClientHelper, AtributsVisualsHelper atributsVisualsHelper);

    default List<DashboardItemExport> toDashboardItemExport(List<DashboardItemEntity> dashboardItemsEntity, EstadisticaClientHelper estadisticaClientHelper, AtributsVisualsHelper atributsVisualsHelper) {
        if (dashboardItemsEntity == null) {
            return null;
        }
        
        List<DashboardItemExport> result = new java.util.ArrayList<>(dashboardItemsEntity.size());
        for (DashboardItemEntity dashboardItemEntity : dashboardItemsEntity) {
            result.add(toDashboardItemExport(dashboardItemEntity, estadisticaClientHelper, atributsVisualsHelper));
        }
        return result;
    }

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
    default EstadisticaWidgetExport toWidgetExport(EstadisticaWidgetEntity widget, AtributsVisualsHelper atributsVisualsHelper) {
        if (widget == null) {
            return null;
        }
        
        EstadisticaWidgetExport result = toWidgetExport(widget);
        
        if (result instanceof EstadisticaSimpleWidgetExport) {
            ((EstadisticaSimpleWidgetExport) result).setAtributsVisuals(
                (es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple) atributsVisualsHelper.getAtributsVisuals(widget)
            );
        } else if (result instanceof EstadisticaGraficWidgetExport) {
            // TODO: afegir mapeig de indicadorInfo, descomposicioDimensioCodi, unitatAgregacio?
            ((EstadisticaGraficWidgetExport) result).setAtributsVisuals(
                (es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic) atributsVisualsHelper.getAtributsVisuals(widget)
            );
        } else if (result instanceof EstadisticaTaulaWidgetExport) {
            // TODO: afegir mapeig de dimensioAgrupacioCodi?
            ((EstadisticaTaulaWidgetExport) result).setAtributsVisuals(
                (es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula) atributsVisualsHelper.getAtributsVisuals(widget)
            );
        }
        
        return result;
    }

    @Mapping(target = "dimensioCodi", source = "dimensio.codi")
    DimensioValorExport toDimensioValorExport(DimensioValorEntity dimensioValorEntity);

    @Mapping(target = "indicadorCodi", source = "indicador.codi")
    IndicadorTaulaExport toIndicadorTaulaExport(IndicadorTaulaEntity indicadorTaulaEntity);
    
}