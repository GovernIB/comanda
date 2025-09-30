package es.caib.comanda.salut.logic.intf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Informació per als informes de estats i latència de un recurs salut.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalutInformeParams implements Serializable {
	@NotNull
	private LocalDateTime dataFi; // TODO Canviar nom a dataReferencia al fer merge de la branca WIP
	@NotNull
	private Long entornAppId;
	@NotNull
	private SalutInformeAgrupacio agrupacio;
}
