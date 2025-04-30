package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EstadisticaTaulaWidget extends BaseResource<Long> {

    @NotNull
    @Size(max = 64)
    private String titol;
    @Size(max = 1024)
    private String descripcio;

    @NotNull
    @Size(max = 16)
    private String aplicacioCodi;

    // Dimensions per les que filtrar
    @NotEmpty
    private List<DimensioValor> dimensionsValor;

    // Periode de temps que es vol mostrar
    private UnitatTempsEnum iniciUnitat;
    // Número de unitats a retrocedir per iniciar el període de la vista
    private Integer inici;
    private UnitatTempsEnum duracioUnitat;
    private Integer duracio;

    // Camps en cas de vist tipus TABLE
    private List<TableColumnsEnum> columnes;

    @NotNull
    private ResourceReference<Indicador, Long> dimensioAgrupacio;
    private String titolAgrupament;

}
