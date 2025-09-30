package es.caib.comanda.salut.logic.intf.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Informació per a l'informe del llistat de salut.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
public class SalutInformeLlistatParams implements Serializable {
	@NotNull
	private List<Long> entornAppIdList;
	@NotNull
	private LocalDateTime dataInici;
	@NotNull
	private LocalDateTime dataFi;
	@NotNull
	private SalutInformeAgrupacio agrupacio;
}
