package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeBaseResource;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Classe que representa un widget gràfic estadístic dins del sistema.
 *
 * La classe EstadisticaGraficWidget permet definir les propietats necessàries per a generar i configurar widgets
 * gràfics basats en estadístiques i indicadors. Aquest widget inclou títol, descripció, codis d'aplicació i
 * indicadors associats, així com configuracions relacionades amb períodes de temps, tipus de gràfic, agrupacions i
 * llegendes.
 *
 * Extèn la classe BaseResource, proporcionant un identificador únic del tipus Long.
 *
 * Propietats:
 * - titol: Títol del widget.
 * - descripcio: Descripció del widget.
 * - aplicacioCodi: Codi de l'aplicació associada.
 * - dimensionsValor: Llista de dimensions i els seus valors associats per filtrar.
 * - iniciUnitat: Unitat temporal per iniciar el període (ex. MES, SETMANA).
 * - inici: Nombre d'unitats temporals a retrocedir per establir el començament del període.
 * - duracioUnitat: Unitat temporal per definir la duració del període.
 * - duracio: Duració del període en unitats.
 * - indicador: Referència cap a un indicador estadístic (instància d'Indicador).
 * - tipusGrafic: Tipus de gràfic a generar (ex. BAR_CHART, PIE_CHART).
 * - tipusValors: Tipus de valors a mostrar al gràfic (ex. NUMERIC, PERCENTAGE).
 * - tempsAgrupacio: Unitat temporal per agrupar les dades en el gràfic.
 * - llegendaX: Text de la llegenda de l'eix X del gràfic.
 * - llegendaY: Text de la llegenda de l'eix Y del gràfic.
 *
 * Validacions:
 * - titol: Obligatori, amb un màxim de 64 caràcters.
 * - descripcio: Opcional, amb un màxim de 1024 caràcters.
 * - aplicacioCodi: Obligatori, amb un màxim de 16 caràcters.
 * - dimensionsValor: No pot estar buit.
 * - inici, duracio: Opcional, però si estan presents, han d'estar en sintonia amb les unitats corresponents.
 * - indicador: Obligatori i ha de referenciar un objecte del tipus Indicador.
 * - tipusGrafic: Obligatori.
 * - tipusValors: Obligatori.
 * - tempsAgrupacio: Obligatori.
 *
 * Ús:
 * Aquesta classe és útil per generar widgets gràfics personalitzats dins d'un sistema d'anàlisi i visualització
 * d'estadístiques. Les configuracions que proporciona asseguren la personalització del comportament i l'aparença
 * dels widgets gràfics, segons els requisits de l'usuari o l'aplicació.
 *
 * Relacions:
 * - Cada EstadisticaGraficWidget està associat a una o més dimensions i valors (DimensioValor).
 * - Utilitza unitats temporals (UnitatTempsEnum) i tipus de gràfics (TipusGraficEnum) per personalitzar la vista.
 * - Conté una referència a un Indicador específic com a font de dades.
 *
 * Aquesta estructura promou una gestió i configuració eficient de visualitzacions gràfiques dins del sistema.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
public class EstadisticaGraficWidget extends PeriodeBaseResource<Long> {

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

    @NotNull
    private ResourceReference<Indicador, Long> indicador;

    // Tipus de vista a generar
    @NotNull
    private TipusGraficEnum tipusGrafic;
    @NotNull
    private GraficValueTypeEnum tipusValors;

    // En cas de gràfic, quina agrupació de temps utilitzar
    @NotNull
    private PeriodeUnitat tempsAgrupacio;

    private String llegendaX;
    private String llegendaY;

}
