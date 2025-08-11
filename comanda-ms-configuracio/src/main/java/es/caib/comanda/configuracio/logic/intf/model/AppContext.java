package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Informació d'un context.
 *
 * @author Límit Tecnologies
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor()
public class AppContext extends BaseResource<Long> {

	@NotNull
	@Size(max = 16)
	private String codi;
	@NotNull
	@Size(max = 100)
	private String nom;
	private String path;
	private List<AppManual> manuals;
	private String api;
	private boolean actiu;

	@Transient
	private ResourceReference<EntornApp, Long> entornApp;

}
