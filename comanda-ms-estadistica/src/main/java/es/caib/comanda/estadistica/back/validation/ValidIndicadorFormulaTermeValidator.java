package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.back.intf.validation.ValidIndicadorFormulaTerme;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorFormulaTerme;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Valida que un terme de fórmula d'indicador ({@link IndicadorFormulaTerme}) sigui coherent:
 * - L'indicador "formula" ha d'existir i ser de tipus FORMULA.
 * - L'indicador "component" ha d'existir, ser de tipus SIMPLE (mai una altra FORMULA: no es permeten fórmules
 *   niades) i pertànyer al mateix entornApp que l'indicador de fórmula.
 * - Un indicador no es pot referenciar a si mateix.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
public class ValidIndicadorFormulaTermeValidator implements ConstraintValidator<ValidIndicadorFormulaTerme, IndicadorFormulaTerme> {

    private final IndicadorRepository indicadorRepository;

    @Override
    public boolean isValid(IndicadorFormulaTerme resource, ConstraintValidatorContext context) {
        if (resource.getIndicadorFormula() == null || resource.getIndicadorComponent() == null) {
            // Ja cobert per les anotacions @NotNull dels camps.
            return true;
        }
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        Long formulaId = resource.getIndicadorFormula().getId();
        Long componentId = resource.getIndicadorComponent().getId();

        if (formulaId != null && componentId != null && formulaId.equals(componentId)) {
            context.buildConstraintViolationWithTemplate("{es.caib.comanda.estadistica.back.validation.ValidIndicadorFormulaTermeValidator.referenciaSiMateix}")
                    .addPropertyNode(IndicadorFormulaTerme.Fields.indicadorComponent)
                    .addConstraintViolation();
            return false;
        }

        IndicadorEntity formula = formulaId != null ? indicadorRepository.findById(formulaId).orElse(null) : null;
        if (formula == null || formula.getTipus() != IndicadorTipus.FORMULA) {
            context.buildConstraintViolationWithTemplate("{es.caib.comanda.estadistica.back.validation.ValidIndicadorFormulaTermeValidator.formulaNoValida}")
                    .addPropertyNode(IndicadorFormulaTerme.Fields.indicadorFormula)
                    .addConstraintViolation();
            isValid = false;
        }

        IndicadorEntity component = componentId != null ? indicadorRepository.findById(componentId).orElse(null) : null;
        if (component == null || component.getTipus() != IndicadorTipus.SIMPLE) {
            context.buildConstraintViolationWithTemplate("{es.caib.comanda.estadistica.back.validation.ValidIndicadorFormulaTermeValidator.componentNoValid}")
                    .addPropertyNode(IndicadorFormulaTerme.Fields.indicadorComponent)
                    .addConstraintViolation();
            isValid = false;
        }

        if (formula != null && component != null && !formula.getEntornAppId().equals(component.getEntornAppId())) {
            context.buildConstraintViolationWithTemplate("{es.caib.comanda.estadistica.back.validation.ValidIndicadorFormulaTermeValidator.entornAppDiferent}")
                    .addPropertyNode(IndicadorFormulaTerme.Fields.indicadorComponent)
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
