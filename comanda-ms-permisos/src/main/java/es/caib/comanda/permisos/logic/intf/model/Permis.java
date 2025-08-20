package es.caib.comanda.permisos.logic.intf.model;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.permisos.back.intf.validation.ValidPermisPrincipal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig
@ValidPermisPrincipal
public class Permis extends BaseResource<Long> {

    @NotNull
    private Long entornAppId;
    @Size(max = 64)
    private String usuari;
    @Size(max = 64)
    private String grup;
    @NotNull @NotEmpty
    private List<String> permisos;
    @NotNull
    private ResourceReference<Objecte, Long> objecte;

    private List<Objecte> objectesHereus;

}
