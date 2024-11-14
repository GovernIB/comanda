package es.caib.comanda.client.configuracio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConfiguracioDto {

	private Long id = null;
	private String codi = null;
	private String nom = null;
	private String urlApp = null;
	private String urlSalut = null;
	private String urlInfo = null;
	private String observacions = null;
	private String color = null;
	private VersioDto versioActual = null;
	private List<VersioDto> versions;
	private List<IntegracioDto> integracions;
	private List<SubsistemaDto> subsistemes;

}
