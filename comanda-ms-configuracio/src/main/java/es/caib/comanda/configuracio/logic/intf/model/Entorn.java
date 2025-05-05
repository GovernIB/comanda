package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Informació d'un entorn.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        quickFilterFields = { "codi", "nom" },
        descriptionField = "nom")
public class Entorn extends BaseResource<Long> {

    @NotNull
    @Size(max = 16)
    private String codi;
    @Size(max = 255)
    private String nom;

}
