package es.caib.comanda.tasques.logic.intf.model;

import es.caib.comanda.ms.broker.model.Prioritat;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        descriptionField = "nom",
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.FILTER, code = Tasca.FILTER, formClass = Tasca.TascaFilter.class),
                @ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = Tasca.PERSPECTIVE_PATH),
                @ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = Tasca.PERSPECTIVE_EXPIRATION),
        }
)
public class Tasca extends BaseResource<Long> {

    public static final String FILTER = "FILTER";
    public static final String PERSPECTIVE_PATH = "PATH";
    public static final String PERSPECTIVE_EXPIRATION = "EXPIRATION";

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
    private LocalDateTime dataInici;
    private LocalDateTime dataFi;
    private LocalDateTime dataCaducitat;
    @NotNull @Size(max = 255)
    private URL url;
    @Size(max = 128)
    private String responsable;

    private List<String> usuarisAmbPermis;
    private List<String> grupsAmbPermis;

    @Transient private String[] treePath;
    @Transient private Long diesPerCaducar;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class TascaFilter implements Serializable {
//        private ResourceReference<App, Long> app;
//        private ResourceReference<Entorn, Long> entorn;

        private String nom;
        private String descripcio;
        private Prioritat prioritat;
        private LocalDateTime dataInici;
        private LocalDateTime dataFi;
        private Boolean acabat = true;
    }
}
