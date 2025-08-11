package es.caib.comanda.configuracio.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Exportacio d'un entorn.
 *
 * @author Límit Tecnologies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntornExport implements Serializable {

    private String codi;
    private String nom;

}
