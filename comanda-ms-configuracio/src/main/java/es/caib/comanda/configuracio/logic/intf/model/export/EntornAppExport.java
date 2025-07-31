package es.caib.comanda.configuracio.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Exportacio d'un Entorn-Aplicació.
 *
 * @author Límit Tecnologies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntornAppExport implements Serializable {

	private String entornCodi;
	private String entornNom;

	// Informació de l'aplicació en l'entorn
	private String infoUrl;
	private Integer infoInterval;
	private boolean activa;

	// Informació de salut
	private String salutUrl;
	private Integer salutInterval;

	// Informació d'estadístiques
	private String estadisticaInfoUrl;
	private String estadisticaUrl;
	private String estadisticaCron;

}
