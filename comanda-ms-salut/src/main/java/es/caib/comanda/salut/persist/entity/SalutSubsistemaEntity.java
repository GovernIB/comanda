package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutSubsistema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entitat de base de dades que emmagatzema les informacions de salut
 * relacionades amb una integració.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "salut_subsistema")
@Getter
@Setter
@NoArgsConstructor
public class SalutSubsistemaEntity extends BaseEntity<SalutSubsistema> {

	@Column(name = "codi", nullable = false)
	private String codi;
	@Enumerated(EnumType.STRING)
	@Column(name = "estat", nullable = false)
	private SalutEstat estat;
	@Column(name = "latencia")
	private Integer latencia;
	@Column(name = "total_ok", nullable = false)
	private Long totalOk;
	@Column(name = "total_error", nullable = false)
	private Long totalError;

	// Percentatges d'estat agregats per període
    @Setter(AccessLevel.NONE)
	@Column(name = "pct_up", precision = 5, scale = 2)
	private java.math.BigDecimal pctUp;
    @Setter(AccessLevel.NONE)
	@Column(name = "pct_warn", precision = 5, scale = 2)
	private java.math.BigDecimal pctWarn;
    @Setter(AccessLevel.NONE)
	@Column(name = "pct_degraded", precision = 5, scale = 2)
	private java.math.BigDecimal pctDegraded;
    @Setter(AccessLevel.NONE)
	@Column(name = "pct_down", precision = 5, scale = 2)
	private java.math.BigDecimal pctDown;
    @Setter(AccessLevel.NONE)
	@Column(name = "pct_maintenance", precision = 5, scale = 2)
	private java.math.BigDecimal pctMaintenance;
    @Setter(AccessLevel.NONE)
	@Column(name = "pct_unknown", precision = 5, scale = 2)
	private java.math.BigDecimal pctUnknown;
    @Setter(AccessLevel.NONE)
    @Column(name = "latencia_mitjana")
    private Integer latenciaMitjana;

    @Setter(AccessLevel.NONE)
    @Column(name = "estat_num_elements")
    private Integer estatNumElements;
    @Setter(AccessLevel.NONE)
    @Column(name = "latencia_num_elements")
    private Integer latenciaNumElements;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "salut_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "salutsub_salut_fk"))
	private SalutEntity salut;


    // Mètodes per actualitzar percentatges d'estats i latències mitjanes
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        this.latenciaMitjana = novaLatencia;
        this.latenciaNumElements = 1;
    }

    public void addLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        if (this.latenciaNumElements == null || this.latenciaNumElements == 0) {
            this.latenciaMitjana = novaLatencia;
            this.latenciaNumElements = 1;
        } else {
            long total = ((long) this.latenciaMitjana * (long) this.latenciaNumElements) + (long) novaLatencia;
            long divisor = (long) this.latenciaNumElements + 1L;
            this.latenciaMitjana = Math.toIntExact(total / divisor);
            this.latenciaNumElements++;
        }
    }

    public void updatePctByEstat(SalutEstat estat) {
        if (estatNumElements == null) this.estatNumElements = 0;
        int newTotal = estatNumElements + 1;

        int currentUpElements = this.pctUp == null ? 0 : getElementsByPercentage(this.pctUp);
        int currentWarnElements = this.pctWarn == null ? 0 : getElementsByPercentage(this.pctWarn);
        int currentDegradedElements = this.pctDegraded == null ? 0 : getElementsByPercentage(this.pctDegraded);
        int currentDownElements = this.pctDown == null ? 0 : getElementsByPercentage(this.pctDown);
        int currentMaintenanceElements = this.pctMaintenance == null ? 0 : getElementsByPercentage(this.pctMaintenance);
        int currentUnknownElements = this.pctUnknown == null ? 0 : getElementsByPercentage(this.pctUnknown);

        int newUpElements = currentUpElements + (SalutEstat.UP.equals(estat) ? 1 : 0);
        int newWarnElements = currentWarnElements + (SalutEstat.WARN.equals(estat) ? 1 : 0);
        int newDegradedElements = currentDegradedElements + (SalutEstat.DEGRADED.equals(estat) ? 1 : 0);
        int newDownElements = currentDownElements + (SalutEstat.DOWN.equals(estat) || SalutEstat.ERROR.equals(estat) ? 1 : 0);
        int newMaintenanceElements = currentMaintenanceElements + (SalutEstat.MAINTENANCE.equals(estat) ? 1 : 0);
        int newUnknownElements = currentUnknownElements + (SalutEstat.UNKNOWN.equals(estat) ? 1 : 0);

        this.pctUp = percent(newUpElements, newTotal);
        this.pctWarn = percent(newWarnElements, newTotal);
        this.pctDegraded = percent(newDegradedElements, newTotal);
        this.pctDown = percent(newDownElements, newTotal);
        this.pctMaintenance = percent(newMaintenanceElements, newTotal);
        this.pctUnknown = percent(newUnknownElements, newTotal);
    }

    private int getElementsByPercentage(BigDecimal percentatge) {
        return Math.round(percentatge.multiply(BigDecimal.valueOf(estatNumElements))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .floatValue());
    }

    private BigDecimal percent(int part, int total) {
        if (total <= 0) return null;
        return BigDecimal.valueOf((part * 100.0) / total)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void addTotalOk(Long numOk) {
        if (numOk == null) return;
        if (this.totalOk == null) this.totalOk = 0L;
        this.totalOk += numOk;
    }

    public void addTotalError(Long numError) {
        if (numError == null) return;
        if (this.totalError == null) this.totalError = 0L;
        this.totalError += numError;
    }
}
