package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitolTipus;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.PosicioSubtitol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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

    @NotBlank
    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.TITOL_MAX_LENGTH)
    private String titol;

    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.SUBTITOL_MAX_LENGTH)
    private String subtitol;

    private int posX;
    private int posY;

    @Min(1)
    private int width;

    @Min(1)
    private int height;

    private DashboardTitolTipus tipusTitol;

    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.COLOR_MAX_LENGTH)
    private String colorTitol;

    private Integer midaFontTitol;

    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.COLOR_MAX_LENGTH)
    private String colorSubtitol;

    private Integer midaFontSubtitol;
    private PosicioSubtitol posicioSubtitol;
    private Integer separacioSubtitol;

    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.COLOR_MAX_LENGTH)
    private String colorFons;

    private Boolean mostrarVoraTop;
    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.COLOR_MAX_LENGTH)
    private String colorVoraTop;
    private Integer ampleVoraTop;

    private Boolean mostrarVoraRight;
    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.COLOR_MAX_LENGTH)
    private String colorVoraRight;
    private Integer ampleVoraRight;

    private Boolean mostrarVoraBottom;
    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.COLOR_MAX_LENGTH)
    private String colorVoraBottom;
    private Integer ampleVoraBottom;

    private Boolean mostrarVoraLeft;
    @Size(max = es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity.COLOR_MAX_LENGTH)
    private String colorVoraLeft;
    private Integer ampleVoraLeft;

    private Boolean destacat;
    private Boolean personalitzat;

    @Valid
    private PlantillaExport plantilla;

}
