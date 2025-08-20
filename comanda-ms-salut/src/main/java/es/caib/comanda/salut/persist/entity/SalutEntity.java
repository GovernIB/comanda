package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.salut.logic.intf.model.Salut;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entitat de base de dades que emmagatzema les informacions de salut
 * retornades per les apps.
 *
 * @author Límit Tecnologies
 */
@Entity
@Table(name = BaseConfig.DB_PREFIX + "salut")
@Getter
@Setter
@NoArgsConstructor
public class SalutEntity extends BaseEntity<Salut> {

	@Column(name = "entorn_app_id", nullable = false)
	private Long entornAppId;
    @Column(name = "data", nullable = false)
    private LocalDateTime data;
	@Column(name = "data_app", nullable = false)
	private LocalDateTime dataApp;
	@Enumerated(EnumType.STRING)
	@Column(name = "app_estat", nullable = false)
	private SalutEstat appEstat;
	@Column(name = "app_latencia")
	private Integer appLatencia;
	@Enumerated(EnumType.STRING)
	@Column(name = "bd_estat")
	private SalutEstat bdEstat;
	@Column(name = "bd_latencia")
	private Integer bdLatencia;

	// Camps d'agregació (percentatges i mitjanes)
	@Setter(AccessLevel.NONE)
    @Column(name = "app_pct_up", precision = 5, scale = 2)
	private java.math.BigDecimal appPctUp;
	@Setter(AccessLevel.NONE)
    @Column(name = "app_pct_warn", precision = 5, scale = 2)
	private java.math.BigDecimal appPctWarn;
	@Setter(AccessLevel.NONE)
    @Column(name = "app_pct_degraded", precision = 5, scale = 2)
	private java.math.BigDecimal appPctDegraded;
	@Setter(AccessLevel.NONE)
    @Column(name = "app_pct_down", precision = 5, scale = 2)
	private java.math.BigDecimal appPctDown;
	@Setter(AccessLevel.NONE)
    @Column(name = "app_pct_maintenance", precision = 5, scale = 2)
	private java.math.BigDecimal appPctMaintenance;
	@Setter(AccessLevel.NONE)
    @Column(name = "app_pct_unknown", precision = 5, scale = 2)
	private java.math.BigDecimal appPctUnknown;
	@Setter(AccessLevel.NONE)
    @Column(name = "bd_pct_up", precision = 5, scale = 2)
	private java.math.BigDecimal bdPctUp;
	@Setter(AccessLevel.NONE)
    @Column(name = "bd_pct_warn", precision = 5, scale = 2)
	private java.math.BigDecimal bdPctWarn;
	@Setter(AccessLevel.NONE)
    @Column(name = "bd_pct_degraded", precision = 5, scale = 2)
	private java.math.BigDecimal bdPctDegraded;
	@Setter(AccessLevel.NONE)
    @Column(name = "bd_pct_down", precision = 5, scale = 2)
	private java.math.BigDecimal bdPctDown;
	@Setter(AccessLevel.NONE)
    @Column(name = "bd_pct_maintenance", precision = 5, scale = 2)
	private java.math.BigDecimal bdPctMaintenance;
	@Setter(AccessLevel.NONE)
    @Column(name = "bd_pct_unknown", precision = 5, scale = 2)
	private java.math.BigDecimal bdPctUnknown;
	@Setter(AccessLevel.NONE)
    @Column(name = "app_latencia_mitjana")
	private Integer appLatenciaMitjana;
    @Setter(AccessLevel.NONE)
    @Column(name = "app_latencia_num_elements")
    private Integer appLatenciaNumElements;
	@Setter(AccessLevel.NONE)
    @Column(name = "bd_latencia_mitjana")
	private Integer bdLatenciaMitjana;
    @Setter(AccessLevel.NONE)
    @Column(name = "bd_latencia_num_elements")
    private Integer bdLatenciaNumElements;

    @Column(name = "num_elements")
    private Integer numElements;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipus_registre", nullable = false)
	private TipusRegistreSalut tipusRegistre = TipusRegistreSalut.MINUT;

