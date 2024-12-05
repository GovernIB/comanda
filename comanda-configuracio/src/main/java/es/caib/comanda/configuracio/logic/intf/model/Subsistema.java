package es.caib.comanda.configuracio.logic.intf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Informació d'un subsistema.
 *
 * @author Limit Tecnologies
 */
@Getter
@NoArgsConstructor
public class Subsistema extends BaseResource<Long> {

	@NotNull
	@Size(max = 16)
	private String codi;
	@NotNull
	@Size(max = 100)
	private String nom;
	private boolean actiu;

	@Transient
	private ResourceReference<App, Long> app;

}
