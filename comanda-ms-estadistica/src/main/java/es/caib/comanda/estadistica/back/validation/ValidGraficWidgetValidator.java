package es.caib.comanda.estadistica.back.validation;

import es.caib.comanda.estadistica.back.intf.validation.ValidGraficWidget;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaGraficWidget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ValidGraficWidgetValidator extends ValidWidgetValidator implements ConstraintValidator<ValidGraficWidget, EstadisticaGraficWidget> {

    private final MessageSource messageSource;
    private Locale locale;

    @Override
    public void initialize(final ValidGraficWidget constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        super.initializeLocale(messageSource);
    }

    @Override
    public boolean isValid(EstadisticaGraficWidget widget, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        isValid = validatePeriode(widget, context) && isValid;
        isValid = validateTipusDades(widget, context) && isValid;
        isValid = validateIndicadorsInfo(widget, context) && isValid;

        return isValid;
    }

    private boolean validateTipusDades(EstadisticaGraficWidget widget, ConstraintValidatorContext context) {
        if (widget.getTipusDades() == null) return true;

        boolean isValid = true;

        if (!TipusGraficDataEnum.UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades()) 
                || !Boolean.TRUE.equals(widget.getAgruparPerDimensioDescomposicio())) {
            isValid = validateField(widget.getTempsAgrupacio() != null, context, "tempsAgrupacio", "És obligatori emplenar aquest camp");
        }

        if (TipusGraficDataEnum.UN_INDICADOR.equals(widget.getTipusDades()) || TipusGraficDataEnum.UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades())) {
            isValid = validateField(widget.getIndicador() != null, context, "indicador", "És obligatori emplenar aquest camp") && isValid;
            isValid = validateField(widget.getAgregacio() != null, context, "agregacio", "És obligatori emplenar aquest camp") && isValid;

            if (TableColumnsEnum.AVERAGE.equals(widget.getAgregacio())) {
                isValid = validateField(widget.getUnitatAgregacio() != null, context, "unitatAgregacio", "És obligatori emplenar aquest camp") && isValid;
            }
            if (TipusGraficDataEnum.UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades())) {
                isValid = validateField(widget.getDescomposicioDimensio() != null, context, "descomposicioDimensio", "És obligatori emplenar aquest camp") && isValid;
            }
        } else if (TipusGraficDataEnum.VARIS_INDICADORS.equals(widget.getTipusDades())) {
            isValid = validateField(widget.getIndicadorsInfo() != null && !widget.getIndicadorsInfo().isEmpty(), context, "indicadorsInfo[0].indicador", "És obligatori emplenar aquest camp") && isValid;
        } else if (TipusGraficDataEnum.DOS_INDICADORS.equals(widget.getTipusDades())) {
            // Pendent
        }

        return isValid;
    }

    private boolean validateIndicadorsInfo(EstadisticaGraficWidget widget, ConstraintValidatorContext context) {
        if (widget.getIndicadorsInfo() == null || widget.getIndicadorsInfo().isEmpty()) return true;
        if (!TipusGraficDataEnum.VARIS_INDICADORS.equals(widget.getTipusDades())) return true;

        AtomicBoolean isValid = new AtomicBoolean(true);

        IndicadorTaula primerIndicador = widget.getIndicadorsInfo().get(0);
        isValid.set(validateField(primerIndicador.getIndicador() != null, context, "indicadorsInfo[0].indicador", "Camp obligatori") && isValid.get());
        widget.getIndicadorsInfo().forEach(ind -> {
            if (ind.getIndicador() != null) {
                isValid.set(validateField(ind.getTitol() != null && !ind.getTitol().isEmpty(), context, "indicadorsInfo[" + widget.getIndicadorsInfo().indexOf(ind) + "].titol", "Camp obligatori") && isValid.get());
                isValid.set(validateField(ind.getAgregacio() != null, context, "indicadorsInfo[" + widget.getIndicadorsInfo().indexOf(ind) + "].agregacio", "Camp obligatori") && isValid.get());
                isValid.set(validateField(!TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()) || ind.getUnitatAgregacio() != null, context, "indicadorsInfo[" + widget.getIndicadorsInfo().indexOf(ind) + "].unitatAgregacio", "Camp obligatori") && isValid.get());
            }
        });

        Map<PeriodeUnitat, List<IndicadorTaula>> groupedAvgIndicadors = widget.getIndicadorsInfo().stream()
                .filter(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()))
                .filter(ind -> ind.getUnitatAgregacio() != null) // Garantir que la unitat no és null
                .collect(Collectors.groupingBy(IndicadorTaula::getUnitatAgregacio));

        if (groupedAvgIndicadors.size() > 1) {
            String message = "No hi pot haver difertents unitats d'agregació";
            groupedAvgIndicadors.values().stream()
                    .flatMap(List::stream)
                    .forEach(ind -> addConstraintViolation(context, message,
                        "indicadorsInfo[" + widget.getIndicadorsInfo().indexOf(ind) + "].unitatAgregacio"));
            isValid.set(false);
        }
        return isValid.get();
    }

}