package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.InputType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

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
		artifacts = {
				@ResourceArtifact(type = ResourceArtifactType.REPORT, code = App.APP_EXPORT, requiresId = true),
//				@ResourceArtifact(type = ResourceArtifactType.ACTION, code = App.APP_IMPORT)
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


	private List<EntornApp> entornApps;

}
