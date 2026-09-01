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
    private static final String MSG_PERCENTATGE_COMB = "es.caib.comanda.estadistica.back.validation.ValidTaulaWidgetValidator.columnes.agregacio.percentatge.combinat";

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
        isValid = validateDimensioAgrupacio(widget, context) && isValid;

        return isValid;
    }

    private boolean validateDimensioAgrupacio(EstadisticaTaulaWidget widget, ConstraintValidatorContext context) {
        // ConsultaEstadisticaHelper.getDadesWidgetTaula sempre desreferencia dimensioAgrupacio; sense
        // aquesta validació, un widget guardat sense agrupació provoca un NullPointerException en consultar-lo.
        return validateField(widget.getDimensioAgrupacio() != null, context, "dimensioAgrupacio", MSG_CAMP_OBLIGATORI);
    }

    private boolean validateColumnes(EstadisticaTaulaWidget widget, ConstraintValidatorContext context) {
        if (widget.getColumnes() == null || widget.getColumnes().isEmpty()) {
            addConstraintViolation(context, MSG_CAMP_OBLIGATORI, "columnes[0].indicador");
            return false;
        }

        AtomicBoolean isValid = new AtomicBoolean(true);

        List<IndicadorTaula> columnes = widget.getColumnes();
        for (int i = 0; i < columnes.size(); i++) {
            IndicadorTaula ind = columnes.get(i);
            isValid.set(validateField(ind.getIndicador() != null && ind.getIndicador().getId() != null, context, "columnes[" + i + "].indicador", MSG_CAMP_OBLIGATORI) && isValid.get());
            isValid.set(validateField(ind.getTitol() != null && !ind.getTitol().isEmpty(), context, "columnes[" + i + "].titol", MSG_CAMP_OBLIGATORI) && isValid.get());
            isValid.set(validateField(ind.getAgregacio() != null, context, "columnes[" + i + "].agregacio", MSG_CAMP_OBLIGATORI) && isValid.get());
            isValid.set(validateField(!TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()) || ind.getUnitatAgregacio() != null, context, "columnes[" + i + "].unitatAgregacio", MSG_CAMP_OBLIGATORI) && isValid.get());
        }

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

        // Una columna PERCENTAGE calcula el seu percentatge a partir d'una altra columna amb el mateix indicador
        // (FetRepositoryCustomImpl.calculateDependentPercentages). Si aquesta columna base és FIRST_SEEN/LAST_SEEN
        // (una data, no un número), el càlcul crema amb NullPointerException/NumberFormatException en temps de consulta.
        Map<Long, List<IndicadorTaula>> columnesPerIndicador = widget.getColumnes().stream()
                .filter(ind -> ind.getIndicador() != null)
                .collect(Collectors.groupingBy(ind -> ind.getIndicador().getId()));
        widget.getColumnes().stream()
                .filter(ind -> ind.getIndicador() != null && TableColumnsEnum.PERCENTAGE.equals(ind.getAgregacio()))
                .forEach(ind -> {
                    List<IndicadorTaula> mateixIndicador = columnesPerIndicador.getOrDefault(ind.getIndicador().getId(), List.of());
                    boolean baseNoNumerica = mateixIndicador.stream()
                            .filter(altre -> !TableColumnsEnum.PERCENTAGE.equals(altre.getAgregacio()))
                            .anyMatch(altre -> TableColumnsEnum.FIRST_SEEN.equals(altre.getAgregacio()) || TableColumnsEnum.LAST_SEEN.equals(altre.getAgregacio()));
                    if (baseNoNumerica) {
                        addConstraintViolation(context,
                                MSG_PERCENTATGE_COMB,
                                "columnes[" + widget.getColumnes().indexOf(ind) + "].agregacio");
                        isValid.set(false);
                    }
                });

        return isValid.get();
    }

}
