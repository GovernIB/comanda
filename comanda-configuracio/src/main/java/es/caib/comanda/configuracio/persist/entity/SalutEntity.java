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
	@Column(name = "bd_estat", nullable = false)
	private SalutEstat bdEstat;
	@Column(name = "bd_latencia")
	private Integer bdLatencia;

	@Formula("TO_CHAR(EXTRACT(YEAR FROM data))")
	private String year;
	@Formula("CONCAT(TO_CHAR(EXTRACT(YEAR FROM data)), LPAD(TO_CHAR(EXTRACT(MONTH FROM data)), 2, '0'))")
	private String yearMonth;
	@Formula("CONCAT(TO_CHAR(EXTRACT(YEAR FROM data)), LPAD(TO_CHAR(EXTRACT(MONTH FROM data)), 2, '0'), LPAD(TO_CHAR(EXTRACT(DAY_OF_MONTH FROM data)), 2, '0'))")
	private String yearMonthDay;
	@Formula("CONCAT(TO_CHAR(EXTRACT(YEAR FROM data)), LPAD(TO_CHAR(EXTRACT(MONTH FROM data)), 2, '0'), LPAD(TO_CHAR(EXTRACT(DAY_OF_MONTH FROM data)), 2, '0'), LPAD(TO_CHAR(EXTRACT(HOUR FROM data)), 2, '0'))")
	private String yearMonthDayHour;

}
