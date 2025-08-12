package es.caib.comanda.avisos.logic.intf.model;

import es.caib.comanda.ms.broker.model.AvisTipus;
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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        descriptionField = "nom",
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.FILTER, code = Avis.FILTER, formClass = Avis.AvisFilter.class),
                @ResourceArtifact(type = ResourceArtifactType.PERSPECTIVE, code = Avis.PERSPECTIVE_PATH),
        }
)
public class Avis extends BaseResource<Long> {

    public static final String FILTER = "FILTER";
    public static final String PERSPECTIVE_PATH = "PATH";

    @NotNull
    private Long entornAppId;

    @NotNull @Size(max = 64)
    private String identificador;
    @NotNull
    private AvisTipus tipus;
    @NotNull @Size(max = 255)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    private LocalDateTime dataInici;
    private LocalDateTime dataFi;

    @Transient private String[] treePath;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class AvisFilter implements Serializable {
//        private ResourceReference<App, Long> app;
//        private ResourceReference<Entorn, Long> entorn;

        private String nom;
        private String descripcio;
        private AvisTipus tipus;
        private LocalDateTime dataInici;
        private LocalDateTime dataFi;
    }
}
