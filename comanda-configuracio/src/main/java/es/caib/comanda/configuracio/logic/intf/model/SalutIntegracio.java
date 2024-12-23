package es.caib.comanda.configuracio.logic.intf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * Informació de salut retornada per una app que està relacionada amb
 * una integració.
 *
 * @author Limit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
public class SalutIntegracio extends BaseResource<Long> {

	@NotNull
	private String codi;
	@NotNull
	private SalutEstat estat;
	private Integer latencia;
	@NotNull
	private Long totalOk;
	@NotNull
	private Long totalError;

	private ResourceReference<Salut, Long> salut;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String nom;

}
