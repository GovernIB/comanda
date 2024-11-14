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
public class VersioDto {

	private Long id = null;
	private String versioCodi = null;
	private Date versionData;

}
