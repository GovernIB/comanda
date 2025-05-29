package es.caib.comanda.estadistica.logic.intf.model.widget;

import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Classe que representa un widget simple d'estadística.
 *
 * Aquesta classe estesa de BaseResource ofereix una manera de modelar widgets estadístics senzills,
 * incloent validacions i relacions amb altres entitats del sistema. El widget es basa en un indicador
 * determinat i permet filtrar dades segons dimensions i un període de temps especificat.
 *
 * Propietats:
 * - `titol`: Títol del widget. És un camp obligatori amb una longitud màxima de 64 caràcters.
 * - `descripcio`: Descripció del widget amb una longitud màxima de 1024 caràcters.
 * - `aplicacioCodi`: Codi que identifica l'aplicació associada. És un camp obligatori amb una longitud màxima de 16 caràcters.
 * - `dimensionsValor`: Llista de valors de dimensió filtrats. És un camp obligatori, no pot estar buit.
 * - `iniciUnitat`: Unitat temporal que defineix el punt d'inici del període (ex. DIA, SETMANA, MES).
 * - `inici`: Nombre d'unitats a retrocedir des de la data actual per establir l'inici del període.
 * - `duracioUnitat`: Unitat temporal que defineix la duració del període (ex. DIA, MES).
 * - `duracio`: Duració del període segons la unitat especificada.
 * - `unitat`: Text addicional per descriure el valor del widget (ex. "dies"). Longitud màxima de 64 caràcters.
 * - `indicador`: Indicador associat al widget. És un camp obligatori que enllaça amb una altra entitat.
 *
 * Validacions:
 * - `titol` no pot ser nul i ha de complir la longitud màxima especificada.
 * - `aplicacioCodi` no pot ser nul i ha de complir la longitud màxima especificada.
 * - `dimensionsValor` no pot estar buit i ha d'incloure almenys una dimensió.
 * - `indicador` no pot ser nul i ha d'enllaçar amb una entitat Indicador vàlida.
 *
 * Ús:
 * Aquesta classe s'utilitza principalment per definir i gestionar la configuració dels widgets simples d'estadística
 * dins de comandes o estadístiques complexes.
 *
 * Relacions:
 * - Està associada a una entitat Indicador a través del camp `indicador`.
 * - Utilitza DimensioValor per especificar les dimensions filtrades.
 * - Es pot parametritzar segons intervals de temps mitjançant les unitats i valors relacionats amb temps.
 *
 * Permet una gestió organitzada i validacions adequades per a l'estructura de dades dels widgets estadístics.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        quickFilterFields = { "titol", "descripcio" },
        descriptionField = "titol"
)
public class EstadisticaSimpleWidget extends EstadisticaWidget { // WidgetBaseResource<Long> {

    @NotNull
    private ResourceReference<IndicadorTaula, Long> indicador;

    // Text a mostrar després del valor. Ex 20 "dies"
    @Size(max = 64)
    private String unitat;

    private boolean compararPeriodeAnterior;

    // Configuracions visuals
    private AtributsVisualsSimple atributsVisuals;
}
