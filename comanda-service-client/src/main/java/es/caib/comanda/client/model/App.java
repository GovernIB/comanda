package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class App {

	private String codi;
	private String nom;
	private String descripcio;
	private String infoUrl;
	private Integer infoInterval;
	private LocalDateTime infoData;
	private String salutUrl;
	private Integer salutInterval;
	private String versio;
	private boolean activa;

	private Integer integracioCount;
	private Integer subsistemaCount;

	private List<AppIntegracio> integracions;
	private List<AppSubsistema> subsistemes;

}
