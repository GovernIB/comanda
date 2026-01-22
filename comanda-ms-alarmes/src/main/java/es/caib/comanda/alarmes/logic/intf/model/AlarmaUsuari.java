package es.caib.comanda.alarmes.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Informació dels usuaris destinataris d'una alarma.
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
		}
)
public class AlarmaUsuari extends BaseResource<Long> {

	private String usuari;
	private boolean llegida;

	private ResourceReference<Alarma, Long> alarma;

}
