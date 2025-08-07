package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FetMapper {

    Fet toFet(FetEntity entity);
    FetEntity toFetEntity(Fet fet);

}
