package es.caib.comanda.alarmes.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Informació de configuració d'una alarma.
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
						roles = { BaseConfig.ROLE_ADMIN },
						grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.DELETE }
				),
		}
)
public class AlarmaConfig extends BaseResource<Long> {

	@NotNull
	private Long entornAppId;
	@NotNull
	@Size(max = 200)
	private String nom;
	@NotNull
	@Size(max = 1024)
	private String missatge;
	@NotNull
	private AlarmaConfigTipus tipus;
	@NotNull
	private AlarmaConfigCondicio condicio;
	@NotNull
	private BigDecimal valor;
	private AlarmaConfigPeriodeUnitat periodeUnitat;
	private BigDecimal periodeValor;
	@NotNull
	private Boolean admin;

}
