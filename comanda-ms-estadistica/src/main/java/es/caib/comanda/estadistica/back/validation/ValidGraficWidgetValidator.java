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

    private static final String MSG_CAMP_OBLIGATORI = "es.caib.comanda.estadistica.back.validation.ValidGraficWidgetValidator.campObligatori";
    private static final String MSG_DIFERENTS_UNITATS = "es.caib.comanda.estadistica.back.validation.ValidGraficWidgetValidator.indicadorsInfo.unitatAgregacio.diferents";
    private static final String MSG_PERCENTATGE_MIX = "es.caib.comanda.estadistica.back.validation.ValidGraficWidgetValidator.indicadorsInfo.agregacio.percentatgeMix";

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
            isValid = validateField(widget.getTempsAgrupacio() != null, context, "tempsAgrupacio", MSG_CAMP_OBLIGATORI);
        }

        if (TipusGraficDataEnum.UN_INDICADOR.equals(widget.getTipusDades()) || TipusGraficDataEnum.UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades())) {
            isValid = validateField(widget.getIndicador() != null, context, "indicador", MSG_CAMP_OBLIGATORI) && isValid;
            isValid = validateField(widget.getAgregacio() != null, context, "agregacio", MSG_CAMP_OBLIGATORI) && isValid;

            if (TableColumnsEnum.AVERAGE.equals(widget.getAgregacio())) {
                isValid = validateField(widget.getUnitatAgregacio() != null, context, "unitatAgregacio", MSG_CAMP_OBLIGATORI) && isValid;
            }
            if (TipusGraficDataEnum.UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades())) {
                isValid = validateField(widget.getDescomposicioDimensio() != null, context, "descomposicioDimensio", MSG_CAMP_OBLIGATORI) && isValid;
            }
        } else if (TipusGraficDataEnum.VARIS_INDICADORS.equals(widget.getTipusDades())) {
            isValid = validateField(widget.getIndicadorsInfo() != null && !widget.getIndicadorsInfo().isEmpty(), context, "indicadorsInfo[0].indicador", MSG_CAMP_OBLIGATORI) && isValid;
        } else if (TipusGraficDataEnum.DOS_INDICADORS.equals(widget.getTipusDades())) {
            isValid = validateField(widget.getIndicador() != null, context, "indicador", MSG_CAMP_OBLIGATORI) && isValid;
            isValid = validateField(widget.getAgregacio() != null, context, "agregacio", MSG_CAMP_OBLIGATORI) && isValid;
            if (TableColumnsEnum.AVERAGE.equals(widget.getAgregacio())) {
                isValid = validateField(widget.getUnitatAgregacio() != null, context, "unitatAgregacio", MSG_CAMP_OBLIGATORI) && isValid;
            }
            isValid = validateField(widget.getIndicadorMax() != null, context, "indicadorMax", MSG_CAMP_OBLIGATORI) && isValid;
            isValid = validateField(widget.getAgregacioMax() != null, context, "agregacioMax", MSG_CAMP_OBLIGATORI) && isValid;
            if (TableColumnsEnum.AVERAGE.equals(widget.getAgregacioMax())) {
                isValid = validateField(widget.getUnitatAgregacioMax() != null, context, "unitatAgregacioMax", MSG_CAMP_OBLIGATORI) && isValid;
            }

            boolean valorEsPercentatge = TableColumnsEnum.PERCENTAGE.equals(widget.getAgregacio());
            boolean maximEsPercentatge = TableColumnsEnum.PERCENTAGE.equals(widget.getAgregacioMax());
            if (valorEsPercentatge != maximEsPercentatge) {
                addConstraintViolation(context, MSG_PERCENTATGE_MIX, "agregacioMax");
                isValid = false;
            }

            if (TableColumnsEnum.AVERAGE.equals(widget.getAgregacio()) && TableColumnsEnum.AVERAGE.equals(widget.getAgregacioMax())
                    && widget.getUnitatAgregacio() != null && !widget.getUnitatAgregacio().equals(widget.getUnitatAgregacioMax())) {
                addConstraintViolation(context, MSG_DIFERENTS_UNITATS, "unitatAgregacioMax");
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateIndicadorsInfo(EstadisticaGraficWidget widget, ConstraintValidatorContext context) {
        if (widget.getIndicadorsInfo() == null || widget.getIndicadorsInfo().isEmpty()) return true;
        if (!TipusGraficDataEnum.VARIS_INDICADORS.equals(widget.getTipusDades())) return true;

        AtomicBoolean isValid = new AtomicBoolean(true);

        List<IndicadorTaula> indicadorsInfo = widget.getIndicadorsInfo();
        for (int i = 0; i < indicadorsInfo.size(); i++) {
            IndicadorTaula ind = indicadorsInfo.get(i);
            isValid.set(validateField(ind.getIndicador() != null && ind.getIndicador().getId() != null, context, "indicadorsInfo[" + i + "].indicador", MSG_CAMP_OBLIGATORI) && isValid.get());
            isValid.set(validateField(ind.getTitol() != null && !ind.getTitol().isEmpty(), context, "indicadorsInfo[" + i + "].titol", MSG_CAMP_OBLIGATORI) && isValid.get());
            isValid.set(validateField(ind.getAgregacio() != null, context, "indicadorsInfo[" + i + "].agregacio", MSG_CAMP_OBLIGATORI) && isValid.get());
            isValid.set(validateField(!TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()) || ind.getUnitatAgregacio() != null, context, "indicadorsInfo[" + i + "].unitatAgregacio", MSG_CAMP_OBLIGATORI) && isValid.get());
        }

        Map<PeriodeUnitat, List<IndicadorTaula>> groupedAvgIndicadors = widget.getIndicadorsInfo().stream()
                .filter(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()))
                .filter(ind -> ind.getUnitatAgregacio() != null) // Garantir que la unitat no és null
                .collect(Collectors.groupingBy(IndicadorTaula::getUnitatAgregacio));

        if (groupedAvgIndicadors.size() > 1) {
            groupedAvgIndicadors.values().stream()
                    .flatMap(List::stream)
                    .forEach(ind -> addConstraintViolation(context, MSG_DIFERENTS_UNITATS,
                        "indicadorsInfo[" + widget.getIndicadorsInfo().indexOf(ind) + "].unitatAgregacio"));
            isValid.set(false);
        }

        boolean hasPercentage = widget.getIndicadorsInfo().stream().anyMatch(ind -> TableColumnsEnum.PERCENTAGE.equals(ind.getAgregacio()));
        boolean hasNonPercentage = widget.getIndicadorsInfo().stream().anyMatch(ind -> ind.getAgregacio() != null && !TableColumnsEnum.PERCENTAGE.equals(ind.getAgregacio()));

        if (hasPercentage && hasNonPercentage) {
            widget.getIndicadorsInfo().stream()
                    .filter(ind -> TableColumnsEnum.PERCENTAGE.equals(ind.getAgregacio()))
                    .forEach(ind -> addConstraintViolation(context, MSG_PERCENTATGE_MIX,
                            "indicadorsInfo[" + widget.getIndicadorsInfo().indexOf(ind) + "].agregacio"));
            isValid.set(false);
        }

        return isValid.get();
    }

}
