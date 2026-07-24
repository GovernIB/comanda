package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@NoArgsConstructor
@FieldNameConstants
@ResourceConfig(
        quickFilterFields = { "codi", "denominacio" },
        descriptionField = "codi"
)
public class UnitatOrganitzativa extends BaseResource<Long> {

    private String codi;
    private String denominacio;
    private String nifCif;
    private String codiUnitatSuperior;
    private String codiUnitatArrel;
    private UOEstatEnum estat;

}
