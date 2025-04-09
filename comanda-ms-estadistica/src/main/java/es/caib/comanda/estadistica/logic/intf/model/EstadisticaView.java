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
public class EstadisticaView extends BaseResource<Long> {

    @NotNull
    @Size(max = 64)
    private String titol;
    @Size(max = 1024)
    private String descripcio;
    // Text a mostrar després del valor. Ex 20 "dies"
    @Size(max = 64)
    private String unitat;

    @NotNull
    @Size(max = 16)
    private String aplicacioCodi;
    @NotNull
    private ResourceReference<Indicador, Long> indicador;

    // Tipus de vista a generar
    @NotNull
    private TipusVistaEnum tipusVista;
    // Dimensions per les que filtrar
    @NotEmpty
    private List<DimensioValor> dimensionsValorFiltre;

    // Periode de temps que es vol mostrar
    private UnitatTempsEnum periodeIniciUnitat;
    // Número de unitats a retrocedir per iniciar el període de la vista
    private Integer periodeInici;
    private UnitatTempsEnum periodeUnitat;
    private Integer periode;

    // En cas de gràfic, quina agrupació de temps utilitzar
    private UnitatTempsEnum tempsAgrupacio;

    // Camps en cas de vist tipus TABLE
    private List<TableColumnsEnum> taulaColumnes;
    private ResourceReference<Dimensio, Long> taulaDimensio;


}
