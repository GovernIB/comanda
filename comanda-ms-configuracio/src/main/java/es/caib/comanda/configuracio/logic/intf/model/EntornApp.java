package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.validation.EntornAppExists;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig.ResourceSort;
import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.validator.constraints.URL;
import org.springframework.hateoas.InputType;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Informació d'una aplicació a monitoritzar.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ResourceConfig(
		descriptionField = "entornAppDescription",
		quickFilterFields = { "entorn.codi", "entorn.nom", "app.codi", "app.nom" },
        defaultSortFields = {
                @ResourceSort(field = "app.ordre"),
                @ResourceSort(field = "entorn.ordre")
        },
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
				@ResourceArtifact(type = ResourceArtifactType.ACTION, code = EntornApp.ENTORN_APP_ACTION_PING_URL, formClass = String.class),
				@ResourceArtifact(type = ResourceArtifactType.ACTION, code = EntornApp.ENTORN_APP_TOOGLE_ACTIVA, requiresId = true),
				@ResourceArtifact(type = ResourceArtifactType.REPORT, code = EntornApp.REPORT_LLISTAR_LOGS, requiresId = true),
				@ResourceArtifact(type = ResourceArtifactType.REPORT, code = EntornApp.REPORT_DESCARREGAR_LOG, requiresId = true, formClass = String.class),
				@ResourceArtifact(type = ResourceArtifactType.REPORT, code = EntornApp.REPORT_PREVISUALITZAR_LOG, requiresId = true, formClass = EntornApp.PrevisualitzarLogParams.class),
				@ResourceArtifact(type = ResourceArtifactType.FILTER, code = EntornApp.ENTORN_APP_FILTER, formClass = EntornApp.EntornAppFilter.class),
				@ResourceArtifact(type = ResourceArtifactType.FILTER, code = EntornApp.SALUT_ENTORN_APP_FILTER, formClass = EntornApp.SalutEntornAppFilter.class)
		}
)
@EntornAppExists
@FieldNameConstants
public class EntornApp extends BaseResource<Long> {

	public final static String ENTORN_APP_ACTION_PING_URL = "pingUrl";
	public final static String ENTORN_APP_FILTER = "entornApp_filter";
	public final static String SALUT_ENTORN_APP_FILTER = "salut_entornApp_filter";
	public final static String ENTORN_APP_TOOGLE_ACTIVA = "toogle_activa";
	public final static String REPORT_LLISTAR_LOGS = "llistar_logs";
	public final static String REPORT_DESCARREGAR_LOG = "descarregar_log";
	public final static String REPORT_PREVISUALITZAR_LOG = "previsualitzar_log";

	@NotNull
	@Transient
	private ResourceReference<App, Long> app;
	@NotNull
	@Transient
	private ResourceReference<Entorn, Long> entorn;

	// Informació de l'aplicació en l'entorn concret
	@URL
	@NotNull
	@Size(max = 200)
	private String infoUrl;
	@Setter(AccessLevel.NONE)
	private LocalDateTime infoData;
	@Size(max = 10)
	@Setter(AccessLevel.NONE)
	private String versio;
	@Size(max = 64)
	@Setter(AccessLevel.NONE)
	private String revisio;
	@Size(max = 10)
	@Setter(AccessLevel.NONE)
	private String jdkVersion;
	@InputType("checkbox")
	@Builder.Default
	private boolean activa = true;

	// Informació de salut
	@URL
	@NotNull
	@Size(max = 200)
	private String salutUrl;

	private List<AppIntegracio> integracions;
	private List<AppSubsistema> subsistemes;
	private List<AppContext> contexts;

	private Integer integracioCount;
	private Integer subsistemaCount;

	// Informació d'estadístiques
	@URL
	@Size(max = 200)
	private String estadisticaInfoUrl;
	@Size(max = 200)
	@URL
	private String estadisticaUrl;
	private String estadisticaCron;

    @Builder.Default
    private Boolean compactable = false;
    private Integer compactacioSetmanalMesos;
    private Integer compactacioMensualMesos;
    private Integer eliminacioMesos;

	// Camps calculats
	private String entornAppDescription;

	public String getRevisioSimplificat() {
		return revisio != null ? revisio.substring(0, 7) : null;
	}


	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldNameConstants
	public static class SalutEntornAppFilter implements Serializable {
		protected ResourceReference<App, Long> app;
		protected ResourceReference<Entorn, Long> entorn;
		protected ResourceReference<EntornApp, Long> entornApp;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldNameConstants
	public static class EntornAppFilter implements Serializable {
		@NotNull
		@ResourceField(onChangeActive = true)
		protected ResourceReference<App, Long> app;
		@NotNull
		@ResourceField(onChangeActive = true)
		protected ResourceReference<Entorn, Long> entorn;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@FieldNameConstants
	public static class PingUrlResponse implements Serializable {
		private Boolean success;
		private String message;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PrevisualitzarLogParams implements Serializable {
		private String fileName;
		private Integer lineCount;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PrevisualitzarLogResponse implements Serializable {
		private String linia;
	}

}
