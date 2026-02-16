package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceField;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * Classe que representa una Dimensió.
 *
 * Una dimensió s'utilitza per categoritzar i organitzar dades dins d'un context específic d'una aplicació.
 * Pot incloure un codi únic, un nom descriptiu, una descripció opcional i un conjunt de valors associats.
 *
 * Propietats:
 * - codi: Un identificador únic per a la dimensió, limitat a 16 caràcters alfanumèrics.
 * - nom: Un nom descriptiu de la dimensió, limitat a 64 caràcters.
 * - descripcio: Una descripció opcional de la dimensió, limitada a 1024 caràcters.
 * - entornAppId: Identificador de l'entorn de l'aplicació al qual pertany la dimensió.
 * - valors: Una llista d'objectes DimensioValor associats als valors de la dimensió.
 *
 * Aquesta classe hereta de BaseResource, que proporciona un identificador únic del tipus Long.
 *
 * Validacions:
 * - `codi`: Només es permeten caràcters alfanumèrics. És obligatori i té una longitud màxima de 16.
 * - `nom`: És obligatori i té una longitud màxima de 64.
 * - `descripcio`: Opcional però amb una longitud màxima de 1024.
 * - `entornAppId`: És obligatori.
 * - `valors`: Conté una llista de valors associats a la dimensió.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@FieldNameConstants
@ResourceConfig(
        quickFilterFields = { "nom", "descripcio" },
        descriptionField = "nom",
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN },
                        grantedPermissions = { PermissionEnum.READ }
                ),
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_CONSULTA },
                        grantedPermissions = { PermissionEnum.READ }
                )
        },
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.FILTER, code = Dimensio.DIMENSIO_FILTER, formClass = Dimensio.DimensioFilter.class),
                @ResourceArtifact(type = ResourceArtifactType.FILTER, code = Dimensio.FILTER_BY_DIMENSIO, formClass = Dimensio.FilterByDimensio.class)
        }
)
public class Dimensio extends BaseResource<Long> {

    /** Named Filter para devolver un solo resultado por el atributo nom **/
    public static final String NAMED_FILTER_BY_APP_GROUP_BY_NOM = "filterByAppGroupByNom";
    public final static String DIMENSIO_FILTER = "dimensioFilter";
    public final static String FILTER_BY_DIMENSIO = "filterByDimensio";
    public final static String FILTER_BY_APP_NAMEDFILTER = "filterByApp";

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "El codi només pot contenir caràcters alfanumèrics")
    @Size(max = 32)
    private String codi;
    @NotNull
    @Size(max = 64)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    @NotNull
    private Long entornAppId;
    private List<DimensioValor> valors;

    private Integer agrupableCount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class DimensioFilter implements Serializable {
        private String codi;
        private String nom;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class FilterByDimensio implements Serializable {
        @ResourceField(descriptionField = Dimensio.Fields.nom) // El description field es força a usar el nom, ja que al frontal s'aprofita per a generar els filtres del DataGrid
        protected ResourceReference<Dimensio, Long> dimensio;
    }
}
