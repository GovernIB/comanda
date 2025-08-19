package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.persist.entity.BaseEntity;
import es.caib.comanda.salut.logic.intf.model.Salut;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
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

}
