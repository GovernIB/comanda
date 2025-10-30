package es.caib.comanda.tasques.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.broker.model.Prioritat;
import es.caib.comanda.ms.broker.model.TascaEstat;
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

import javax.persistence.Column;
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
    private Long entornId;
    private Long appId;

    @NotNull @Size(max = 64)
    private String identificador;
    @NotNull @Size(max = 64)
    private String tipus;
    @NotNull @Size(max = 255)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    @NotNull
    private TascaEstat estat;
    @Size(max = 1024)
    private String estatDescripcio;
    @Size(max = 128)
    private String numeroExpedient;
    private Prioritat prioritat;
    private LocalDateTime dataInici;
    private LocalDateTime dataFi;
    private LocalDateTime dataCaducitat;
    @NotNull @Size(max = 255)
    private URL url;
    @Size(max = 128)
    private String responsable;
    @Size(max = 128)
    private String grup;

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
        private String nom;
        private String descripcio;
        private String tipus;
        private String numeroExpedient;
        private Prioritat prioritat;
        private LocalDateTime dataInici1;
        private LocalDateTime dataInici2;
        private LocalDateTime dataFi1;
        private LocalDateTime dataFi2;
        private LocalDateTime dataCaducitat1;
        private LocalDateTime dataCaducitat2;
    }
}
