package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
	quickFilterFields = { "entorn.codi", "entorn.nom" },
	artifacts = {
		@ResourceArtifact(type = ResourceArtifactType.ACTION, code = EntornApp.ENTORN_APP_ACTION_REFRESH, formClass = EntornApp.EntornAppParamAction.class),
		@ResourceArtifact(type = ResourceArtifactType.ACTION, code = EntornApp.ENTORN_APP_ACTION_REPROGRAMAR, formClass = EntornApp.EntornAppParamAction.class),
	}
)
public class EntornApp extends BaseResource<Long> {

	public final static String ENTORN_APP_ACTION_REFRESH = "refresh";
	public final static String ENTORN_APP_ACTION_REPROGRAMAR = "reprogramar";

	@Transient
	private ResourceReference<App, Long> app;
	@Transient
	private ResourceReference<Entorn, Long> entorn;

	// Informació de l'aplicació en l'entorn concret
	@NotNull
	@Size(max = 200)
	private String infoUrl;
	@NotNull
	@Builder.Default
	private Integer infoInterval = 1;
	@Setter(AccessLevel.NONE)
	private LocalDateTime infoData;
	@Size(max = 10)
	@Setter(AccessLevel.NONE)
	private String versio;
	@InputType("checkbox")
	@Builder.Default
	private boolean activa = true;

	// Informació de salut
	@NotNull
	@Size(max = 200)
	private String salutUrl;
	@NotNull
	@Builder.Default
	private Integer salutInterval = 1;

	private List<AppIntegracio> integracions;
	private List<AppSubsistema> subsistemes;

	private Integer integracioCount;
	private Integer subsistemaCount;

	// Informació d'estadístiques
	@Size(max = 200)
	private String estadisticaInfoUrl;
	@Size(max = 200)
	private String estadisticaUrl;
	private String estadisticaCron;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class EntornAppParamAction implements Serializable {
		private Long entornAppId;
	}

}
