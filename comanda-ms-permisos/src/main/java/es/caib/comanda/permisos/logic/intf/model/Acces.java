package es.caib.comanda.permisos.logic.intf.model;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.permisos.back.intf.validation.ValidPermisPrincipal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig
@ValidPermisPrincipal
public class Acces extends BaseResource<Long> {

    @Size(max = 64)
    private String acces;

}
