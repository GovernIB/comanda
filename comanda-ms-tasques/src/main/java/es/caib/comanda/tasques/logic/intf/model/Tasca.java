package es.caib.comanda.tasques.logic.intf.model;

import es.caib.comanda.ms.broker.model.Prioritat;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        descriptionField = "nom"
)
public class Tasca extends BaseResource<Long> {

    @NotNull
    private Long entornAppId;

    @NotNull @Size(max = 64)
    private String identificador;
    @NotNull @Size(max = 64)
    private String tipus;
    @NotNull @Size(max = 255)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    private Prioritat prioritat;
    private LocalDate dataInici;
    private LocalDate dataFi;
    private LocalDate dataCaducitat;
    @NotNull @Size(max = 255)
    private URL url;
    @Size(max = 128)
    private String responsable;

    private List<String> usuarisAmbPermis;
    private List<String> grupsAmbPermis;

}
