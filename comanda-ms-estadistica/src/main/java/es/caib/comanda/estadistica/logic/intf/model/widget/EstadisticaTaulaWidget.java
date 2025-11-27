package es.caib.comanda.estadistica.logic.intf.model.widget;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.Nulls;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.back.intf.validation.ValidTaulaWidget;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Classe que representa un widget d'estadística tipus taula.
 *
 * Aquesta classe permet gestionar la configuració d'un widget d'estadística que mostra dades en format de taula.
 * Conté informació sobre el títol, la descripció, càlculs d'agrupació, dimensions de filtratge, períodes de temps,
 * columnes a mostrar i altres criteris específics.
 *
 * Propietats:
 * - `titol`: El títol del widget. És obligatori i té una longitud màxima de 64 caràcters.
 * - `descripcio`: La descripció del widget. És opcional i té una longitud màxima de 1024 caràcters.
 * - `aplicacioCodi`: El codi de l'aplicació associada al widget. És obligatori i té una longitud màxima de 16 caràcters.
 * - `dimensionsValor`: Llista de dimensions i els seus valors associats per filtrar les dades. És obligatori i no pot ser buit.
 * - `iniciUnitat`: Unitats de temps del punt inicial del període (per exemple, MES, DIA). És opcional.
 * - `inici`: Nombre d'unitats de temps a retrocedir per calcular l'inici del període de temps mostrable. És opcional.
 * - `duracioUnitat`: Unitats de temps per especificar la duració del període. És opcional.
 * - `duracio`: La quantitat total de duració en les unitats de temps especificades. És opcional.
 * - `columnes`: Les columnes de la taula que s'han de mostrar; seleccionades des de l'enumeració `TableColumnsEnum`. És opcional.
 * - `dimensioAgrupacio`: Una referència a l'indicador que defineix l'agrupació principal del conjunt de dades. És obligatori.
 * - `titolAgrupament`: El títol que s'assigna a l'agrupació particular dins la taula. És opcional.
 *
 * Ús:
 * Aquesta classe és útil per a aplicacions que requereixen establir detalls específics sobre les taules que s'han de generar
 * a partir d'anàlisis estadístics. Proporciona suport per definir detalls mínims i opcions d'agrupació i filtratge avançades.
 *
 * Relacions:
 * - La propietat `dimensioAgrupacio` fa referència a un objecte `Indicador` identificat pel seu ID.
 * - La llista `dimensionsValor` conté objectes `DimensioValor` per aplicar filtres sobre els valors de dades.
 *
 * Validacions:
 * - Els camps obligatoris com `titol`, `aplicacioCodi`, `dimensionsValor` i `dimensioAgrupacio` han d'estar degudament completats.
 * - Els valors de mida màxima en camps com `titol` i `descripcio` no s’han de superar per respectar el format.
 *
 * Exemple de l'escenari:
 * Aquesta classe es pot utilitzar per configurar widgets en aplicacions de gestió d'estadístiques per visualitzar dades
 * en format de taula amb filtres, columnes seleccionades i períodes definits per l'usuari.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        quickFilterFields = { "titol", "descripcio" },
        descriptionField = "titol",
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN },
                        grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.DELETE }
                )
        }
)
@ValidTaulaWidget
@FieldNameConstants
public class EstadisticaTaulaWidget extends EstadisticaWidget { // WidgetBaseResource<Long> {

    // Camps en cas de vist tipus TABLE
    private List<IndicadorTaula> columnes;

    @NotNull
    private ResourceReference<Dimensio, Long> dimensioAgrupacio;
    private String titolAgrupament;

    // Atributs per a la configuració visual de la taula
    @JsonUnwrapped
    private AtributsVisualsTaula atributsVisuals;

    @Transient
    private ResourceReference<Indicador, Long> indicador;
    @Transient
    @JsonSetter(nulls = Nulls.SKIP)
    private TableColumnsEnum agregacio;
    @Transient
    @JsonSetter(nulls = Nulls.SKIP)
    private PeriodeUnitat unitatAgregacio;

}
