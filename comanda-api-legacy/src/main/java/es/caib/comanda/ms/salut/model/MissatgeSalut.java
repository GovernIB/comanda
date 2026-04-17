package es.caib.comanda.ms.salut.model;

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
public class MissatgeSalut implements Serializable {
    @JsonSerialize(using = Rfc3339DateSerializer.class)
    @JsonDeserialize(using = LenientDateDeserializer.class)
    private Date data;
    private SalutNivell nivell;
    private String missatge;
}