    @OneToMany(mappedBy="salut", cascade = CascadeType.ALL)
    private Set<SalutIntegracioEntity> salutIntegracions;
    @OneToMany(mappedBy="salut", cascade = CascadeType.ALL)
    private Set<SalutSubsistemaEntity> salutSubsistemes;
    @OneToMany(mappedBy="salut", cascade = CascadeType.ALL)
    private Set<SalutMissatgeEntity> salutMissatges;
    @OneToMany(mappedBy="salut", cascade = CascadeType.ALL)
    private Set<SalutDetallEntity> salutDetalls;

    @Formula("(CASE WHEN (app_estat = 'UP') THEN 1 ELSE 0 END)")
    private Boolean appUp;
    @Formula("(CASE WHEN (bd_estat = 'UP') THEN 1 ELSE 0 END)")
    private Boolean bdUp;

	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_integracio sint where sint.salut_id = id and sint.estat = 'UP')")
	private Integer integracioUpCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_integracio sint where sint.salut_id = id and sint.estat = 'DOWN')")
	private Integer integracioDownCount;
    @Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_integracio sint where sint.salut_id = id and sint.estat = 'UNKNOWN')")
    private Integer integracioDesconegutCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_subsistema ssub where ssub.salut_id = id and ssub.estat = 'UP')")
	private Integer subsistemaUpCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_subsistema ssub where ssub.salut_id = id and ssub.estat = 'DOWN')")
	private Integer subsistemaDownCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_missatge smsg where smsg.salut_id = id and smsg.nivell = 'ERROR')")
	private Integer missatgeErrorCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_missatge smsg where smsg.salut_id = id and smsg.nivell = 'WARN')")
	private Integer missatgeWarnCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_missatge smsg where smsg.salut_id = id and smsg.nivell = 'INFO')")
	private Integer missatgeInfoCount;

    @Formula("TO_CHAR(data, 'YYYY')")
    private String year;
    @Formula("TO_CHAR(data, 'YYYYMM')")
    private String yearMonth;
    @Formula("TO_CHAR(data, 'YYYYMMDD')")
    private String yearMonthDay;
    @Formula("TO_CHAR(data, 'YYYYMMDDHH24')")
    private String yearMonthDayHour;
    @Formula("TO_CHAR(data, 'YYYYMMDDHH24MI')")
    private String yearMonthDayHourMinute;


    // Mètodes per actualitzar percentatges d'estats i latències mitjanes
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setAppLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        this.appLatenciaMitjana = novaLatencia;
        this.appLatenciaNumElements = 1;
    }

    public void addAppLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        if (this.appLatenciaNumElements == null || this.appLatenciaNumElements == 0) {
            this.appLatenciaMitjana = novaLatencia;
            this.appLatenciaNumElements = 1;
        } else {
            long total = ((long) this.appLatenciaMitjana * (long) this.appLatenciaNumElements) + (long) novaLatencia;
            long divisor = (long) this.appLatenciaNumElements + 1L;
            this.appLatenciaMitjana = Math.toIntExact(total / divisor);
            this.appLatenciaNumElements++;
        }
    }

    public void setBdLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        this.bdLatenciaMitjana = novaLatencia;
        this.bdLatenciaNumElements = 1;
    }

    public void addBdLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        if (this.bdLatenciaNumElements == null || this.bdLatenciaNumElements == 0) {
            this.bdLatenciaMitjana = novaLatencia;
            this.bdLatenciaNumElements = 1;
        } else {
            long total = ((long) this.bdLatenciaMitjana * (long) this.bdLatenciaNumElements) + (long) novaLatencia;
            long divisor = (long) this.bdLatenciaNumElements + 1L;
            this.bdLatenciaMitjana = Math.toIntExact(total / divisor);
            this.bdLatenciaNumElements++;
        }
    }

    public void updateAppPctByEstat(SalutEstat estat) {
        if (numElements == null) this.numElements = 0;
        int newTotal = numElements + 1;

        int currentUpElements = this.appPctUp == null ? 0 : getElementsByPercentage(this.appPctUp);
        int currentWarnElements = this.appPctWarn == null ? 0 : getElementsByPercentage(this.appPctWarn);
        int currentDegradedElements = this.appPctDegraded == null ? 0 : getElementsByPercentage(this.appPctDegraded);
        int currentDownElements = this.appPctDown == null ? 0 : getElementsByPercentage(this.appPctDown);
        int currentMaintenanceElements = this.appPctMaintenance == null ? 0 : getElementsByPercentage(this.appPctMaintenance);
        int currentUnknownElements = this.appPctUnknown == null ? 0 : getElementsByPercentage(this.appPctUnknown);

        int newUpElements = currentUpElements + (SalutEstat.UP.equals(estat) ? 1 : 0);
        int newWarnElements = currentWarnElements + (SalutEstat.WARN.equals(estat) ? 1 : 0);
        int newDegradedElements = currentDegradedElements + (SalutEstat.DEGRADED.equals(estat) ? 1 : 0);
        int newDownElements = currentDownElements + (SalutEstat.DOWN.equals(estat) || SalutEstat.ERROR.equals(estat) ? 1 : 0);
        int newMaintenanceElements = currentMaintenanceElements + (SalutEstat.MAINTENANCE.equals(estat) ? 1 : 0);
        int newUnknownElements = currentUnknownElements + (SalutEstat.UNKNOWN.equals(estat) ? 1 : 0);

        this.appPctUp = percent(newUpElements, newTotal);
        this.appPctWarn = percent(newWarnElements, newTotal);
        this.appPctDegraded = percent(newDegradedElements, newTotal);
        this.appPctDown = percent(newDownElements, newTotal);
        this.appPctMaintenance = percent(newMaintenanceElements, newTotal);
        this.appPctUnknown = percent(newUnknownElements, newTotal);
    }

    public void updateBdPctByEstat(SalutEstat estat) {
        if (numElements == null) this.numElements = 0;
        int newTotal = numElements + 1;

        int currentUpElements = this.bdPctUp == null ? 0 : getElementsByPercentage(this.bdPctUp);
        int currentWarnElements = this.bdPctWarn == null ? 0 : getElementsByPercentage(this.bdPctWarn);
        int currentDegradedElements = this.bdPctDegraded == null ? 0 : getElementsByPercentage(this.bdPctDegraded);
        int currentDownElements = this.bdPctDown == null ? 0 : getElementsByPercentage(this.bdPctDown);
        int currentMaintenanceElements = this.bdPctMaintenance == null ? 0 : getElementsByPercentage(this.bdPctMaintenance);
        int currentUnknownElements = this.bdPctUnknown == null ? 0 : getElementsByPercentage(this.bdPctUnknown);

        int newUpElements = currentUpElements + (SalutEstat.UP.equals(estat) ? 1 : 0);
        int newWarnElements = currentWarnElements + (SalutEstat.WARN.equals(estat) ? 1 : 0);
        int newDegradedElements = currentDegradedElements + (SalutEstat.DEGRADED.equals(estat) ? 1 : 0);
        int newDownElements = currentDownElements + (SalutEstat.DOWN.equals(estat) || SalutEstat.ERROR.equals(estat) ? 1 : 0);
        int newMaintenanceElements = currentMaintenanceElements + (SalutEstat.MAINTENANCE.equals(estat) ? 1 : 0);
        int newUnknownElements = currentUnknownElements + (SalutEstat.UNKNOWN.equals(estat) ? 1 : 0);

        this.bdPctUp = percent(newUpElements, newTotal);
        this.bdPctWarn = percent(newWarnElements, newTotal);
        this.bdPctDegraded = percent(newDegradedElements, newTotal);
        this.bdPctDown = percent(newDownElements, newTotal);
        this.bdPctMaintenance = percent(newMaintenanceElements, newTotal);
        this.bdPctUnknown = percent(newUnknownElements, newTotal);
    }

    private int getElementsByPercentage(BigDecimal percentatge) {
        return Math.round(percentatge.multiply(BigDecimal.valueOf(numElements))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .floatValue());
    }

    private BigDecimal percent(int part, int total) {
        if (total <= 0) return null;
        return BigDecimal.valueOf((part * 100.0) / total)
                .setScale(2, RoundingMode.HALF_UP);
    }

}
