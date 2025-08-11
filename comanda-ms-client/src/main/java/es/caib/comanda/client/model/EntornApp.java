package es.caib.comanda.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EntornApp implements Serializable {

	private Long id;
	private EntornRef entorn;
	private AppRef app;

	private String infoUrl;
	private Integer infoInterval;
	private LocalDateTime infoData;
	private String versio;
	private boolean activa;

	private String salutUrl;
	private Integer salutInterval;

	private Integer integracioCount;
	private Integer subsistemaCount;

	private List<AppIntegracio> integracions;
	private List<AppSubsistema> subsistemes;
	private List<AppContext> contexts;

	private String estadisticaInfoUrl;
	private String estadisticaUrl;
	private String estadisticaCron;

}
