package es.caib.comanda.estadistica.logic.intf.model.dashboard;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Configuració d'un filtre mostrat a la capçalera d'un Dashboard. Cada fila d'aquest recurs és un control de filtre
 * (una dimensió o el període) que l'usuari podrà emplenar en visualitzar el dashboard; el valor seleccionat s'aplica
 * a tots els widgets del dashboard (vegeu {@link DashboardFiltreTipus}).
 *
 * A diferència del filtre de dimensions propi d'un widget (que fixa sempre el mateix valor), aquest recurs només en
 * defineix la configuració (quin control es mostra); la selecció de valors la fa l'usuari en temps de visualització
 * i es transporta com a paràmetre de l'informe {@code widget_data} (vegeu {@code DashboardFiltreSeleccio}).
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        descriptionField = "titol",
        accessConstraints = {
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.AUTHENTICATED,
                        grantedPermissions = { PermissionEnum.READ }
                ),
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_ADMIN },
                        grantedPermissions = { PermissionEnum.READ, PermissionEnum.WRITE, PermissionEnum.CREATE, PermissionEnum.DELETE }
                ),
                @ResourceAccessConstraint(
                        type = ResourceAccessConstraint.ResourceAccessConstraintType.ROLE,
                        roles = { BaseConfig.ROLE_CONSULTA },
                        grantedPermissions = { PermissionEnum.READ }
                )
        }
)
public class DashboardFiltre extends BaseResource<Long> {

    @NotNull
    private ResourceReference<Dashboard, Long> dashboard;

    @NotNull
    private DashboardFiltreTipus tipus;

    /** Codi de la dimensió a filtrar. Obligatori si tipus=DIMENSIO, ha de ser buit si tipus=PERIODE. **/
    @Size(max = 32)
    private String dimensioCodi;

    /** Etiqueta a mostrar al control. Si es deixa buit, s'usa el nom de la dimensió (o "Període"). **/
    @Size(max = 64)
    private String titol;

    @NotNull
    private int ordre;

    /** Només aplica a tipus=DIMENSIO: permet seleccionar més d'un valor alhora. **/
    private Boolean multiple = true;

    @AssertTrue(message = "El codi de dimensió és obligatori per a filtres de tipus DIMENSIO i no s'ha d'emplenar per a PERIODE")
    public boolean isDimensioCodiCoherent() {
        if (tipus == null) {
            return true; // ja es valida per separat amb @NotNull
        }
        return tipus == DashboardFiltreTipus.DIMENSIO
                ? dimensioCodi != null && !dimensioCodi.isBlank()
                : dimensioCodi == null;
    }

}
