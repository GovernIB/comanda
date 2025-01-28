package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Informació d'una integració.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AppIntegracio extends BaseResource<Long> {

	@NotNull
	@Size(max = 10)
	private String codi;
	@NotNull
	@Size(max = 100)
	private String nom;
	private boolean activa;

	@Transient
	private ResourceReference<App, Long> app;

}
