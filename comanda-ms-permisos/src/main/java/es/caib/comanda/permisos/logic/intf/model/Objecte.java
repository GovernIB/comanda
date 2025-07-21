package es.caib.comanda.permisos.logic.intf.model;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(descriptionField = "nom")
public class Objecte extends BaseResource<Long> {

    @NotNull @Size(max = 64)
    private String tipus;
    @NotNull @Size(max = 255)
    private String nom;
    @NotNull
    private String identificador;

}
