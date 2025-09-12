package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceArtifact;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
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
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Classe que representa un valor associat a una dimensió.
 *
 * La classe DimensioValor vincula un valor específic amb una dimensió determinada dins del sistema.
 * Aquesta classe hereta de BaseResource, que proporciona un identificador únic del tipus Long.
 *
 * Propietats:
 * - `valor`: Representa el valor text de la dimensió. És un camp obligatori amb una longitud màxima de 255 caràcters.
 * - `dimensio`: Referència a l'entitat Dimensio a la qual pertany el valor.
 *             És un camp obligatori que utilitza el tipus genèric ResourceReference.
 *
 * Validacions:
 * - `valor`: No pot ser nul i ha de tenir com a màxim 255 caràcters.
 * - `dimensio`: No pot ser nul.
 *
 * Ús:
 * Aquesta classe s'utilitza per gestionar i organitzar els valors associats a una dimensió dins del context del model
 * d'aplicació.
 *
 * Relacions:
 * - Cada objecte DimensioValor està associat a una única Dimensio mitjançant la propietat `dimensio`.
 *
 * Permet assegurar estructures clares i validacions adequades per a dades relacionades amb dimensions dins
 * d'una aplicació complexa.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        quickFilterFields = { "dimensio.nom", "valor" },
        descriptionField = "desc",
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN },
                        grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.DELETE }
                )
        },
        artifacts = {
                @ResourceArtifact(type = ResourceArtifactType.FILTER, code = DimensioValor.DIMENSIO_VALOR_FILTER, formClass = DimensioValor.DimensioValorFilter.class)
        }
)
public class DimensioValor extends BaseResource<Long> {

    public static final String DIMENSIO_VALOR_FILTER = "dimensioValorFilter";

    @NotNull
    @Size(max = 255)
    private String valor;
    @NotNull
    private ResourceReference<Dimensio, Long> dimensio;

    // Configuració d'agrupació
    private Boolean agrupable;
    private String valorAgrupacio;

    public String getDesc() {
        return dimensio.getDescription() + " [" + valor + "]";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldNameConstants
    public static class DimensioValorFilter implements Serializable {
        private String valor;
        private Boolean agrupable;
        private String valorAgrupacio;
    }
}
