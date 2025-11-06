package es.caib.comanda.configuracio.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Exportacio d'una aplicació
 *
 * @author Límit Tecnologies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppExport implements Serializable {

	private String codi;
	private String nom;
	private String descripcio;
    @Builder.Default
	private boolean activa = true;
	private byte[] logo;
	private List<EntornAppExport> entornApps;

}
