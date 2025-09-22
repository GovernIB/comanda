package es.caib.comanda.salut.logic.intf.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;

/**
 * Informació de salut retornada per una app que està relacionada amb
 * una integració.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
public class SalutSubsistema extends BaseResource<Long> {

	@NotNull
	private String codi;
	@NotNull
	private SalutEstat estat;
	private Integer latencia;
	private Long totalOk;
	Long totalError;
	Integer totalTempsMig;
	Long peticionsOkUltimPeriode;
	Long peticionsErrorUltimPeriode;
	Integer tempsMigUltimPeriode;

	private ResourceReference<Salut, Long> salut;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private String nom;

}
