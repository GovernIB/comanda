package es.caib.comanda.estadistica.logic.intf.model.paleta;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTemplatePaletteGroup extends BaseResource<Long> {

    @NotNull
    private PaletteGroupType groupType;
    private ResourceReference<Paleta, Long> widgetPalette;
    private ResourceReference<Paleta, Long> chartPalette;
    @Size(max = 64)
    private String widgetPaletteClientId;
    @Size(max = 64)
    private String chartPaletteClientId;
    private Integer ordre;
}
