package es.caib.comanda.estadistica.logic.intf.model.export;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Classe per exportar un títol dins d'un quadre de comandament (Dashboard).
 *
 * @author Límit Tecnologies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTitolExport implements Serializable {

    private String titol;
    private String subtitol;
    private int posX;
    private int posY;
    private int width;
    private int height;
    private String colorTitol;
    private Integer midaFontTitol;
    private String colorSubtitol;
    private Integer midaFontSubtitol;
    private String colorFons;
    private Boolean mostrarVora;
    private String colorVora;
    private Integer ampleVora;

}
