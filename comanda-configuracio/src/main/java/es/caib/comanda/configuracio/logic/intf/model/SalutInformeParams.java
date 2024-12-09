package es.caib.comanda.configuracio.logic.intf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Informació per a l'informe de latència del recurs salut.
 *
 * @author Limit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
public class SalutInformeParams {

	@NotNull
	private String appCodi;
	@NotNull
	private LocalDateTime dataInici;
	@NotNull
	private LocalDateTime dataFi;
	@NotNull
	private SalutInformeAgrupacio agrupacio;

}
