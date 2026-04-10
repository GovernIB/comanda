package es.caib.comanda.alarmes.logic.intf.model;

import es.caib.comanda.alarmes.back.intf.validation.ValidAdminValue;
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

import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
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
						type = ResourceAccessConstraint.ResourceAccessConstraintType.AUTHENTICATED,
						grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE }
				),
				@ResourceAccessConstraint(
						type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
						roles = { BaseConfig.ROLE_ADMIN, BaseConfig.ROLE_CONSULTA },
						grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE }
				),
		},
		artifacts = {
				@ResourceArtifact(type = ResourceArtifactType.ACTION, code = AlarmaConfig.ALARMA_CONFIG_DELETE_ACTION, requiresId = true),
                @ResourceArtifact(type = ResourceArtifactType.FILTER, code = AlarmaConfig.ALARMA_CONFIG_FILTER, formClass = AlarmaConfig.AlarmaConfigFilter.class)
		}
)
public class AlarmaConfig extends BaseResource<Long> {

	public final static String ALARMA_CONFIG_DELETE_ACTION = "delete_alarmaConfig";
    public final static String ALARMA_CONFIG_FILTER = "alarmaConfig_filter";

	@NotNull
	private Long entornAppId;
	@Size(max = 200)
	private String nom;
	@NotNull
	@Size(max = 1024)
	private String missatge;
	@NotNull
	private AlarmaConfigTipus tipus;
	private AlarmaConfigCondicio condicio;
    @Digits(integer = 15, fraction = 4)
	private BigDecimal valor;
	private AlarmaConfigPeriodeUnitat periodeUnitat;
	private BigDecimal periodeValor;
    @ValidAdminValue
	private boolean admin;
    @ValidAdminValue
	private boolean correuGeneric;
    @Transient
    private AlarmaTipusUsuari tipusUsuariAlarma;

    public AlarmaTipusUsuari getTipusUsuariAlarma() {
        return admin
                ? (correuGeneric ? AlarmaTipusUsuari.ADMINISTRADOR_GENERIC : AlarmaTipusUsuari.ADMINISTRADOR)
                : (correuGeneric ? AlarmaTipusUsuari.USUARI_GENERIC        : AlarmaTipusUsuari.USUARI);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class AlarmaConfigFilter implements Serializable {
        protected Long entornAppId;
        protected String nom;
        protected AlarmaConfigTipus tipus;
        private boolean admin;
        private boolean correuGeneric;
    }
}
