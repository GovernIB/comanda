package es.caib.comanda.configuracio.logic.intf.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Informació de salut retornada per una app.
 *
 * @author Limit Tecnologies
 */
@Getter
@NoArgsConstructor
public class Salut extends BaseResource<Long> {

	@NotNull
	@Size(max = 16)
	private String codi;
	@NotNull
	private LocalDateTime data;
	@NotNull
	@Size(max = 16)
	private String versio;
	@NotNull
	private SalutEstat appEstat;
	private Integer appLatencia;
	@NotNull
	private SalutEstat bdEstat;
	private Integer bdLatencia;

	private String year;
	private String yearMonth;
	private String yearMonthDay;
	private String yearMonthDayHour;

}
