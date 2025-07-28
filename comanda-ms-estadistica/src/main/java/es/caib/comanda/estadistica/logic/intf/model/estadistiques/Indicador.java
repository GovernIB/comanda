package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import es.caib.comanda.ms.estadistica.model.Format;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Classe que representa un Indicador.
 *
 * Un indicador és un element que proporciona una mesura quantitativa o qualitativa dins d'un determinat
 * entorn d'aplicació. Aquesta classe s'empra per definir propietats base com el codi, el nom, una
 * descripció opcional, l'entorn associat i un format específic.
 *
 * Propietats:
 * - codi: Identificador únic i obligatori per a l'indicador, limitat a 16 caràcters alfanumèrics.
 * - nom: Nom descriptiu de l'indicador, obligatori i limitat a 64 caràcters.
 * - descripcio: Descripció opcional de l'indicador, limitada a 1024 caràcters.
 * - entornAppId: Identificador obligatori de l'entorn d'aplicació associat a l'indicador.
 * - format: Format opcional que descriu com es representa el valor de l'indicador (LONG, DECIMAL, PERCENTAGE,
 *   CURRENCY, DATE, DATETIME, BOOLEAN).
 *
 * Aquesta classe hereta de BaseResource, proporcionant un identificador únic del tipus Long
 * per a cada instància.
 *
 * Validacions:
 * - `codi`: Només es permeten caràcters alfanumèrics. És obligatori i té una longitud màxima de 16.
 * - `nom`: Camp obligatori amb una longitud màxima de 64.
 * - `descripcio`: És opcional amb una longitud màxima de 1024.
 * - `entornAppId`: Camp obligatori que identifica l'entorn d'aplicació.
 *
 * Objectiu:
 * - Facilitar la representació estructurada i validada d'indicadors en sistemes estadístics o d'anàlisi.
 * - Proveir atributs clau comuns per als indicadors que es poden integrar en diferents contextos.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
    quickFilterFields = { "codi", "nom" },
    descriptionField = "codiNomDescription")
public class Indicador extends BaseResource<Long> {

    /** Named Filter para devolver un solo resultado por el atributo nom **/
    public static final String NAMED_FILTER_GROUP_BY_NOM = "groupByNom";

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "El codi només pot contenir caràcters alfanumèrics")
    @Size(max = 16)
    private String codi;
    @NotNull
    @Size(max = 64)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    @NotNull
    private Long entornAppId;
    private Format format;

    public String getCodiNomDescription() {
        return this.codi + " - " + this.nom;
    }

}
