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
            addConstraintViolation(context, "És obligatori emplenar aquest camp", "columnes[0].indicador");
            return false;
        }

        AtomicBoolean isValid = new AtomicBoolean(true);

        IndicadorTaula primerIndicador = widget.getColumnes().get(0);
        isValid.set(validateField(primerIndicador.getIndicador() != null, context, "columnes[0].indicador", "Camp obligatori") && isValid.get());
        widget.getColumnes().forEach(ind -> {
            if (ind.getIndicador() != null) {
                isValid.set(validateField(ind.getTitol() != null && !ind.getTitol().isEmpty(), context, "columnes[" + widget.getColumnes().indexOf(ind) + "].titol", "Camp obligatori") && isValid.get());
                isValid.set(validateField(ind.getAgregacio() != null, context, "columnes[" + widget.getColumnes().indexOf(ind) + "].agregacio", "Camp obligatori") && isValid.get());
                isValid.set(validateField(!TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()) || ind.getUnitatAgregacio() != null, context, "columnes[" + widget.getColumnes().indexOf(ind) + "].unitatAgregacio", "Camp obligatori") && isValid.get());
            }
        });

        Map<PeriodeUnitat, List<IndicadorTaula>> groupedAvgIndicadors = widget.getColumnes().stream()
                .filter(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()))
                .filter(ind -> ind.getUnitatAgregacio() != null) // Garantir que la unitat no és null
                .collect(Collectors.groupingBy(IndicadorTaula::getUnitatAgregacio));

        if (groupedAvgIndicadors.size() > 1) {
            String message = "No hi pot haver difertents unitats d'agregació";
            groupedAvgIndicadors.values().stream()
                    .flatMap(List::stream)
                    .forEach(ind -> addConstraintViolation(context, message,
                            "columnes[" + widget.getColumnes().indexOf(ind) + "].unitatAgregacio"));
            isValid.set(false);
        }
        return isValid.get();
    }

}
