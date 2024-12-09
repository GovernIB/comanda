package es.caib.comanda.configuracio.persist.entity;

import es.caib.comanda.configuracio.logic.intf.config.BaseConfig;
import es.caib.comanda.configuracio.logic.intf.model.Salut;
import es.caib.comanda.configuracio.logic.intf.model.SalutEstat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDateTime;

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

	@Column(name = "codi", length = 16, nullable = false)
	private String codi;
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
