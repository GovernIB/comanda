package es.caib.comanda.estadistica.logic.intf.model.cache;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ResourceConfig(
        descriptionField = "id"
)
public class ComandaCache extends BaseResource<String> {

    private String descripcio;
    private Integer entrades;
    private Long mida;

}
