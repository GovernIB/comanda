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
    private long errorCount;
	@JsonIgnore
	private long maintenanceCount;
	@JsonIgnore
	private long unknownCount;

	public SalutInformeEstatItem(
			Date dataAgrupacio,
			long upCount,
			long warnCount,
			long degradedCount,
			long downCount,
            long errorCount,
			long maintenanceCount,
			long unknownCount) {
		this.data = dataAgrupacio.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		this.upCount = upCount;
		this.warnCount = warnCount;
		this.degradedCount = degradedCount;
		this.downCount = downCount;
        this.errorCount = errorCount;
		this.maintenanceCount = maintenanceCount;
		this.unknownCount = unknownCount;
	}

	@JsonIgnore
	public long getNotUpCount() {
		return downCount + errorCount + maintenanceCount + unknownCount;
	}

	public long getTotalCount() {
		return upCount + warnCount + degradedCount + downCount + errorCount + maintenanceCount + unknownCount;
	}

	public boolean isAlwaysUp() {
		return getNotUpCount() == 0;
	}

	public boolean isAlwaysDown() {
		return upCount == 0;
	}

	public double getUpPercent() {return round(upCount);}

	public double getWarnPercent() {return round(warnCount);}

	public double getDegradedPercent() {return round(degradedCount);}

	public double getDownPercent() {return round(downCount);}

    public double getErrorPercent() {return round(errorCount);}

	public double getMaintenancePercent() {return round(maintenanceCount);}

	public double getUnknownPercent() {
		return round(unknownCount);
	}

    private double round(long value) {
        return Math.round(((double) value / getTotalCount()) * 10000.0) / 100.0;
    }
}
