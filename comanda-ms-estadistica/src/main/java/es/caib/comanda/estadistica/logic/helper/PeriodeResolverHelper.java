package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.periode.Periode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAlineacio;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeAnchor;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeEspecificAny;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeMode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.periode.PresetPeriode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

public class PeriodeResolverHelper {

    public static PeriodeDates resolvePeriod(Periode periode) {
        return resolvePeriod(periode, null);
    }

    /**
     * Resol un període igual que {@link #resolvePeriod(Periode)}, però si es proporciona {@code filterBounds}
     * (el període del filtre de capçalera del dashboard, ja resolt) es manté la consistència amb el tipus del
     * període propi del component: s'ancora al final del filtre en lloc de a "avui", i el resultat es limita
     * perquè no surti dels límits del filtre (vegeu {@link #clampToFilterBounds}).
     */
    public static PeriodeDates resolvePeriod(Periode periode, PeriodeDates filterBounds) {
        PeriodeDates result;
        switch (periode.getPeriodeMode()) {
            case PRESET:
                result = resolvePresetPeriod(periode.getPresetPeriode(), periode.getPresetCount(), filterBounds);
                break;
            case RELATIU:
                result = resolveRelativePeriod(
                        periode.getRelatiuPuntReferencia(),
                        periode.getRelatiuCount(),
                        periode.getRelatiueUnitat(),
                        periode.getRelatiuAlineacio(),
                        filterBounds);
                break;
            case ABSOLUT:
                result = resolveAbsolutePeriod(periode);
                break;
            default:
                throw new IllegalArgumentException("Invalid period mode");
        }
        return clampToFilterBounds(result, filterBounds);
    }

    /**
     * Limita un període resolt perquè no surti dels límits del filtre de capçalera del dashboard, si n'hi ha
     * (p. ex. "darrers 30 dies" amb un filtre de només 10 dies passa a ser els 10 dies sencers del filtre).
     * Un {@code filterBounds.start} nul significa que el filtre no imposa cap límit inferior (equivalent a no
     * filtrar per període pel que fa a la data d'inici), així que en aquest cas no es limita l'inici.
     */
    private static PeriodeDates clampToFilterBounds(PeriodeDates dates, PeriodeDates filterBounds) {
        if (filterBounds == null || dates == null) {
            return dates;
        }
        LocalDate start = dates.getStart();
        LocalDate end = dates.getEnd();
        if (filterBounds.getStart() != null && (start == null || start.isBefore(filterBounds.getStart()))) {
            start = filterBounds.getStart();
        }
        if (filterBounds.getEnd() != null && (end == null || end.isAfter(filterBounds.getEnd()))) {
            end = filterBounds.getEnd();
        }
        // Si el període propi del component ni tan sols arriba a intersectar amb el filtre (p. ex. un "darrer
        // any complet" dins d'un filtre curt i llunyà), es limita directament al període sencer del filtre en
        // lloc de retornar un interval invertit (start > end).
        if (start != null && end != null && start.isAfter(end)) {
            start = filterBounds.getStart() != null ? filterBounds.getStart() : end;
            end = filterBounds.getEnd() != null ? filterBounds.getEnd() : end;
        }
        return PeriodeDates.builder().start(start).end(end).build();
    }

