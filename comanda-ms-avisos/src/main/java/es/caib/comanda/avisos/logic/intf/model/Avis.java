package es.caib.comanda.avisos.logic.intf.model;

import es.caib.comanda.ms.broker.model.AvisTipus;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        descriptionField = "nom"
)
public class Avis extends BaseResource<Long> {

    @NotNull
    private Long entornAppId;

    @NotNull @Size(max = 64)
    private String identificador;
    @NotNull
    private AvisTipus tipus;
    @NotNull @Size(max = 255)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    private LocalDate dataInici;
    private LocalDate dataFi;

}
