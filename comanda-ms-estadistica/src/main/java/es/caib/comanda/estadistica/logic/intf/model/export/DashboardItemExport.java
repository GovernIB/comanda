package es.caib.comanda.estadistica.logic.intf.model.export;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Classe per exportar un element dins d'un quadre de comandament (Dashboard).
 *
 *  @author Límit Tecnologies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardItemExport implements Serializable {

    @NotBlank
    private String appCodi;

    @NotBlank
    private String entornCodi;

    @NotNull
    @Valid
    private EstadisticaWidgetExport widget;

    private int posX;
    private int posY;

    @Min(1)
    private int width;

    @Min(1)
    private int height;

    private Boolean destacat;
    private Boolean personalitzat;

    @Valid
    private PlantillaExport plantilla;

    @Valid
    private AtributsVisuals atributsVisuals;

}
