package es.caib.comanda.alarmes.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Informació d'una alarma.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
		descriptionField = "nom",
		accessConstraints = {
				@ResourceAccessConstraint(
						type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
						roles = { BaseConfig.ROLE_ADMIN, BaseConfig.ROLE_CONSULTA },
						grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE }
				),
		},
		artifacts = {
				@ResourceArtifact(type = ResourceArtifactType.ACTION, code = Alarma.ESBORRAR_ACTION, requiresId = true),
				@ResourceArtifact(type = ResourceArtifactType.ACTION, code = Alarma.ESBORRAR_TOTES_ACTION),
		}
)
public class Alarma extends BaseResource<Long> {

	public static final String ESBORRAR_ACTION = "ALARMA_ESBORRAR";
	public static final String ESBORRAR_TOTES_ACTION = "ALARMA_ESBORRAR_TOTES";

	@NotNull
	private Long entornAppId;
	@NotNull
	@Size(max = 1024)
	private String missatge;
	private AlarmaEstat estat;
	private LocalDateTime dataActivacio;
	private LocalDateTime dataEnviament;
	private LocalDateTime dataEsborrat;

	@NotNull
	private ResourceReference<AlarmaConfig, Long> alarmaConfig;

}
