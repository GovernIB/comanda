package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutIntegracio;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static lombok.AccessLevel.NONE;

/**
 * Entitat de base de dades que emmagatzema les informacions de salut
 * relacionades amb una integració.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "salut_integracio")
@Getter
@Setter
@NoArgsConstructor
public class SalutIntegracioEntity extends BaseEntity<SalutIntegracio> {
    public static final int CODI_MAX_LENGTH = 32;

	@Column(name = "codi", length = CODI_MAX_LENGTH, nullable = false)	private String codi;
	@Enumerated(EnumType.STRING)
	@Column(name = "estat", nullable = false)	            private SalutEstat estat;
	@Column(name = "latencia")                          	private Integer latencia;
	@Column(name = "total_ok", nullable = false)            private Long totalOk;
	@Column(name = "total_error", nullable = false)     	private Long totalError;
	@Column(name = "total_tempsmig", nullable = false) 	    private Integer totalTempsMig;
	@Column(name = "pet_ok_ultperiode", nullable = false) 	private Long peticionsOkUltimPeriode;
	@Column(name = "pet_error_ultperiode", nullable = false) private Long peticionsErrorUltimPeriode;
	@Column(name = "temps_mig_ultperiode", nullable = false) private Integer tempsMigUltimPeriode;
	@Column(name = "endpoint", length = 255)                private String endpoint;

	// Comptadors d'estat agregats per període
    @Setter(NONE) @Column(name = "count_up")  	            private int countUp = 0;
    @Setter(NONE) @Column(name = "count_warn")	            private int countWarn = 0;
    @Setter(NONE) @Column(name = "count_degraded")	        private int countDegraded = 0;
    @Setter(NONE) @Column(name = "count_down")    	        private int countDown = 0;
    @Setter(NONE) @Column(name = "count_error")   	        private int countError = 0;
    @Setter(NONE) @Column(name = "count_maintenance")	    private int countMaintenance = 0;
    @Setter(NONE) @Column(name = "count_unknown") 	        private int countUnknown = 0;
    @Setter(NONE) @Column(name = "estat_num_elements")      private int estatNumElements = 0;

    @Column(name = "latencia_mitjana")                      private Integer latenciaMitjana;
    @Setter(NONE) @Column(name = "latencia_num_elements")   private int latenciaNumElements = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "salut_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "salutint_salut_fk"))
	private SalutEntity salut;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(
			name = "pare_id",
			referencedColumnName = "id",
			foreignKey = @ForeignKey(name = BaseConfig.DB_PREFIX + "salutint_pare_fk"))
	private SalutIntegracioEntity pare;

    // Mètodes per actualitzar percentatges d'estats i latències mitjanes
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        this.latenciaMitjana = novaLatencia;
        this.latenciaNumElements = 1;
    }

    public void addLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        if (this.latenciaNumElements == 0) {
            this.latenciaMitjana = novaLatencia;
            this.latenciaNumElements = 1;
        } else {
            long total = ((long) this.latenciaMitjana * (long) this.latenciaNumElements) + (long) novaLatencia;
            long divisor = (long) this.latenciaNumElements + 1L;
            this.latenciaMitjana = Math.toIntExact(total / divisor);
            this.latenciaNumElements++;
        }
    }

    public void updateCountByEstat(SalutEstat estat) {
        this.countUp           = nextCount(this.countUp,           estat == SalutEstat.UP);
        this.countWarn         = nextCount(this.countWarn,         estat == SalutEstat.WARN);
        this.countDegraded     = nextCount(this.countDegraded,     estat == SalutEstat.DEGRADED);
        this.countDown         = nextCount(this.countDown,         estat == SalutEstat.DOWN);
        this.countError        = nextCount(this.countError,        estat == SalutEstat.ERROR);
        this.countMaintenance  = nextCount(this.countMaintenance,  estat == SalutEstat.MAINTENANCE);
        this.countUnknown      = nextCount(this.countUnknown,      estat == SalutEstat.UNKNOWN);
    }

    private Integer nextCount(int currentCount, boolean matches) {
        return currentCount + (matches ? 1 : 0);
    }

    public BigDecimal getPctUp() { return percent(countUp, estatNumElements); }
    public BigDecimal getPctWarn() { return percent(countWarn, estatNumElements); }
    public BigDecimal getPctDegraded() { return percent(countDegraded, estatNumElements); }
    public BigDecimal getPctDown() { return percent(countDown, estatNumElements); }
    public BigDecimal getPctError() { return percent(countError, estatNumElements); }
    public BigDecimal getPctMaintenance() { return percent(countMaintenance, estatNumElements); }
    public BigDecimal getPctUnknown() { return percent(countUnknown, estatNumElements); }

    private BigDecimal percent(int part, int total) {
        if (total <= 0) return null;
        return BigDecimal.valueOf((part * 100.0) / total).setScale(2, RoundingMode.HALF_UP);
    }

	public void addPeticionsOkUltimPeriode(Long numOk) {
		if (numOk == null) return;
		if (this.peticionsOkUltimPeriode == null) this.peticionsOkUltimPeriode = 0L;
		this.peticionsOkUltimPeriode += numOk;
	}

	public void addPeticionsErrorUltimPeriode(Long numError) {
		if (numError == null) return;
		if (this.peticionsErrorUltimPeriode == null) this.peticionsErrorUltimPeriode = 0L;
		this.peticionsErrorUltimPeriode += numError;
	}

    public void addTempsMigUltimPeriode(Integer nouTempsMig, Long peticionsNouPeriode) {
        if (nouTempsMig == null || peticionsNouPeriode == null || peticionsNouPeriode == 0) return;

        long tempsTotal = ((long)this.tempsMigUltimPeriode * this.peticionsOkUltimPeriode) + (nouTempsMig * peticionsNouPeriode);
        long numPeticions = Math.max(this.peticionsOkUltimPeriode + peticionsNouPeriode, 1L);
        this.tempsMigUltimPeriode = Math.toIntExact(tempsTotal / numPeticions);
    }

}
