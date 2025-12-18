package es.caib.comanda.api.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface ApiMapper {

    @Mapping(target = "redireccio", source = "url")
    public es.caib.comanda.model.v1.tasca.Tasca convert(es.caib.comanda.client.model.Tasca tasca);
    public es.caib.comanda.model.v1.avis.Avis convert(es.caib.comanda.client.model.Avis avis);


    default LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }

    default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime() : null;
    }

}
