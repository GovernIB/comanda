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
public class SalutInfo implements Serializable {
    private String codi;
    @JsonSerialize(using = Rfc3339DateSerializer.class)
    @JsonDeserialize(using = LenientDateDeserializer.class)
    private Date data;
    private EstatSalut estatGlobal;
    private EstatSalut estatBaseDeDades;
    private List<IntegracioSalut> integracions;
    private InformacioSistema informacioSistema;
    private List<MissatgeSalut> missatges;
    private String versio;
    private List<SubsistemaSalut> subsistemes;
}
