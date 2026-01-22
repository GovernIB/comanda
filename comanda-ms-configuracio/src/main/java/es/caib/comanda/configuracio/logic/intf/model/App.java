package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig.ResourceSort;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.InputType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.io.Serializable;

/**
 * Informació d'una aplicació a monitoritzar.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
		quickFilterFields = { "codi", "nom" },
		descriptionField = "nom",
        defaultSortFields = { @ResourceSort(field = "ordre") },
        orderField = "ordre",
		accessConstraints = {
				@ResourceAccessConstraint(
						type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
						roles = { BaseConfig.ROLE_ADMIN },
						grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.DELETE }
				),
				@ResourceAccessConstraint(
						type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
						roles = { BaseConfig.ROLE_CONSULTA },
						grantedPermissions = { PermissionEnum.READ }
				)
		},
		artifacts = {
				@ResourceArtifact(type = ResourceArtifactType.REPORT, code = App.APP_EXPORT, requiresId = true),
				@ResourceArtifact(type = ResourceArtifactType.ACTION, code = App.APP_IMPORT, formClass = App.AppImportForm.class)
		})
public class App extends BaseResource<Long> {

	public final static String APP_EXPORT = "app_export";
	public final static String APP_IMPORT = "app_import";

	@NotNull
	@Size(max = 16)
	private String codi;
	@NotNull
	@Size(max = 100)
	private String nom;
	@Size(max = 1000)
	private String descripcio;
	@InputType("checkbox")
	private boolean activa = true;
	private byte[] logo;
    private Long ordre;

	private List<EntornApp> entornApps;

	// Formulari per a la importació (HAL-FORMS)
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AppImportForm implements Serializable {
		@NotNull
        private String jsonContent;
		private String decision; // OVERWRITE | COMBINE | SKIP (opcional)
	}
}
