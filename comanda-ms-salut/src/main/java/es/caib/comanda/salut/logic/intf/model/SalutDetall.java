package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Detall que forma part d'una informació de salut.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
