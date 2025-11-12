package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.salut.logic.intf.model.Salut;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
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

import static lombok.AccessLevel.NONE;

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

	@Column(name = "entorn_app_id", nullable = false)	        private Long entornAppId;
    @Column(name = "data", nullable = false)                    private LocalDateTime data;
	@Column(name = "data_app", nullable = false)            	private LocalDateTime dataApp;
	@Enumerated(EnumType.STRING)
	@Column(name = "app_estat", nullable = false)           	private SalutEstat appEstat;
	@Column(name = "app_latencia")                          	private Integer appLatencia;
	@Enumerated(EnumType.STRING)
	@Column(name = "bd_estat")                              	private SalutEstat bdEstat;
	@Column(name = "bd_latencia")                              	private Integer bdLatencia;
	@Column(name = "peticio_error")                             private boolean peticioError;

	// Camps d'agregació (comptadors i mitjanes)
    @Setter(NONE) @Column(name = "app_count_up")                private int appCountUp = 0;
    @Setter(NONE) @Column(name = "app_count_warn")              private int appCountWarn = 0;
    @Setter(NONE) @Column(name = "app_count_degraded")          private int appCountDegraded = 0;
    @Setter(NONE) @Column(name = "app_count_down")              private int appCountDown = 0;
    @Setter(NONE) @Column(name = "app_count_error")             private int appCountError = 0;
    @Setter(NONE) @Column(name = "app_count_maintenance")       private int appCountMaintenance = 0;
    @Setter(NONE) @Column(name = "app_count_unknown")           private int appCountUnknown = 0;

    @Setter(NONE) @Column(name = "bd_count_up")                 private int bdCountUp = 0;
    @Setter(NONE) @Column(name = "bd_count_warn")               private int bdCountWarn = 0;
    @Setter(NONE) @Column(name = "bd_count_degraded")           private int bdCountDegraded = 0;
    @Setter(NONE) @Column(name = "bd_count_down")               private int bdCountDown = 0;
    @Setter(NONE) @Column(name = "bd_count_error")              private int bdCountError = 0;
    @Setter(NONE) @Column(name = "bd_count_maintenance")        private int bdCountMaintenance = 0;
    @Setter(NONE) @Column(name = "bd_count_unknown")            private int bdCountUnknown = 0;

	@Setter(NONE) @Column(name = "app_latencia_mitjana")        private Integer appLatenciaMitjana;
    @Setter(NONE) @Column(name = "app_latencia_num_elements")   private int appLatenciaNumElements = 0;
	@Setter(NONE) @Column(name = "bd_latencia_mitjana")         private Integer bdLatenciaMitjana;
    @Setter(NONE) @Column(name = "bd_latencia_num_elements")    private int bdLatenciaNumElements = 0;

    @Column(name = "num_elements")                              private int numElements = 0;
	@Enumerated(EnumType.STRING)
	@Column(name = "tipus_registre", nullable = false)      	private TipusRegistreSalut tipusRegistre = TipusRegistreSalut.MINUT;

    @OneToMany(mappedBy="salut", cascade = CascadeType.ALL)     private Set<SalutIntegracioEntity> salutIntegracions;
    @OneToMany(mappedBy="salut", cascade = CascadeType.ALL)     private Set<SalutSubsistemaEntity> salutSubsistemes;
    @OneToMany(mappedBy="salut", cascade = CascadeType.ALL)     private Set<SalutMissatgeEntity> salutMissatges;
    @OneToMany(mappedBy="salut", cascade = CascadeType.ALL)     private Set<SalutDetallEntity> salutDetalls;

    @Formula("(CASE WHEN (app_estat = 'UP') THEN 1 ELSE 0 END)")
    private Boolean appUp;
    @Formula("(CASE WHEN (bd_estat = 'UP') THEN 1 ELSE 0 END)")
    private Boolean bdUp;

	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_integracio sint where sint.salut_id = id and sint.estat = 'UP')")
	private Integer integracioUpCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_integracio sint where sint.salut_id = id and sint.estat IN ('DOWN', 'ERROR'))")
	private Integer integracioDownCount;
    @Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_integracio sint where sint.salut_id = id and sint.estat IN ('WARN', 'DEGRADED'))")
    private Integer integracioWarnCount;
    @Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_integracio sint where sint.salut_id = id and sint.estat = 'UNKNOWN')")
    private Integer integracioDesconegutCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_subsistema ssub where ssub.salut_id = id and ssub.estat = 'UP')")
	private Integer subsistemaUpCount;
	@Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_subsistema ssub where ssub.salut_id = id and ssub.estat IN ('DOWN', 'ERROR'))")
	private Integer subsistemaDownCount;
    @Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_subsistema ssub where ssub.salut_id = id and ssub.estat IN ('WARN', 'DEGRADED'))")
    private Integer subsistemaWarnCount;
    @Formula("(select count(*) from " + BaseConfig.DB_PREFIX + "salut_subsistema ssub where ssub.salut_id = id and ssub.estat = 'UNKNOWN')")
    private Integer subsistemaDesconegutCount;
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


    // Mètodes per actualitzar comptadors, percentatges d'estats i latències mitjanes
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setAppLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        this.appLatenciaMitjana = novaLatencia;
        this.appLatenciaNumElements = 1;
    }

    public void addAppLatenciaMitjana(Integer novaLatencia) {
        if (novaLatencia == null) return;

        if (this.appLatenciaNumElements == 0) {
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

        if (this.bdLatenciaNumElements == 0) {
            this.bdLatenciaMitjana = novaLatencia;
            this.bdLatenciaNumElements = 1;
        } else {
            long total = ((long) this.bdLatenciaMitjana * (long) this.bdLatenciaNumElements) + (long) novaLatencia;
            long divisor = (long) this.bdLatenciaNumElements + 1L;
            this.bdLatenciaMitjana = Math.toIntExact(total / divisor);
            this.bdLatenciaNumElements++;
        }
    }

    public void updateAppCountByEstat(SalutEstat estat) {
        this.appCountUp          = nextCount(this.appCountUp,          estat == SalutEstat.UP);
        this.appCountWarn        = nextCount(this.appCountWarn,        estat == SalutEstat.WARN);
        this.appCountDegraded    = nextCount(this.appCountDegraded,    estat == SalutEstat.DEGRADED);
        this.appCountDown        = nextCount(this.appCountDown,        estat == SalutEstat.DOWN);
        this.appCountError       = nextCount(this.appCountError,       estat == SalutEstat.ERROR);
        this.appCountMaintenance = nextCount(this.appCountMaintenance, estat == SalutEstat.MAINTENANCE);
        this.appCountUnknown     = nextCount(this.appCountUnknown,     estat == SalutEstat.UNKNOWN);
    }

    public void updateBdCountByEstat(SalutEstat estat) {
        this.bdCountUp           = nextCount(this.bdCountUp,           estat == SalutEstat.UP);
        this.bdCountWarn         = nextCount(this.bdCountWarn,         estat == SalutEstat.WARN);
        this.bdCountDegraded     = nextCount(this.bdCountDegraded,     estat == SalutEstat.DEGRADED);
        this.bdCountDown         = nextCount(this.bdCountDown,         estat == SalutEstat.DOWN);
        this.bdCountError        = nextCount(this.bdCountError,        estat == SalutEstat.ERROR);
        this.bdCountMaintenance  = nextCount(this.bdCountMaintenance,  estat == SalutEstat.MAINTENANCE);
        this.bdCountUnknown      = nextCount(this.bdCountUnknown,      estat == SalutEstat.UNKNOWN);
    }

    private Integer nextCount(int currentCount, boolean matches) {
        return currentCount + (matches ? 1 : 0);
    }

    public BigDecimal getAppPctUp() { return percent(appCountUp, numElements); }
    public BigDecimal getAppPctWarn() { return percent(appCountWarn, numElements); }
    public BigDecimal getAppPctDegraded() { return percent(appCountDegraded, numElements); }
    public BigDecimal getAppPctDown() { return percent(appCountDown, numElements); }
    public BigDecimal getAppPctError() { return percent(appCountError, numElements); }
    public BigDecimal getAppPctMaintenance() { return percent(appCountMaintenance, numElements); }
    public BigDecimal getAppPctUnknown() { return percent(appCountUnknown, numElements); }

    public BigDecimal getBdPctUp() { return percent(bdCountUp, numElements); }
    public BigDecimal getBdPctWarn() { return percent(bdCountWarn, numElements); }
    public BigDecimal getBdPctDegraded() { return percent(bdCountDegraded, numElements); }
    public BigDecimal getBdPctDown() { return percent(bdCountDown, numElements); }
    public BigDecimal getBdPctError() { return percent(bdCountError, numElements); }
    public BigDecimal getBdPctMaintenance() { return percent(bdCountMaintenance, numElements); }
    public BigDecimal getBdPctUnknown() { return percent(bdCountUnknown, numElements); }

    private BigDecimal percent(int part, int total) {
        if (total <= 0) return null;
        return BigDecimal.valueOf((part * 100.0) / total).setScale(2, RoundingMode.HALF_UP);
    }

}
