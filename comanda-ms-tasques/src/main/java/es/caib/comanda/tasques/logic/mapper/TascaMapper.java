package es.caib.comanda.tasques.logic.mapper;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.tasques.logic.helper.TasquesClientHelper;
import es.caib.comanda.tasques.logic.intf.model.Tasca;
import es.caib.comanda.tasques.persist.entity.TascaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Optional;

@Mapper(componentModel = "spring", uses = { TasquesClientHelper.class })
public interface TascaMapper {

    @Mapping(target = "dataInici", expression = "java(tasca.getDataInici() != null ? tasca.getDataInici().toLocalDateTime() : null)")
    @Mapping(target = "dataFi", expression = "java(tasca.getDataFi() != null ? tasca.getDataFi().toLocalDateTime() : null)")
    @Mapping(target = "dataCaducitat", expression = "java(tasca.getDataCaducitat() != null ? tasca.getDataCaducitat().toLocalDateTime() : null)")
    @Mapping(target = "url", source = "tasca.redireccio")
    @Mapping(target = "entornAppId", ignore = true)
    @Mapping(target = "entornId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    Tasca toTasca(TasquesClientHelper tasquesClientHelper, es.caib.comanda.model.v1.tasca.Tasca tasca);

    @Mapping(target = "dataInici", expression = "java(tasca.getDataInici() != null ? tasca.getDataInici().toLocalDateTime() : null)")
    @Mapping(target = "dataFi", expression = "java(tasca.getDataFi() != null ? tasca.getDataFi().toLocalDateTime() : null)")
    @Mapping(target = "dataCaducitat", expression = "java(tasca.getDataCaducitat() != null ? tasca.getDataCaducitat().toLocalDateTime() : null)")
    @Mapping(target = "url", source = "tasca.redireccio")
    @Mapping(target = "entornAppId", source = "entornApp.id")
    @Mapping(target = "entornId", source = "entornApp.entorn.id")
    @Mapping(target = "appId", source = "entornApp.app.id")
    Tasca toTasca(es.caib.comanda.model.v1.tasca.Tasca tasca, EntornApp entornApp);

    TascaEntity toTascaEntity(Tasca tasca);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "entornAppId", ignore = true)
    @Mapping(target = "entornId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    @Mapping(target = "identificador", ignore = true)
    void updateTasca(Tasca source, @MappingTarget TascaEntity target);


    @AfterMapping
    default void mapEntornApp(TasquesClientHelper tasquesClientHelper,
                              es.caib.comanda.model.v1.tasca.Tasca tasca,
                              @MappingTarget Tasca target) {
        EntornApp entornApp = toEntornApp(tasquesClientHelper, tasca.getEntornCodi(), tasca.getAppCodi());
        target.setEntornAppId(entornApp.getId());
        target.setEntornId(entornApp.getEntorn().getId());
        target.setAppId(entornApp.getApp().getId());
    }

    default EntornApp toEntornApp(TasquesClientHelper tasquesClientHelper, String entornCodi, String appCodi) {
        Optional<EntornApp> entornApp = tasquesClientHelper.entornAppFindByEntornCodiAndAppCodi(
                entornCodi,
                appCodi);

        if (entornApp.isEmpty()) {
            throw new ResourceNotFoundException(
                    EntornApp.class,
                    "(entornCodi=" + entornCodi + ", appCodi=" + appCodi + ")");
        }

        return entornApp.get();
    }
}
