package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Informació de salut retornada per una app.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
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

	private Boolean appUp;
	private Boolean bdUp;

	private Integer integracioUpCount;
	private Integer integracioDownCount;
	private Integer subsistemaUpCount;
	private Integer subsistemaDownCount;
	private Integer missatgeErrorCount;
	private Integer missatgeWarnCount;
	private Integer missatgeInfoCount;

	private String year;
	private String yearMonth;
	private String yearMonthDay;
	private String yearMonthDayHour;

	private List<SalutIntegracio> integracions;
	private List<SalutSubsistema> subsistemes;
	private List<SalutMissatge> missatges;
	private List<SalutDetall> detalls;

}
