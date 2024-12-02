package es.caib.comanda.configuracio.logic.intf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * Informació de salut retornada per una app que està relacionada amb
 * una integració.
 *
 * @author Limit Tecnologies
 */
@Getter
@NoArgsConstructor
public class SalutIntegracio extends BaseResource<Long> {

	@NotNull
	private String codi;
	@NotNull
	private SalutEstat estat;
	private Integer latencia;
	@NotNull
	private Integer totalOk;
	@NotNull
	private Integer totalError;

	@Transient
	private ResourceReference<Salut, Long> salut;

}
