package es.caib.comanda.salut.logic.intf.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Informació per a l'informe històric d'estat del recurs salut.
 *
 * @author Límit Tecnologies
 */
@Getter
@AllArgsConstructor
public class SalutInformeEstatItem implements Serializable {

	private LocalDateTime data;
	@JsonIgnore
	private long upCount;
	@JsonIgnore
	private long warnCount;
	@JsonIgnore
	private long degradedCount;
	@JsonIgnore
	private long downCount;
	@JsonIgnore
	private long mantenanceCount;
	@JsonIgnore
	private long unknownCount;

	public SalutInformeEstatItem(
			Date dataAgrupacio,
			long upCount,
			long warnCount,
			long degradedCount,
			long downCount,
			long mantenanceCount,
			long unknownCount) {
		this.data = dataAgrupacio.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		this.upCount = upCount;
		this.warnCount = warnCount;
		this.degradedCount = degradedCount;
		this.downCount = downCount;
		this.mantenanceCount = mantenanceCount;
		this.unknownCount = unknownCount;
	}

	@JsonIgnore
	public long getNotUpCount() {
		return downCount + mantenanceCount + unknownCount;
	}

	public long getTotalCount() {
		return upCount + warnCount + degradedCount + downCount + mantenanceCount + unknownCount;
	}

	public boolean isAlwaysUp() {
		return getNotUpCount() == 0;
	}

	public boolean isAlwaysDown() {
		return upCount == 0;
	}

	public double getUpPercent() {
		return Math.round(((double) upCount / getTotalCount()) * 10000.0) / 100.0;
	}

	public double getWarnPercent() {
		return Math.round(((double) warnCount / getTotalCount()) * 10000.0) / 100.0;
	}

	public double getDegradedPercentatge() {
		return Math.round(((double) degradedCount / getTotalCount()) * 10000.0) / 100.0;
	}

	public double getDownPercentatge() {
		return Math.round(((double) downCount / getTotalCount()) * 10000.0) / 100.0;
	}

	public double getMantenancePercentatge() {
		return Math.round(((double) mantenanceCount / getTotalCount()) * 10000.0) / 100.0;
	}

	public double getUnknownPercentatge() {
		return Math.round(((double) unknownCount / getTotalCount()) * 10000.0) / 100.0;
	}

}
