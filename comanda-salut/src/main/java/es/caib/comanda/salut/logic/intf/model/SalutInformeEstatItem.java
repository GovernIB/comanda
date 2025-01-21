package es.caib.comanda.salut.logic.intf.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Informació per a l'informe històric d'estat del recurs salut.
 *
 * @author Limit Tecnologies
 */
@Getter
@AllArgsConstructor
public class SalutInformeEstatItem {

	private LocalDateTime data;
	@JsonIgnore
	private long upCount;
	@JsonIgnore
	private long downCount;
	@JsonIgnore
	private long outOfServiceCount;
	@JsonIgnore
	private long unknownCount;

	public SalutInformeEstatItem(
			Date dataAgrupacio,
			long upCount,
			long downCount,
			long outOfServiceCount,
			long unknownCount) {
		this.data = dataAgrupacio.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		this.upCount = upCount;
		this.downCount = downCount;
		this.outOfServiceCount = outOfServiceCount;
		this.unknownCount = unknownCount;
	}

	@JsonIgnore
	public long getNotUpCount() {
		return downCount + outOfServiceCount + unknownCount;
	}

	public long getTotalCount() {
		return upCount + downCount + outOfServiceCount + unknownCount;
	}

	public boolean isAlwaysUp() {
		return getNotUpCount() == 0;
	}

	public boolean isAlwaysDown() {
		return upCount == 0;
	}

}
