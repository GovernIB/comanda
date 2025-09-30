package es.caib.comanda.salut.logic.intf.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Informació per a l'informe històric d'estat del recurs salut.
 *
 * @author Límit Tecnologies
 */
@Getter
@AllArgsConstructor
public class SalutInformeEstatItem implements Serializable {

	private LocalDateTime data;
    @JsonIgnore	private int upCount;
    @JsonIgnore	private int warnCount;
    @JsonIgnore	private int degradedCount;
    @JsonIgnore	private int downCount;
    @JsonIgnore private int errorCount;
    @JsonIgnore	private int maintenanceCount;
    @JsonIgnore	private int unknownCount;

    @JsonIgnore private int totalCount;

    private double upPercent;
    private double warnPercent;
    private double degradedPercent;
    private double downPercent;
    private double errorPercent;
    private double maintenancePercent;
    private double unknownPercent;


    public SalutInformeEstatItem(SalutEntity salutEntity) {

        switch (salutEntity.getTipusRegistre()) {
            case HORA:
                this.data = salutEntity.getData().withMinute(0).withSecond(0);
                break;
            case DIA:
                this.data = salutEntity.getData().withHour(0).withMinute(0).withSecond(0);
                break;
			default:
                this.data = salutEntity.getData().withSecond(0);
        }

        this.upCount = salutEntity.getAppCountUp();
        this.warnCount = salutEntity.getAppCountWarn();
        this.degradedCount = salutEntity.getAppCountDegraded();
        this.downCount = salutEntity.getAppCountDown();
        this.errorCount = salutEntity.getAppCountError();
        this.maintenanceCount = salutEntity.getAppCountMaintenance();
        this.unknownCount = salutEntity.getAppCountUnknown();

        this.totalCount = salutEntity.getNumElements();

        this.upPercent = getPercent(upCount, totalCount);
        this.warnPercent = getPercent(warnCount, totalCount);
        this.degradedPercent = getPercent(degradedCount, totalCount);
        this.downPercent = getPercent(downCount, totalCount);
        this.errorPercent = getPercent(errorCount, totalCount);
        this.maintenancePercent = getPercent(maintenanceCount, totalCount);
        this.unknownPercent = getPercent(unknownCount, totalCount);
    }

                                 @JsonIgnore
	public long getNotUpCount() { return downCount + errorCount + maintenanceCount + unknownCount; }
	public boolean isAlwaysUp() { return getNotUpCount() == 0; }
	public boolean isAlwaysDown() { return getNotUpCount() == 100; }

    private long getCount(double pct) {
        if (totalCount == 0) return 0;
        return Math.round(pct * totalCount / 100);
    }

    private double getPercent(long part, long total) {
        if (total <= 0) return 0.0;
        return BigDecimal.valueOf((part * 100.0) / total).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
