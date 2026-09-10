package es.caib.comanda.salut.logic.intf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Informació per als informes de historics de un entornApp.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalutInformeHistoricsParams implements Serializable {
	@NotNull
	private Long entornAppId;
}
