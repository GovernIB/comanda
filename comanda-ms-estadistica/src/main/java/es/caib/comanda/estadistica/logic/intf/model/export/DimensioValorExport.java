package es.caib.comanda.estadistica.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Classe per exportar un valor associat a una dimensió.
 *
 *  @author Límit Tecnologies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimensioValorExport implements Serializable {

    private String dimensioCodi;
    private String valor;

}
