package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaTaulaWidget;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Classe que representa un IndicadorTaula.
 *
 * Aquesta classe s'utilitza per establir la relació entre un indicador i un widget estadístic, associant-los a un tipus de
 * columna específic en una taula, i proporcionant un títol opcional.
 *
 * Camps principals:
 * - `indicador`: Referència obligatòria a un objecte de tipus Indicador, que defineix les propietats del mateix.
 * - `widget`: Referència obligatòria a un objecte del tipus EstadisticaTaulaWidget, que conté el widget associat.
 * - `tipus`: Enumeració obligatòria per definir el tipus de columna.
 * - `titol`: Camp de text obligatori que defineix el títol associat (màxim 64 caràcters).
 *
 * Validacions:
 * - `indicador` i `widget` no poden ser nuls.
 * - `titol` no pot superar els 64 caràcters.
 * - `tipus` ha de ser un valor vàlid de l'enumeració TableColumnsEnum.
 *
 * Objectiu:
 * - Proporcionar una representació estructurada d'un indicador associat a una taula estadística, útil en l'anàlisi i gestió
 *   de dades en formats tabulars.
 * - Permetre la configuració de relacions entre indicadors, widgets i configuracions de columna dins un entorn visual
 *   estadístic.
 *
 * Aquesta classe hereta de BaseResource amb un identificador de tipus Long.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        quickFilterFields = { "titol" },
        descriptionField = "titol")
public class IndicadorTaula extends BaseResource<Long> {

    @NotNull
    private ResourceReference<Indicador, Long> indicador;
    @NotNull
    private ResourceReference<EstadisticaTaulaWidget, Long> widget;
    @NotNull
    @JsonSetter(nulls = Nulls.SKIP)
    private TableColumnsEnum agregacio;
    @JsonSetter(nulls = Nulls.SKIP)
    private PeriodeUnitat unitatAgregacio;
    @NotNull
    @Size(max = 64)
    private String titol;

}
