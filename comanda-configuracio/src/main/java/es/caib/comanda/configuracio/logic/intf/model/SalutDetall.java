package es.caib.comanda.configuracio.logic.intf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Detall que forma part d'una informació de salut.
 *
 * @author Limit Tecnologies
 */
@Getter
@NoArgsConstructor
public class SalutDetall extends BaseResource<Long> {

	@NotNull
	@Size(max = 10)
	private String codi;
	@NotNull
	@Size(max = 100)
	private String nom;
	@NotNull
	@Size(max = 2048)
	private String valor;

	@Transient
	private ResourceReference<Salut, Long> salut;

}
