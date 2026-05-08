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
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AppInfo implements Serializable {
    private String codi;
    private String nom;
    @JsonSerialize(using = Rfc3339DateSerializer.class)
    @JsonDeserialize(using = LenientDateDeserializer.class)
    private Date data;
    private String versio;
    private String revisio;
    private String jdkVersion;
    private String versioJboss;
    private List<IntegracioInfo> integracions;
    private List<SubsistemaInfo> subsistemes;
    private List<ContextInfo> contexts;
}
