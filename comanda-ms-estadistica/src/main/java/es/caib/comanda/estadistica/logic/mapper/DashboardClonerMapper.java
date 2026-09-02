package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardFiltreEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DashboardClonerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "v", ignore = true)
    @Mapping(target = "dashboard", ignore = true)
    DashboardTitolEntity cloneTitol(DashboardTitolEntity original);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "v", ignore = true)
    @Mapping(target = "dashboard", ignore = true)
    @Mapping(target = "widget", ignore = true)
    @Mapping(target = "entornId", ignore = true)
    DashboardItemEntity cloneItem(DashboardItemEntity original);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "v", ignore = true)
    @Mapping(target = "dashboard", ignore = true)
    DashboardFiltreEntity cloneFiltre(DashboardFiltreEntity original);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "v", ignore = true)
    @Mapping(target = "titol", ignore = true)
    @Mapping(target = "appId", ignore = true)
    @Mapping(target = "indicadorInfo", ignore = true)
    EstadisticaSimpleWidgetEntity cloneSimpleWidget(EstadisticaSimpleWidgetEntity original);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "v", ignore = true)
    @Mapping(target = "titol", ignore = true)
    @Mapping(target = "appId", ignore = true)
    @Mapping(target = "indicadorsInfo", ignore = true)
    EstadisticaGraficWidgetEntity cloneGraficWidget(EstadisticaGraficWidgetEntity original);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "v", ignore = true)
    @Mapping(target = "titol", ignore = true)
    @Mapping(target = "appId", ignore = true)
    @Mapping(target = "columnes", ignore = true)
    EstadisticaTaulaWidgetEntity cloneTaulaWidget(EstadisticaTaulaWidgetEntity original);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "v", ignore = true)
    @Mapping(target = "widget", ignore = true)
    @Mapping(target = "indicadorId", ignore = true)
    IndicadorTaulaEntity cloneIndicadorTaula(IndicadorTaulaEntity original);

}
