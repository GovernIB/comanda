package es.caib.comanda.salut.logic.intf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Informació per a l'informe de latència del recurs salut.
 *
 * @author Limit Tecnologies
 */
@Getter
@AllArgsConstructor
public class SalutInformeLatenciaItem {

	private LocalDateTime data;
	private Double latenciaMitja;

	public SalutInformeLatenciaItem(
			Date dataAgrupacio,
			Double latenciaMitja) {
		this.data = dataAgrupacio.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		this.latenciaMitja = latenciaMitja;
	}

}
