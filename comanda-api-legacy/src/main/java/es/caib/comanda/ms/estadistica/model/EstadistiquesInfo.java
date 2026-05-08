package es.caib.comanda.ms.estadistica.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import es.caib.comanda.legacy.json.LenientDateDeserializer;
import es.caib.comanda.legacy.json.Rfc3339DateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EstadistiquesInfo implements Serializable {
    private String codi;
    private String versio;
    @JsonSerialize(using = Rfc3339DateSerializer.class)
    @JsonDeserialize(using = LenientDateDeserializer.class)
    private Date data;
    private List<DimensioDesc> dimensions;
    private List<IndicadorDesc> indicadors;
}
