package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.InputType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Informació d'una aplicació a monitoritzar.
 *
 * @author Limit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(quickFilterFields = { "codi", "nom" })
public class App extends BaseResource<Long> {

	@NotNull
	@Size(max = 16)
	private String codi;
	@NotNull
	@Size(max = 100)
	private String nom;
	@Size(max = 1000)
	private String descripcio;
	@NotNull
	@Size(max = 200)
	private String infoUrl;
	@NotNull
	private Integer infoInterval = 1;
	@Setter(AccessLevel.NONE)
	private LocalDateTime infoData;
	@NotNull
	@Size(max = 200)
	private String salutUrl;
	@NotNull
	private Integer salutInterval = 1;
	@Size(max = 10)
	@Setter(AccessLevel.NONE)
	private String versio;
	@InputType("checkbox")
	private boolean activa;

	private Integer integracioCount;
	private Integer subsistemaCount;

	private List<AppIntegracio> integracions;
	private List<AppSubsistema> subsistemes;

}
