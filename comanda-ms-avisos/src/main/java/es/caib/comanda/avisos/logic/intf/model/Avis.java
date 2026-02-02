package es.caib.comanda.avisos.logic.intf.model;

import es.caib.comanda.model.v1.avis.AvisTipus;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
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
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN, BaseConfig.ROLE_CONSULTA },
                        grantedPermissions = { PermissionEnum.READ }
                )
        },
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
    private Long entornId;
    private Long appId;

    @NotNull
    private String identificador;
    @NotNull
    private AvisTipus tipus;
    @NotNull @Size(max = 255)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    private LocalDateTime dataInici;
    private LocalDateTime dataFi;

    @Size(max = 255)
    private URL url;
    @Size(max = 128)
    private String responsable;
    @Size(max = 128)
    private String grup;

    private List<String> usuarisAmbPermis;
    private List<String> grupsAmbPermis;

    @Transient private String[] treePath;
    @Transient private String entornCodi;
    @Transient private String appCodi;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class AvisFilter implements Serializable {
        private String nom;
        private String descripcio;
        private AvisTipus tipus;
        private LocalDateTime dataInici1;
        private LocalDateTime dataInici2;
        private LocalDateTime dataFi1;
        private LocalDateTime dataFi2;
    }
}
