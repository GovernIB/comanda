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
 * Informació d'un subsistema.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor()
public class AppManual extends BaseResource<Long> {

	@NotNull
	@Size(max = 128)
	private String nom;
	@NotNull
	@Size(max = 255)
	private String path;

	@Transient
	private ResourceReference<AppContext, Long> appContext;

}
