package es.caib.comanda.alarmes.logic.intf.model;

import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Informació d'una alarma.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        descriptionField = "nom"
)
public class AlarmaTrigger extends BaseResource<Long> {

	@NotNull
	private Long entornAppId;
	@NotNull
	@Size(max = 1024)
	private String nom;
	@NotNull
	@Size(max = 1024)
	private String missatge;
	private AlarmaEstat estat;
	private boolean notificada;

}
