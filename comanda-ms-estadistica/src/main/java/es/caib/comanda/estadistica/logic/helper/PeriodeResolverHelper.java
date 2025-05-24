package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.periode.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

public class PeriodeResolverHelper {

    public static PeriodeDates resolvePeriod(WidgetBaseResource<Long> widget) {
        switch (widget.getPeriodeMode()) {
            case PRESET:
                return resolvePresetPeriod(widget.getPresetPeriode(), widget.getPresetCount());
            case RELATIU:
                return resolveRelativePeriod(
                        widget.getRelatiuPuntReferencia(),
                        widget.getRelatiuCount(),
                        widget.getRelatiueUnitat(),
                        widget.getRelatiuAlineacio());
            case ABSOLUT:
                return resolveAbsolutePeriod(widget);
            default:
                throw new IllegalArgumentException("Invalid period mode");
        }
    }

    private static PeriodeDates resolvePresetPeriod(PresetPeriode preset, Integer count) {
        LocalDate today = LocalDate.now();
        switch (preset) {
            // Rolling periods
            case DARRERS_7_DIES:
                return PeriodeDates.builder().start(today.minusDays(7)).end(today).build();
            case DARRERS_14_DIES:
                return PeriodeDates.builder().start(today.minusDays(14)).end(today).build();
            case DARRERS_30_DIES:
                return PeriodeDates.builder().start(today.minusDays(30)).end(today).build();
            case DARRERS_90_DIES:
                return PeriodeDates.builder().start(today.minusDays(90)).end(today).build();
            case DARRERS_180_DIES:
                return PeriodeDates.builder().start(today.minusDays(180)).end(today).build();
            case DARRERS_365_DIES:
                return PeriodeDates.builder().start(today.minusDays(365)).end(today).build();
            case DARRERES_4_SETMANES:
                return PeriodeDates.builder().start(today.minusWeeks(4)).end(today).build();
            case DARRERES_12_SETMANES:
                return PeriodeDates.builder().start(today.minusWeeks(12)).end(today).build();
            case DARRERES_52_SETMANES:
                return PeriodeDates.builder().start(today.minusWeeks(52)).end(today).build();
            case DARRERS_3_MESOS:
                return PeriodeDates.builder().start(today.minusMonths(3)).end(today).build();
            case DARRERS_6_MESOS:
                return PeriodeDates.builder().start(today.minusMonths(6)).end(today).build();
            case DARRERS_12_MESOS:
                return PeriodeDates.builder().start(today.minusMonths(12)).end(today).build();
            case DARRERS_4_TRIMESTRES:
                return PeriodeDates.builder().start(today.minusMonths(12)).end(today).build();
            case DARRER_1_ANY:
                return PeriodeDates.builder().start(today.minusYears(1)).end(today).build();
            case DARRERS_2_ANYS:
                return PeriodeDates.builder().start(today.minusYears(2)).end(today).build();
            case DARRERS_5_ANYS:
                return PeriodeDates.builder().start(today.minusYears(5)).end(today).build();
            // Complete periods
            case DARRER_COMPLET_DIA:
                return PeriodeDates.builder().start(today.minusDays(1)).end(today.minusDays(1)).build();
            case DARRERA_COMPLETA_SETMANA:
                LocalDate lastWeekEnd = today.minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7);
                return PeriodeDates.builder().start(lastWeekEnd.minusDays(6)).end(lastWeekEnd).build();
            case DARRER_COMPLET_MES:
                LocalDate lastMonth = today.minusMonths(1);
                return PeriodeDates.builder().start(lastMonth.withDayOfMonth(1))
                        .end(lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())).build();
            case DARRER_COMPLET_TRIMESTRE:
                LocalDate lastQuarter = today.minusMonths(3);
                int quarterStartMonth = ((lastQuarter.getMonthValue() - 1) / 3) * 3 + 1;
                LocalDate quarterStart = LocalDate.of(lastQuarter.getYear(), quarterStartMonth, 1);
                return PeriodeDates.builder().start(quarterStart)
                        .end(quarterStart.plusMonths(3).minusDays(1)).build();
            case DARRER_COMPLET_ANY:
                int lastYear = today.getYear() - 1;
                return PeriodeDates.builder().start(LocalDate.of(lastYear, 1, 1))
                        .end(LocalDate.of(lastYear, 12, 31)).build();
            // To Date periods
            case AVUI:
                return PeriodeDates.builder().start(today).end(today).build();
            case AHIR:
                return PeriodeDates.builder().start(today.minusDays(1)).end(today.minusDays(1)).build();
            case AQUESTA_SETMANA_FINS_ARA:
                return PeriodeDates.builder().start(today.with(ChronoField.DAY_OF_WEEK, 1)).end(today).build();
            case AQUEST_MES_FINS_ARA:
                return PeriodeDates.builder().start(today.withDayOfMonth(1)).end(today).build();
            case AQUEST_TRIMESTRE_FINS_ARA:
                int currentQuarterStartMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
                return PeriodeDates.builder().start(LocalDate.of(today.getYear(), currentQuarterStartMonth, 1))
                        .end(today).build();
            case AQUEST_ANY_FINS_ARA:
                return PeriodeDates.builder().start(LocalDate.of(today.getYear(), 1, 1)).end(today).build();
            // Generic N periods
            case DARRERS_N_DIES:
                return PeriodeDates.builder().start(today.minusDays(count)).end(today).build();
            case DARRERES_N_SETMANES:
                return PeriodeDates.builder().start(today.minusWeeks(count)).end(today).build();
            case DARRERS_N_MESOS:
                return PeriodeDates.builder().start(today.minusMonths(count)).end(today).build();
            case DARRERS_N_TRIMESTRES:
                return PeriodeDates.builder().start(today.minusMonths(count * 3L)).end(today).build();
            case DARRERS_N_ANYS:
                return PeriodeDates.builder().start(today.minusYears(count)).end(today).build();
            default:
                throw new IllegalArgumentException("Unsupported preset period");
        }
    }

    private static PeriodeDates resolveRelativePeriod(PeriodeAnchor anchor,
                                               Integer count,
                                               PeriodeUnitat unit, 
                                               PeriodeAlineacio alignment) {
        LocalDate end = calculateReferenceDate(anchor);
        LocalDate start = calculateStartDate(end, count, unit);

        if (alignment != null) {
            start = alignDate(start, alignment, unit);
            end = alignDate(end, alignment, unit);
        }

        return PeriodeDates.builder()
                .start(start)
                .end(end)
                .build();
    }

    private static LocalDate calculateReferenceDate(PeriodeAnchor anchor) {
        var today = LocalDate.now();
        switch (anchor) {
            case ARA:
            case INICI_DIA:
                return today;
            case INICI_SETMANA:
                return today.with(ChronoField.DAY_OF_WEEK, 1);
            case INICI_MES:
                return today.withDayOfMonth(1);
            case INICI_TRIMESTRE:
                int quarter = (today.getMonthValue() - 1) / 3;
                return LocalDate.of(today.getYear(), quarter * 3 + 1, 1);
            case INICI_ANY:
                return LocalDate.of(today.getYear(), 1, 1);
            default:
                return today;
        }
    }

    private static LocalDate calculateStartDate(LocalDate reference, Integer count, PeriodeUnitat unit) {
        switch (unit) {
            case DIA:
                return reference.minusDays(count);
            case SETMANA:
                return reference.minusWeeks(count);
            case MES:
                return reference.minusMonths(count);
            case TRIMESTRE:
                return reference.minusMonths(count * 3L);
            case ANY:
                return reference.minusYears(count);
            default:
                throw new IllegalArgumentException("Unsupported period unit");
        }
    }

    private static LocalDate alignDate(LocalDate date, PeriodeAlineacio alignment, PeriodeUnitat unit) {
        switch (alignment) {
            case ROLLING:
                return date;
            case COMPLETE_UNITS:
                switch (unit) {
                    case DIA:
                        return date;
                    case SETMANA:
                        return date.with(ChronoField.DAY_OF_WEEK, 1);
                    case MES:
                        return date.withDayOfMonth(1);
                    case TRIMESTRE:
                        int quarter = (date.getMonthValue() - 1) / 3;
                        return LocalDate.of(date.getYear(), quarter * 3 + 1, 1);
                    case ANY:
                        return LocalDate.of(date.getYear(), 1, 1);
                    default:
                        return date;
                }
            default:
                return date;
        }
    }

    private static PeriodeDates resolveAbsolutePeriod(WidgetBaseResource<Long> widget) {
        switch (widget.getAbsolutTipus()) {
            case DATE_RANGE:
                return PeriodeDates.builder()
                        .start(widget.getAbsolutDataInici())
                        .end(widget.getAbsolutDataFi())
                        .build();
            case SPECIFIC_PERIOD_OF_YEAR:
                int year = calculateYear(widget.getAbsolutAnyReferencia(), widget.getAbsolutAnyValor());
                int firstPeriod = widget.getAbsolutPeriodeInici() != null
                        ? widget.getAbsolutPeriodeInici()
                        : 1;
                int lastPeriod = widget.getAbsolutPeriodeFi() != null
                        ? widget.getAbsolutPeriodeFi()
                        : firstPeriod;

                LocalDate startDate, endDate;
                switch (widget.getAbsolutPeriodeUnitat()) {
                    case SETMANA:
                        startDate = LocalDate.of(year, 1, 1)
                                .plusWeeks(firstPeriod - 1)
                                .with(ChronoField.DAY_OF_WEEK, 1);
                        endDate = LocalDate.of(year, 1, 1)
                                .plusWeeks(lastPeriod - 1)
                                .with(ChronoField.DAY_OF_WEEK, 7);
                        break;
                    case MES:
                        startDate = LocalDate.of(year, firstPeriod, 1);
                        endDate = LocalDate.of(year, lastPeriod, 1)
                                .plusMonths(1).minusDays(1);
                        break;
                    case TRIMESTRE:
                        startDate = LocalDate.of(year, ((firstPeriod - 1) * 3) + 1, 1);
                        endDate = LocalDate.of(year, (lastPeriod * 3), 1)
                                .plusMonths(1).minusDays(1);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported period unit for specific year period");
                }

                return PeriodeDates.builder()
                        .start(startDate)
                        .end(endDate)
                        .build();
            default:
                throw new IllegalArgumentException("Unsupported absolute period type");
        }
    }

    private static int calculateYear (PeriodeEspecificAny yearReference, Integer specificYear){
        LocalDate today = LocalDate.now();
        switch (yearReference) {
            case CURRENT_YEAR:
                return today.getYear();
            case PREVIOUS_YEAR:
                return today.getYear() - 1;
            case SPECIFIC_YEAR:
                if (specificYear == null) {
                    throw new IllegalArgumentException("Specific year value is required");
                }
                return specificYear;
            default:
                throw new IllegalArgumentException("Invalid year reference");
        }
    }
    
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodeDates {
        public LocalDate start;
        public LocalDate end;
    }
}
