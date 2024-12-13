package es.caib.comanda.configuracio.logic.intf.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.InputType;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Informació d'una aplicació a monitoritzar.
 *
 * @author Limit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
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
	private Integer infoInterval;
	@Setter(AccessLevel.NONE)
	private LocalDateTime infoData;
	@NotNull
	@Size(max = 200)
	private String salutUrl;
	@NotNull
	private Integer salutInterval;
	@Size(max = 10)
	@Setter(AccessLevel.NONE)
	private String versio;
	@InputType("checkbox")
	private boolean activa;

	private Integer integracioCount;
	private Integer subsistemaCount;

}
