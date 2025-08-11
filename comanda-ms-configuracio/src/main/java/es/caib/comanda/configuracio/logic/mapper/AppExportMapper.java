package es.caib.comanda.configuracio.logic.mapper;

import es.caib.comanda.configuracio.logic.intf.model.export.AppExport;
import es.caib.comanda.configuracio.logic.intf.model.export.EntornAppExport;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper per convertir una AppEntity a una AppExport.
 * També inclou la conversió de EntornAppEntity a EntornAppExport.
 */
@Mapper(componentModel = "spring")
public interface AppExportMapper {

    /**
     * Converteix una AppEntity a una AppExport.
     * Els camps amb el mateix nom es mapegen automàticament.
     * La llista d'entornApps es mapeja utilitzant el mètode toEntornAppExport.
     */
    public AppExport toExport(AppEntity entity);
    public List<AppExport> toExport(List<AppEntity> entities);
    
    /**
     * Converteix una EntornAppEntity a una EntornAppExport.
     * El camp entornCodi es mapeja des del codi de l'entorn.
     * La resta de camps amb el mateix nom es mapegen automàticament.
     */
    @Mapping(target = "entornCodi", source = "entorn.codi")
    @Mapping(target = "entornNom", source = "entorn.nom")
    public EntornAppExport toEntornAppExport(EntornAppEntity entity);

}
