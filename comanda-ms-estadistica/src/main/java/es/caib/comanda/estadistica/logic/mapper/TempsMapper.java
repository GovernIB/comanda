package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.estadistica.persist.entity.estadistiques.TempsEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TempsMapper {

    Temps toTemps(TempsEntity entity);
    TempsEntity toTempsEntity(Temps temps);

}
