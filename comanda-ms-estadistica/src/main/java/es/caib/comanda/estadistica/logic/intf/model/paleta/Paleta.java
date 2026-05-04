package es.caib.comanda.estadistica.logic.intf.model.paleta;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ResourceConfig(
        quickFilterFields = { "nom" },
        descriptionField = "nom"
)
public class Paleta extends BaseResource<Long> {

    @NotNull
    @Size(max = 128)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    private Integer ordre;
    private List<PaletaColor> colors;

    @Transient
    @Size(max = 64)
    private String clientId;

    @Transient
    private String key;
    @Transient
    private String value;
}
