package es.caib.comanda.avisos.logic.mapper;

import es.caib.comanda.avisos.logic.helper.AvisClientHelper;
import es.caib.comanda.avisos.logic.intf.model.Avis;
import es.caib.comanda.avisos.persist.entity.AvisEntity;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Optional;

@Mapper(componentModel = "spring", uses = { AvisClientHelper.class })
public interface AvisMapper {

    @Mapping(target = "dataInici", expression = "java(avis.getDataInici() != null ? avis.getDataInici().toLocalDateTime() : null)")
    @Mapping(target = "dataFi", expression = "java(avis.getDataFi() != null ? avis.getDataFi().toLocalDateTime() : null)")
    @Mapping(target = "url", source = "avis.redireccio")
    @Mapping(target = "entornAppId", ignore = true)
    @Mapping(target = "entornId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    Avis toAvis(AvisClientHelper avisClientHelper, es.caib.comanda.model.v1.avis.Avis avis);

    @Mapping(target = "dataInici", expression = "java(avis.getDataInici() != null ? avis.getDataInici().toLocalDateTime() : null)")
    @Mapping(target = "dataFi", expression = "java(avis.getDataFi() != null ? avis.getDataFi().toLocalDateTime() : null)")
    @Mapping(target = "url", source = "avis.redireccio")
    @Mapping(target = "entornAppId", source = "entornApp.id")
    @Mapping(target = "entornId", source = "entornApp.entorn.id")
    @Mapping(target = "appId", source = "entornApp.app.id")
    Avis toAvis(es.caib.comanda.model.v1.avis.Avis avis, EntornApp entornApp);

    AvisEntity toAvisEntity(Avis avis);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "entornAppId", ignore = true)
    @Mapping(target = "entornId", ignore = true)
    @Mapping(target = "appId", ignore = true)
    @Mapping(target = "identificador", ignore = true)
    @Mapping(target = "dataInici", expression = "java(source.getDataInici() != null ? source.getDataInici().toLocalDateTime() : null)")
    @Mapping(target = "dataFi", expression = "java(source.getDataFi() != null ? source.getDataFi().toLocalDateTime() : null)")
    @Mapping(target = "url", source = "source.redireccio")
    void updateAvis(es.caib.comanda.model.v1.avis.Avis source, @MappingTarget AvisEntity target);


    @AfterMapping
    default void mapEntornApp(AvisClientHelper avisClientHelper,
                              es.caib.comanda.model.v1.avis.Avis avis,
                              @MappingTarget Avis target) {
        EntornApp entornApp = toEntornApp(avisClientHelper, avis.getEntornCodi(), avis.getAppCodi());
        target.setEntornAppId(entornApp.getId());
        target.setEntornId(entornApp.getEntorn().getId());
        target.setAppId(entornApp.getApp().getId());
    }

    default EntornApp toEntornApp(AvisClientHelper avisClientHelper, String entornCodi, String appCodi) {
        Optional<EntornApp> entornApp = avisClientHelper.entornAppFindByEntornCodiAndAppCodi(
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
