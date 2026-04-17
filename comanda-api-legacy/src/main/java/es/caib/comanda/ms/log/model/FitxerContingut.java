package es.caib.comanda.ms.log.model;

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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FitxerContingut implements Serializable {
    private byte[] contingut;
    private String mimeType;
    private String nom;
    @JsonSerialize(using = Rfc3339DateSerializer.class)
    @JsonDeserialize(using = LenientDateDeserializer.class)
    private Date dataCreacio;
    @JsonSerialize(using = Rfc3339DateSerializer.class)
    @JsonDeserialize(using = LenientDateDeserializer.class)
    private Date dataModificacio;
    private Long mida;
}
