package es.caib.comanda.configuracio.logic.intf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Informació d'una aplicació.
 *
 * @author Limit Tecnologies
 */
@Setter
@Getter
@NoArgsConstructor
public class App extends BaseResource<Long> {

	@NotNull
	@Size(max = 10)
	private String codi;
	@NotNull
	@Size(max = 100)
	private String nom;
	@NotNull
	@Size(max = 1000)
	private String descripcio;
	@NotNull
	@Size(max = 200)
	private String infoUrl;
	@NotNull
	@Size(max = 200)
	private String salutUrl;

}
