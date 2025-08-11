package es.caib.comanda.estadistica.logic.intf.model.widget;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ResourceConfig(
        quickFilterFields = { "nom" },
        descriptionField = "nom"
)
public class EntornResource extends BaseResource<Long> implements Serializable {

    private Long id;
    private String nom;

}