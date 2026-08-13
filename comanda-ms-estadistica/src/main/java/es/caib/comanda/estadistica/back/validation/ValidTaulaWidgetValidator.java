package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.back.intf.validation.ValidTaulaWidget;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaTaulaWidget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ValidTaulaWidgetValidator extends ValidWidgetValidator implements ConstraintValidator<ValidTaulaWidget, EstadisticaTaulaWidget> {

    private static final String MSG_CAMP_OBLIGATORI = "es.caib.comanda.estadistica.back.validation.ValidTaulaWidgetValidator.campObligatori";
    private static final String MSG_DIFERENTS_UNITATS = "es.caib.comanda.estadistica.back.validation.ValidTaulaWidgetValidator.columnes.unitatAgregacio.diferents";
    private static final String MSG_PERCENTATGE_MIX = "es.caib.comanda.estadistica.back.validation.ValidTaulaWidgetValidator.columnes.agregacio.percentatgeMix";

    private final MessageSource messageSource;

    @Override
    public void initialize(final ValidTaulaWidget constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        super.initializeLocale(messageSource);
    }

    @Override
    public boolean isValid(EstadisticaTaulaWidget widget, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        isValid = validatePeriode(widget, context) && isValid;
        isValid = validateColumnes(widget, context) && isValid;

        return isValid;
    }

    private boolean validateColumnes(EstadisticaTaulaWidget widget, ConstraintValidatorContext context) {
        if (widget.getColumnes() == null || widget.getColumnes().isEmpty()) {
            addConstraintViolation(context, MSG_CAMP_OBLIGATORI, "columnes[0].indicador");
            return false;
        }

        AtomicBoolean isValid = new AtomicBoolean(true);

        IndicadorTaula primerIndicador = widget.getColumnes().get(0);
        isValid.set(validateField(primerIndicador.getIndicador() != null, context, "columnes[0].indicador", MSG_CAMP_OBLIGATORI) && isValid.get());
        widget.getColumnes().forEach(ind -> {
            if (ind.getIndicador() != null) {
                isValid.set(validateField(ind.getTitol() != null && !ind.getTitol().isEmpty(), context, "columnes[" + widget.getColumnes().indexOf(ind) + "].titol", MSG_CAMP_OBLIGATORI) && isValid.get());
                isValid.set(validateField(ind.getAgregacio() != null, context, "columnes[" + widget.getColumnes().indexOf(ind) + "].agregacio", MSG_CAMP_OBLIGATORI) && isValid.get());
                isValid.set(validateField(!TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()) || ind.getUnitatAgregacio() != null, context, "columnes[" + widget.getColumnes().indexOf(ind) + "].unitatAgregacio", MSG_CAMP_OBLIGATORI) && isValid.get());
            }
        });

        Map<PeriodeUnitat, List<IndicadorTaula>> groupedAvgIndicadors = widget.getColumnes().stream()
                .filter(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()))
                .filter(ind -> ind.getUnitatAgregacio() != null) // Garantir que la unitat no és null
                .collect(Collectors.groupingBy(IndicadorTaula::getUnitatAgregacio));

        if (groupedAvgIndicadors.size() > 1) {
            groupedAvgIndicadors.values().stream()
                    .flatMap(List::stream)
                    .forEach(ind -> addConstraintViolation(context, MSG_DIFERENTS_UNITATS,
                            "columnes[" + widget.getColumnes().indexOf(ind) + "].unitatAgregacio"));
            isValid.set(false);
        }

        boolean hasPercentage = widget.getColumnes().stream().anyMatch(ind -> TableColumnsEnum.PERCENTAGE.equals(ind.getAgregacio()));
        boolean hasNonPercentage = widget.getColumnes().stream().anyMatch(ind -> ind.getAgregacio() != null && !TableColumnsEnum.PERCENTAGE.equals(ind.getAgregacio()));

        if (hasPercentage && hasNonPercentage) {
            widget.getColumnes().stream()
                    .filter(ind -> TableColumnsEnum.PERCENTAGE.equals(ind.getAgregacio()))
                    .forEach(ind -> addConstraintViolation(context, MSG_PERCENTATGE_MIX,
                            "columnes[" + widget.getColumnes().indexOf(ind) + "].agregacio"));
            isValid.set(false);
        }

        return isValid.get();
    }

}
