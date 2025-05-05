package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.ms.estadistica.model.Format;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class Indicador extends BaseResource<Long> {

    @NotNull
    @Size(max = 64)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    @NotNull
    @Size(max = 16)
    private String aplicacioCodi;
    private Format format;

}
