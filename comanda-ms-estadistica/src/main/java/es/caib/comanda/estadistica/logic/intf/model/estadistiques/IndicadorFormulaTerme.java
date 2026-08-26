package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.back.intf.validation.ValidIndicadorFormulaTerme;
import es.caib.comanda.ms.logic.intf.annotation.ResourceAccessConstraint;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.permission.PermissionEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import javax.validation.constraints.NotNull;

/**
 * Classe que representa un terme d'una fórmula d'indicador: un indicador component (del mateix entornApp que
 * la fórmula, i sempre de tipus SIMPLE, mai una altra FORMULA) i l'operador amb què s'aplica sobre l'acumulat
 * dels termes anteriors dins la mateixa fórmula (p.ex. IND1 + IND2 - IND3).
 *
 * Propietats:
 * - indicadorFormula: L'indicador de tipus FORMULA al qual pertany aquest terme.
 * - indicadorComponent: L'indicador (SIMPLE) el valor del qual s'utilitza en aquest terme.
 * - operador: SUMA o RESTA.
 * - ordre: Posició del terme dins la fórmula.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@NoArgsConstructor
@FieldNameConstants
@ValidIndicadorFormulaTerme
@ResourceConfig(
        descriptionField = "ordre",
        accessConstraints = {
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
public class IndicadorFormulaTerme extends BaseResource<Long> {

    @NotNull
    private ResourceReference<Indicador, Long> indicadorFormula;
    @NotNull
    private ResourceReference<Indicador, Long> indicadorComponent;
    @NotNull
    private OperadorFormulaEnum operador;
    @NotNull
    private Integer ordre;

}
