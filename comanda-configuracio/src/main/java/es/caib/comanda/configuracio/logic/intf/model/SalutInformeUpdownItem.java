package es.caib.comanda.configuracio.logic.intf.model;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Informació per a l'informe UP / DOWN del recurs salut.
 *
 * @author Limit Tecnologies
 */
@Getter
public class SalutInformeUpdownItem {

	private final String appCodi;
	private final LocalDateTime data;
	private final boolean up;

	public SalutInformeUpdownItem(
			String appCodi,
			LocalDateTime data,
			SalutEstat estat) {
		this.appCodi = appCodi;
		this.data = data;
		this.up = SalutEstat.UP.equals(estat);
	}

}
