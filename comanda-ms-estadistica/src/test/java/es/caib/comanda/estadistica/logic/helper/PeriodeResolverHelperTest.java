package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.periode.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PeriodeResolverHelperTest {

    private final LocalDate today = LocalDate.now();

    // ========================================================================
    // 1. TESTOS PARAMETritzats PER A PRESET PERIODS
    // ========================================================================

    @ParameterizedTest
    @MethodSource("provideExactRollingPresetPeriods")
    @DisplayName("resolvePeriod: resol correctament períodes rolling sense aproximacions de dies")
    void resolvePeriod_quanPresetRolling_llavorsCalculaDatesExactes(PresetPeriode preset, Function<LocalDate, LocalDate> expectedStartCalc) {
        // Arrange
        Periode periode = Periode.builder().periodeMode(PeriodeMode.PRESET).presetPeriode(preset).build();

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePeriod(periode);

        // Assert: Comparem la data exacta, no un nombre aproximat de dies
        assertThat(result.start).isEqualTo(expectedStartCalc.apply(today));
        assertThat(result.end).isEqualTo(today);
    }

    private static Stream<Arguments> provideExactRollingPresetPeriods() {
        return Stream.of(
            arguments(PresetPeriode.DARRERS_7_DIES, (Function<LocalDate, LocalDate>) d -> d.minusDays(7)),
            arguments(PresetPeriode.DARRERS_14_DIES, (Function<LocalDate, LocalDate>) d -> d.minusDays(14)),
            arguments(PresetPeriode.DARRERS_30_DIES, (Function<LocalDate, LocalDate>) d -> d.minusDays(30)),
            arguments(PresetPeriode.DARRERS_90_DIES, (Function<LocalDate, LocalDate>) d -> d.minusDays(90)),
            arguments(PresetPeriode.DARRERS_180_DIES, (Function<LocalDate, LocalDate>) d -> d.minusDays(180)),
            arguments(PresetPeriode.DARRERS_365_DIES, (Function<LocalDate, LocalDate>) d -> d.minusDays(365)),
            arguments(PresetPeriode.DARRERES_4_SETMANES, (Function<LocalDate, LocalDate>) d -> d.minusWeeks(4)),
            arguments(PresetPeriode.DARRERES_12_SETMANES, (Function<LocalDate, LocalDate>) d -> d.minusWeeks(12)),
            arguments(PresetPeriode.DARRERES_52_SETMANES, (Function<LocalDate, LocalDate>) d -> d.minusWeeks(52)),
            arguments(PresetPeriode.DARRERS_3_MESOS, (Function<LocalDate, LocalDate>) d -> d.minusMonths(3)),
            arguments(PresetPeriode.DARRERS_6_MESOS, (Function<LocalDate, LocalDate>) d -> d.minusMonths(6)),
            arguments(PresetPeriode.DARRERS_12_MESOS, (Function<LocalDate, LocalDate>) d -> d.minusMonths(12)),
            arguments(PresetPeriode.DARRERS_4_TRIMESTRES, (Function<LocalDate, LocalDate>) d -> d.minusMonths(12)),
            arguments(PresetPeriode.DARRER_1_ANY, (Function<LocalDate, LocalDate>) d -> d.minusYears(1)),
            arguments(PresetPeriode.DARRERS_5_ANYS, (Function<LocalDate, LocalDate>) d -> d.minusYears(5)),
            arguments(PresetPeriode.AVUI, (Function<LocalDate, LocalDate>) d -> d)
        );
    }

    @Test
    @DisplayName("resolvePeriod: DARRER_COMPLET_MES calcula correctament el mes anterior")
    void resolvePeriod_quanDarrerCompletMes_llavorsCalculaPrimerIUltimDiaMesAnterior() {
        // Arrange
        Periode periode = Periode.builder().periodeMode(PeriodeMode.PRESET).presetPeriode(PresetPeriode.DARRER_COMPLET_MES).build();
        LocalDate lastMonth = today.minusMonths(1);

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePeriod(periode);

        // Assert
        assertThat(result.start).isEqualTo(lastMonth.withDayOfMonth(1));
        assertThat(result.end).isEqualTo(lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()));
    }

    @Test
    @DisplayName("resolvePeriod: DARRER_COMPLET_TRIMESTRE calcula correctament el trimestre anterior")
    void resolvePeriod_quanDarrerCompletTrimestre_llavorsCalculaTrimestreAnterior() {
        // Arrange
        Periode periode = Periode.builder().periodeMode(PeriodeMode.PRESET).presetPeriode(PresetPeriode.DARRER_COMPLET_TRIMESTRE).build();
        LocalDate lastQuarterRef = today.minusMonths(3);
        int quarterStartMonth = ((lastQuarterRef.getMonthValue() - 1) / 3) * 3 + 1;
        LocalDate expectedStart = LocalDate.of(lastQuarterRef.getYear(), quarterStartMonth, 1);
        LocalDate expectedEnd = expectedStart.plusMonths(3).minusDays(1);

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePeriod(periode);

        // Assert
        assertThat(result.start).isEqualTo(expectedStart);
        assertThat(result.end).isEqualTo(expectedEnd);
    }

    @Test
    @DisplayName("resolvePeriod: AQUEST_TRIMESTRE_FINS_ARA calcula correctament des de l'inici del trimestre")
    void resolvePeriod_quanAquestTrimestreFinsAra_llavorsCalculaDesIniciTrimestre() {
        // Arrange
        Periode periode = Periode.builder().periodeMode(PeriodeMode.PRESET).presetPeriode(PresetPeriode.AQUEST_TRIMESTRE_FINS_ARA).build();
        int currentQuarterStartMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
        LocalDate expectedStart = LocalDate.of(today.getYear(), currentQuarterStartMonth, 1);

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePeriod(periode);

        // Assert
        assertThat(result.start).isEqualTo(expectedStart);
        assertThat(result.end).isEqualTo(today);
    }

    @ParameterizedTest
    @MethodSource("provideGenericNPresetPeriods")
    @DisplayName("resolvePeriod: resol correctament períodes genèrics N amb càlcul exacte")
    void resolvePeriod_quanPresetGenericN_llavorsCalculaDatesExactes(PresetPeriode preset, int count, Function<LocalDate, LocalDate> expectedStartCalc) {
        // Arrange
        Periode periode = Periode.builder().periodeMode(PeriodeMode.PRESET).presetPeriode(preset).presetCount(count).build();

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePeriod(periode);

        // Assert
        assertThat(result.start).isEqualTo(expectedStartCalc.apply(today));
        assertThat(result.end).isEqualTo(today);
    }

    private static Stream<Arguments> provideGenericNPresetPeriods() {
        return Stream.of(
            arguments(PresetPeriode.DARRERS_N_DIES, 5, (Function<LocalDate, LocalDate>) d -> d.minusDays(5)),
            arguments(PresetPeriode.DARRERES_N_SETMANES, 2, (Function<LocalDate, LocalDate>) d -> d.minusWeeks(2)),
            arguments(PresetPeriode.DARRERS_N_MESOS, 2, (Function<LocalDate, LocalDate>) d -> d.minusMonths(2)),
            arguments(PresetPeriode.DARRERS_N_TRIMESTRES, 1, (Function<LocalDate, LocalDate>) d -> d.minusMonths(3)),
            arguments(PresetPeriode.DARRERS_N_ANYS, 1, (Function<LocalDate, LocalDate>) d -> d.minusYears(1))
        );
    }

    // ========================================================================
    // 2. TESTOS PER A PERÍODES RELATIUS I HELPERS
    // ========================================================================

    @ParameterizedTest
    @EnumSource(value = PeriodeAnchor.class, names = {"ARA", "INICI_DIA", "INICI_SETMANA", "INICI_MES", "INICI_TRIMESTRE", "INICI_ANY"})
    @DisplayName("calculateReferenceDate: calcula correctament la data de referència per a cada anchor")
    void calculateReferenceDate_quanAnchorValid_llavorsCalculaDataCorrecta(PeriodeAnchor anchor) {
        // Arrange
        Periode periode = Periode.builder()
            .periodeMode(PeriodeMode.RELATIU)
            .relatiuPuntReferencia(anchor)
            .relatiuCount(1)
            .relatiueUnitat(PeriodeUnitat.DIA)
            .build();

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePeriod(periode);

        // Assert: Verify the end date matches the anchor logic
        LocalDate expectedEnd;
        switch (anchor) {
            case INICI_SETMANA: expectedEnd = today.with(ChronoField.DAY_OF_WEEK, 1); break;
            case INICI_MES: expectedEnd = today.withDayOfMonth(1); break;
            case INICI_TRIMESTRE:
                int q = (today.getMonthValue() - 1) / 3;
                expectedEnd = LocalDate.of(today.getYear(), q * 3 + 1, 1);
                break;
            case INICI_ANY: expectedEnd = LocalDate.of(today.getYear(), 1, 1); break;
            default: expectedEnd = today; break;
        }
        assertThat(result.end).isEqualTo(expectedEnd);
    }

    @Test
    @DisplayName("alignDate: aplica COMPLETE_UNITS correctament per a SETMANA, MES i TRIMESTRE")
    void alignDate_quanCompleteUnits_llavorsAlineaAlIniciDeLaUnitat() {
        // Arrange
        LocalDate midMonth = LocalDate.of(2023, 5, 15);

        // Act & Assert Setmana
        LocalDate startWeek = PeriodeResolverHelper.resolvePeriod(Periode.builder()
            .periodeMode(PeriodeMode.RELATIU).relatiuPuntReferencia(PeriodeAnchor.ARA)
            .relatiuCount(0).relatiueUnitat(PeriodeUnitat.SETMANA).relatiuAlineacio(PeriodeAlineacio.COMPLETE_UNITS).build()).end;
        assertThat(startWeek.getDayOfWeek().getValue()).isEqualTo(1); // Dilluns

        // Act & Assert Mes
        LocalDate startMonth = PeriodeResolverHelper.resolvePeriod(Periode.builder()
            .periodeMode(PeriodeMode.RELATIU).relatiuPuntReferencia(PeriodeAnchor.ARA)
            .relatiuCount(0).relatiueUnitat(PeriodeUnitat.MES).relatiuAlineacio(PeriodeAlineacio.COMPLETE_UNITS).build()).end;
        assertThat(startMonth.getDayOfMonth()).isEqualTo(1);

        // Act & Assert Trimestre
        LocalDate startQuarter = PeriodeResolverHelper.resolvePeriod(Periode.builder()
            .periodeMode(PeriodeMode.RELATIU).relatiuPuntReferencia(PeriodeAnchor.ARA)
            .relatiuCount(0).relatiueUnitat(PeriodeUnitat.TRIMESTRE).relatiuAlineacio(PeriodeAlineacio.COMPLETE_UNITS).build()).end;
        assertThat(startQuarter.getMonthValue()).isIn(1, 4, 7, 10);
        assertThat(startQuarter.getDayOfMonth()).isEqualTo(1);
    }

    // ========================================================================
    // 3. TESTOS PER A PERÍODES ABSOLUTS
    // ========================================================================

    @Test
    @DisplayName("resolveAbsolutePeriod: DATE_RANGE utilitza LocalDate.now() si dataFi és null")
    void resolveAbsolutePeriod_quanDateRangeSenseDataFi_llavorsUsaAvui() {
        // Arrange
        LocalDate start = LocalDate.of(2023, 1, 1);
        Periode periode = Periode.builder()
            .periodeMode(PeriodeMode.ABSOLUT)
            .absolutTipus(PeriodeAbsolutTipus.DATE_RANGE)
            .absolutDataInici(start)
            .absolutDataFi(null)
            .build();

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePeriod(periode);

        // Assert
        assertThat(result.start).isEqualTo(start);
        assertThat(result.end).isEqualTo(today);
    }

    @Test
    @DisplayName("resolveAbsolutePeriod: SPECIFIC_PERIOD_OF_YEAR calcula correctament per a MES")
    void resolveAbsolutePeriod_quanSpecificPeriodMes_llavorsCalculaDatesCorrectes() {
        // Arrange
        Periode periode = Periode.builder()
            .periodeMode(PeriodeMode.ABSOLUT)
            .absolutTipus(PeriodeAbsolutTipus.SPECIFIC_PERIOD_OF_YEAR)
            .absolutAnyReferencia(PeriodeEspecificAny.CURRENT_YEAR)
            .absolutPeriodeUnitat(PeriodeUnitat.MES)
            .absolutPeriodeInici(3)
            .absolutPeriodeFi(5)
            .build();

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePeriod(periode);

        // Assert
        assertThat(result.start).isEqualTo(LocalDate.of(today.getYear(), 3, 1));
        assertThat(result.end).isEqualTo(LocalDate.of(today.getYear(), 5, 31));
    }

    @Test
    @DisplayName("calculateYear: llança excepció quan és SPECIFIC_YEAR però el valor és null")
    void calculateYear_quanSpecificYearNull_llancaExcepcio() {
        // Arrange
        Periode periode = Periode.builder()
            .periodeMode(PeriodeMode.ABSOLUT)
            .absolutTipus(PeriodeAbsolutTipus.SPECIFIC_PERIOD_OF_YEAR)
            .absolutAnyReferencia(PeriodeEspecificAny.SPECIFIC_YEAR)
            .absolutAnyValor(null)
            .absolutPeriodeUnitat(PeriodeUnitat.MES)
            .build();

        // Act & Assert
        assertThatThrownBy(() -> PeriodeResolverHelper.resolvePeriod(periode))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Specific year value is required");
    }

    // ========================================================================
    // 4. TESTOS PER A resolvePreviousPeriod (Lògica Complexa)
    // ========================================================================

    @Test
    @DisplayName("resolvePreviousPeriod: PRESET desplaça el període enrere per la seva durada")
    void resolvePreviousPeriod_quanPreset_llavorsDesplaçaEnrerePerDurada() {
        // Arrange
        Periode periode = Periode.builder().periodeMode(PeriodeMode.PRESET).presetPeriode(PresetPeriode.DARRERS_7_DIES).build();
        PeriodeResolverHelper.PeriodeDates current = new PeriodeResolverHelper.PeriodeDates(today.minusDays(10), today.minusDays(3));

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePreviousPeriod(periode, current);

        // Assert
        long duration = ChronoUnit.DAYS.between(current.start, current.end) + 1; // 8 days
        assertThat(result.start).isEqualTo(current.start.minusDays(duration));
        assertThat(result.end).isEqualTo(current.end.minusDays(duration));
    }

    @Test
    @DisplayName("resolvePreviousPeriod: RELATIU duplica el count i acaba el dia abans de l'inici actual")
    void resolvePreviousPeriod_quanRelatiu_llavorsDuplicaCountIAcabarDiaAbans() {
        // Arrange
        Periode periode = Periode.builder()
            .periodeMode(PeriodeMode.RELATIU)
            .relatiuPuntReferencia(PeriodeAnchor.ARA)
            .relatiuCount(5)
            .relatiueUnitat(PeriodeUnitat.DIA)
            .build();
        PeriodeResolverHelper.PeriodeDates current = new PeriodeResolverHelper.PeriodeDates(today.minusDays(5), today);

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePreviousPeriod(periode, current);

        // Assert
        assertThat(result.end).isEqualTo(current.start.minusDays(1));
        // El start hauria de ser 10 dies enrere respecte a 'today' (count * 2)
        assertThat(result.start).isEqualTo(today.minusDays(10));
    }

    @Test
    @DisplayName("resolvePreviousPeriod: ABSOLUT SPECIFIC_PERIOD va a l'últim període de l'any anterior si firstPeriod == 1")
    void resolvePreviousPeriod_quanAbsolutFirstPeriod1_llavorsVaAnyAnterior() {
        // Arrange
        Periode periode = Periode.builder()
            .periodeMode(PeriodeMode.ABSOLUT)
            .absolutTipus(PeriodeAbsolutTipus.SPECIFIC_PERIOD_OF_YEAR)
            .absolutAnyReferencia(PeriodeEspecificAny.CURRENT_YEAR)
            .absolutPeriodeUnitat(PeriodeUnitat.TRIMESTRE)
            .absolutPeriodeInici(1)
            .absolutPeriodeFi(1)
            .build();
        LocalDate currentStart = LocalDate.of(today.getYear(), 1, 1);
        LocalDate currentEnd = LocalDate.of(today.getYear(), 3, 31);
        PeriodeResolverHelper.PeriodeDates current = new PeriodeResolverHelper.PeriodeDates(currentStart, currentEnd);

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePreviousPeriod(periode, current);

        // Assert: Hauria de ser el 4t trimestre de l'any anterior
        assertThat(result.start.getYear()).isEqualTo(today.getYear() - 1);
        assertThat(result.start.getMonthValue()).isEqualTo(10);
        assertThat(result.end.getMonthValue()).isEqualTo(12);
    }

    @Test
    @DisplayName("resolvePreviousPeriod: ABSOLUT SPECIFIC_PERIOD va al període anterior del mateix any si firstPeriod > 1")
    void resolvePreviousPeriod_quanAbsolutFirstPeriodMajor1_llavorsVaPeriodeAnteriorMateixAny() {
        // Arrange
        Periode periode = Periode.builder()
            .periodeMode(PeriodeMode.ABSOLUT)
            .absolutTipus(PeriodeAbsolutTipus.SPECIFIC_PERIOD_OF_YEAR)
            .absolutAnyReferencia(PeriodeEspecificAny.CURRENT_YEAR)
            .absolutPeriodeUnitat(PeriodeUnitat.MES)
            .absolutPeriodeInici(3)
            .absolutPeriodeFi(3)
            .build();
        LocalDate currentStart = LocalDate.of(today.getYear(), 3, 1);
        LocalDate currentEnd = LocalDate.of(today.getYear(), 3, 31);
        PeriodeResolverHelper.PeriodeDates current = new PeriodeResolverHelper.PeriodeDates(currentStart, currentEnd);

        // Act
        PeriodeResolverHelper.PeriodeDates result = PeriodeResolverHelper.resolvePreviousPeriod(periode, current);

        // Assert: Hauria de ser el mes 2 (Febrer) del mateix any
        assertThat(result.start.getYear()).isEqualTo(today.getYear());
        assertThat(result.start.getMonthValue()).isEqualTo(2);
        assertThat(result.end.getMonthValue()).isEqualTo(2);
    }
}
