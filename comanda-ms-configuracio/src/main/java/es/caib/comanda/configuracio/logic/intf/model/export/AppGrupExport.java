package es.caib.comanda.configuracio.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Exportació d'un grup d'aplicacions
 *
 * @author Límit Tecnologies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppGrupExport implements Serializable {

	private String nom;
	private String colorClar;
	private String colorFosc;

}