    private static PeriodeDates resolvePresetPeriod(PresetPeriode preset, Integer count, PeriodeDates filterBounds) {
        LocalDate today = filterBounds != null ? filterBounds.getEnd() : LocalDate.now();
        // Les variants "completes" (dia/setmana/mes/trimestre/any complet) exclouen per disseny la unitat en
        // curs perquè "avui" encara no ha acabat. Quan el punt de referència ve del filtre de capçalera, el
        // seu extrem final ja es considera tancat (és un límit explícit triat per l'usuari, no "ara mateix"),
        // així que es tracta com si fos "demà" perquè la mateixa resta d'una unitat hi aterri exactament.
        LocalDate completeToday = filterBounds != null ? filterBounds.getEnd().plusDays(1) : LocalDate.now();
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
                return PeriodeDates.builder().start(completeToday.minusDays(1)).end(completeToday.minusDays(1)).build();
            case DARRERA_COMPLETA_SETMANA:
                LocalDate lastWeekEnd = completeToday.minusWeeks(1).with(ChronoField.DAY_OF_WEEK, 7);
                return PeriodeDates.builder().start(lastWeekEnd.minusDays(6)).end(lastWeekEnd).build();
            case DARRER_COMPLET_MES:
                LocalDate lastMonth = completeToday.minusMonths(1);
                return PeriodeDates.builder().start(lastMonth.withDayOfMonth(1))
                        .end(lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())).build();
            case DARRER_COMPLET_TRIMESTRE:
                LocalDate lastQuarter = completeToday.minusMonths(3);
                int quarterStartMonth = ((lastQuarter.getMonthValue() - 1) / 3) * 3 + 1;
                LocalDate quarterStart = LocalDate.of(lastQuarter.getYear(), quarterStartMonth, 1);
                return PeriodeDates.builder().start(quarterStart)
                        .end(quarterStart.plusMonths(3).minusDays(1)).build();
            case DARRER_COMPLET_ANY:
                int lastYear = completeToday.getYear() - 1;
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
                                               PeriodeAlineacio alignment,
                                               PeriodeDates filterBounds) {
        LocalDate referenceToday = filterBounds != null ? filterBounds.getEnd() : LocalDate.now();
        LocalDate end = calculateReferenceDate(anchor, referenceToday);
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

    private static LocalDate calculateReferenceDate(PeriodeAnchor anchor, LocalDate today) {
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

    private static PeriodeDates resolveAbsolutePeriod(Periode periode) {
        switch (periode.getAbsolutTipus()) {
            case DATE_RANGE:
                return PeriodeDates.builder()
                        .start(periode.getAbsolutDataInici())
                        .end(periode.getAbsolutDataFi() != null ? periode.getAbsolutDataFi() : LocalDate.now())
                        .build();
            case SPECIFIC_PERIOD_OF_YEAR:
                int year = calculateYear(periode.getAbsolutAnyReferencia(), periode.getAbsolutAnyValor());
                int firstPeriod = periode.getAbsolutPeriodeInici() != null
                        ? periode.getAbsolutPeriodeInici()
                        : 1;
                int lastPeriod = periode.getAbsolutPeriodeFi() != null
                        ? periode.getAbsolutPeriodeFi()
                        : firstPeriod;

                LocalDate startDate, endDate;
                switch (periode.getAbsolutPeriodeUnitat()) {
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

    /**
     * Calcula el període previ basat on el període actual.
     * 
     * @param periode Període actual
     * @param currentPeriod Dates del període actual
     * @return The previous period dates
     */
    public static PeriodeDates resolvePreviousPeriod(Periode periode, PeriodeDates currentPeriod) {
        LocalDate currentStart = currentPeriod.getStart();
        LocalDate currentEnd = currentPeriod.getEnd();

        // Calculate the duration of the current period
        long days = java.time.temporal.ChronoUnit.DAYS.between(currentStart, currentEnd) + 1;

        switch (periode.getPeriodeMode()) {
            case PRESET:
                // For preset periods, we shift the period back by its duration
                return PeriodeDates.builder()
                        .start(currentStart.minusDays(days))
                        .end(currentEnd.minusDays(days))
                        .build();
            case RELATIU:
                // For relative periods, we double the count
                Periode previousPeriode = Periode.builder()
                        .periodeMode(periode.getPeriodeMode())
                        .relatiuPuntReferencia(periode.getRelatiuPuntReferencia())
                        .relatiuCount(periode.getRelatiuCount() * 2)
                        .relatiueUnitat(periode.getRelatiueUnitat())
                        .relatiuAlineacio(periode.getRelatiuAlineacio())
                        .build();
                PeriodeDates extendedPeriod = resolvePeriod(previousPeriode);
                return PeriodeDates.builder()
                        .start(extendedPeriod.getStart())
                        .end(currentStart.minusDays(1))
                        .build();
            case ABSOLUT:
                switch (periode.getAbsolutTipus()) {
                    case DATE_RANGE:
                        // For date ranges, we shift the period back by its duration
                        return PeriodeDates.builder()
                                .start(currentStart.minusDays(days))
                                .end(currentEnd.minusDays(days))
                                .build();
                    case SPECIFIC_PERIOD_OF_YEAR:
                        // For specific periods of a year, we go back one period in the same year
                        // or to the last period of the previous year
                        int year = calculateYear(periode.getAbsolutAnyReferencia(), periode.getAbsolutAnyValor());
                        int firstPeriod = periode.getAbsolutPeriodeInici() != null
                                ? periode.getAbsolutPeriodeInici()
                                : 1;
                        int lastPeriod = periode.getAbsolutPeriodeFi() != null
                                ? periode.getAbsolutPeriodeFi()
                                : firstPeriod;

                        // If we're at the first period, go to the last period of the previous year
                        if (firstPeriod == 1) {
                            int previousYear = year - 1;
                            int maxPeriod;
                            switch (periode.getAbsolutPeriodeUnitat()) {
                                case SETMANA:
                                    maxPeriod = 52; // Approximate, could be 53 in some years
                                    break;
                                case MES:
                                    maxPeriod = 12;
                                    break;
                                case TRIMESTRE:
                                    maxPeriod = 4;
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unsupported period unit for specific year period");
                            }

                            Periode previousYearPeriode = Periode.builder()
                                    .periodeMode(PeriodeMode.ABSOLUT)
                                    .absolutTipus(periode.getAbsolutTipus())
                                    .absolutAnyReferencia(PeriodeEspecificAny.SPECIFIC_YEAR)
                                    .absolutAnyValor(previousYear)
                                    .absolutPeriodeUnitat(periode.getAbsolutPeriodeUnitat())
                                    .absolutPeriodeInici(maxPeriod - (lastPeriod - firstPeriod))
                                    .absolutPeriodeFi(maxPeriod)
                                    .build();
                            return resolvePeriod(previousYearPeriode);
                        } else {
                            // Otherwise, go to the previous period in the same year
                            int previousFirstPeriod = firstPeriod - 1;
                            int previousLastPeriod = lastPeriod - 1;

                            Periode previousPeriodePeriode = Periode.builder()
                                    .periodeMode(PeriodeMode.ABSOLUT)
                                    .absolutTipus(periode.getAbsolutTipus())
                                    .absolutAnyReferencia(periode.getAbsolutAnyReferencia())
                                    .absolutAnyValor(periode.getAbsolutAnyValor())
                                    .absolutPeriodeUnitat(periode.getAbsolutPeriodeUnitat())
                                    .absolutPeriodeInici(previousFirstPeriod)
                                    .absolutPeriodeFi(previousLastPeriod)
                                    .build();
                            return resolvePeriod(previousPeriodePeriode);
                        }
                    default:
                        throw new IllegalArgumentException("Unsupported absolute period type");
                }
            default:
                throw new IllegalArgumentException("Invalid period mode");
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
