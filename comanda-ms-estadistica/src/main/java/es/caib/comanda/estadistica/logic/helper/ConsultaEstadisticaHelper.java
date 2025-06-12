package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.helper.PeriodeResolverHelper.PeriodeDates;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula;
import es.caib.comanda.estadistica.logic.intf.model.consulta.DadesComunsWidgetConsulta;
import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetGraficItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetSimpleItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetTaulaItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.ms.estadistica.model.Format;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum.*;

/**
 * Lògica comuna per a obtenir i consultar informació estadística de les apps.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConsultaEstadisticaHelper {

    private final FetRepository fetRepository;

    private final AtributsVisualsHelper atributsVisualsHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;

    private final RestTemplate restTemplate;


    // CONSULTA ESTADISTIQUES
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Recupera una llista de fets estadístics corresponents a un entorn d'aplicació específic i a un període de dates especificat.
     * Els fets estadístics inclouen informació agregada relativa a dimensions, indicadors i temps associats.
     *
     * @param entornAppId Identificador de l'entorn d'aplicació pel qual es volen recuperar els fets estadístics.
     * @param dataInici Data d'inici del període pel qual es volen recuperar els fets estadístics.
     * @param dataFi Data de finalització del període pel qual es volen recuperar els fets estadístics.
     * @return Una llista d'objectes {@link Fet} que representen els fets estadístics associats al període i entorn especificats.
     */
    @Transactional(readOnly = true)
    public List<Fet> getEstadistiquesPeriode(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi) {

        // Get statistics directly from the database using JSON_VALUE and JSON_TABLE
        List<FetEntity> fets = fetRepository.findByEntornAppIdAndTempsDataBetween(
                entornAppId,
                dataInici,
                dataFi);
//                nivellAgrupacio.name());

        // Convert to DTOs
        return toFets(fets);
    }

    /**
     * Recupera una llista de fets estadístics corresponents a un entorn d'aplicació específic i a un període de dates especificat,
     * tenint en compte un conjunt de dimensions filtrades. Si no es proporcionen filtres de dimensions, es fa una crida a la funcionalitat
     * estàndard que no té en compte dimensions (mètode {@link #getEstadistiquesPeriode}).
     *
     * @param entornAppId Identificador de l'entorn d'aplicació pel qual es volen recuperar els fets estadístics.
     * @param dataInici Data d'inici del període pel qual es volen recuperar els fets estadístics.
     * @param dataFi Data de finalització del període pel qual es volen recuperar els fets estadístics.
     * @param dimensionsFiltre Un mapa que conté les dimensions a filtrar, on la clau és el nom de la dimensió i el valor és
     *                         una llista de valors que s'han de considerar per aquesta dimensió.
     * @return Una llista d'objectes {@link Fet} que representen els fets estadístics associats al període, l'entorn i
     *         les dimensions especificades en els filtres.
     */
    @Transactional(readOnly = true)
    public List<Fet> getEstadistiquesPeriodeAmbDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre) {

        // If no dimensions filter is provided, use the standard method
        if (dimensionsFiltre == null || dimensionsFiltre.isEmpty()) {
            return getEstadistiquesPeriode(entornAppId, dataInici, dataFi);
        }

        // Get statistics directly from the database using JSON_VALUE and JSON_TABLE
        List<FetEntity> fets = fetRepository.findByEntornAppIdAndTempsDataBetweenAndDimensions(
                entornAppId,
                dataInici,
                dataFi,
                dimensionsFiltre);
//                nivellAgrupacio.name());

        // Convert to DTOs
        return toFets(fets);
    }


    @Transactional(readOnly = true)
    public InformeWidgetItem getDadesWidget(DashboardItemEntity dashboardItem) {

        WidgetTipus tipus = determineWidgetType(dashboardItem);
        DadesComunsWidgetConsulta dadesComunsConsulta = getDadesComunsConsulta(dashboardItem);

        switch (tipus) {
            case SIMPLE: return getDadesWidgetSimple(dashboardItem, dadesComunsConsulta);
            case GRAFIC: return getDadesWidgetGrafic(dashboardItem, dadesComunsConsulta);
            case TAULA: return getDadesWidgetTaula(dashboardItem, dadesComunsConsulta);
        }

        throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId(), null, "Tipus de widget incorrecte");
    }

    private InformeWidgetItem getDadesWidgetSimple(DashboardItemEntity dashboardItem, DadesComunsWidgetConsulta dadesComunsConsulta) {

        EstadisticaSimpleWidgetEntity widget = (EstadisticaSimpleWidgetEntity)dashboardItem.getWidget();
        TableColumnsEnum agregacio = widget.getIndicadorInfo().getAgregacio();
        Format format = widget.getIndicadorInfo().getIndicador().getFormat();
        boolean compararPeriodeAnterior = widget.isCompararPeriodeAnterior() && !TableColumnsEnum.FIRST_SEEN.equals(agregacio) && !TableColumnsEnum.LAST_SEEN.equals(agregacio);
        String valorConsulta = calculateValorSimple(widget, dadesComunsConsulta.getPeriodeDates(), dadesComunsConsulta.getEntornAppId());
        PeriodeDates periodePrevi = compararPeriodeAnterior
                ? PeriodeResolverHelper.resolvePreviousPeriod(widget.getPeriode(), dadesComunsConsulta.getPeriodeDates())
                : null;
        String valorConsultaPrevia = compararPeriodeAnterior
                ? calculateCanviPercentual(widget, valorConsulta, periodePrevi, dadesComunsConsulta.getEntornAppId())
                : null;

        return InformeWidgetSimpleItem.builder()
                .dashboardItemId(dashboardItem.getId())
                .tipus(WidgetTipus.SIMPLE)
                .titol(widget.getTitol())
                .valor(valorConsulta)
                .unitat(widget.getUnitat())
                .descripcio(widget.getDescripcio())
                .canviPercentual(valorConsultaPrevia)
                .atributsVisuals((AtributsVisualsSimple) dadesComunsConsulta.getAtributsVisuals())
                .posX(dashboardItem.getPosX())
                .posY(dashboardItem.getPosY())
                .width(dashboardItem.getWidth())
                .height(dashboardItem.getHeight())
                .build();
    }

    private InformeWidgetItem getDadesWidgetGrafic(DashboardItemEntity dashboardItem, DadesComunsWidgetConsulta dadesComunsConsulta) {

        EstadisticaGraficWidgetEntity widget = (EstadisticaGraficWidgetEntity)dashboardItem.getWidget();
        PeriodeUnitat tempsAgrupacio = widget.getTempsAgrupacio();
        // Mapa de dimensions per filtrar la consulta
        Map<String, List<String>> dimensionsFiltre = widget.getDimensionsValor() != null && !widget.getDimensionsValor().isEmpty()
                ? createDimensionsFiltre(widget.getDimensionsValor())
                : new HashMap<>();


        if (UN_INDICADOR.equals(widget.getTipusDades()) || UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades()) || DOS_INDICADORS.equals(widget.getTipusDades())) {

            IndicadorTaulaEntity indicadorInfo = widget.getIndicadorInfo();
            IndicadorAgregacio indicadorAgregacio = indicadorInfo != null ?
                    IndicadorAgregacio.builder()
                            .indicadorCodi(indicadorInfo.getIndicador().getCodi())
                            .agregacio(indicadorInfo.getAgregacio())
                            .unitatAgregacio(indicadorInfo.getUnitatAgregacio())
                            .build()
                    : null;

            if (UN_INDICADOR.equals(widget.getTipusDades())) {

            } else if (UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades())) {

                String descomposicioDimensioCodi = widget.getDescomposicioDimensio() != null ? widget.getDescomposicioDimensio().getCodi() : null;
                Boolean agruparPerDimensioDescomposicio = widget.getAgruparPerDimensioDescomposicio();

            } else if (DOS_INDICADORS.equals(widget.getTipusDades())) {
                throw new NotImplementedException("La configuració de 2 indicadors encara no ha estat implementada");
            }
        } else if (VARIS_INDICADORS.equals(widget.getTipusDades())) {
                List<IndicadorAgregacio> indicadorsAgregacio = widget.getIndicadorsInfo().stream()
                        .map(columna -> IndicadorAgregacio.builder()
                                .indicadorCodi(columna.getIndicador().getCodi())
                                .agregacio(columna.getAgregacio())
                                .unitatAgregacio(columna.getUnitatAgregacio())
                                .build())
                        .collect(Collectors.toList());
//                List<Map<String, String>> columnes = new ArrayList<>();
//                columnes.add(Map.of("id", "agrupacio", "label", widget.getTitolAgrupament()));
//                IntStream.range(0, widget.getIndicadorsInfo().size()).forEach(index -> {
//                    var columna = widget.getIndicadorsInfo().get(index);
//                    columnes.add(Map.of("id", "col" + index, "label", columna.getTitol()));
//                });

        } else {
            throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId(), null, "Tipus de dades incorrecte");
        }

        return InformeWidgetGraficItem.builder()
                .dashboardItemId(dashboardItem.getId())
                .tipus(WidgetTipus.GRAFIC)
                .titol(widget.getTitol())
                .llegendaX(widget.getLlegendaX())
//                .llegendaY(widget.getLlegendaY())
                .tipusGrafic(widget.getTipusGrafic())
                .tipusDades(widget.getTipusDades())
                // TODO:
                .labels(new ArrayList<>())
                .series(new ArrayList<>())
                .dimensionValues(new ArrayList<>())

                .atributsVisuals((AtributsVisualsGrafic) dadesComunsConsulta.getAtributsVisuals())
                .posX(dashboardItem.getPosX())
                .posY(dashboardItem.getPosY())
                .width(dashboardItem.getWidth())
                .height(dashboardItem.getHeight())
                .build();
    }

    private InformeWidgetItem getDadesWidgetTaula(DashboardItemEntity dashboardItem, DadesComunsWidgetConsulta dadesComunsConsulta) {
        EstadisticaTaulaWidgetEntity widget = (EstadisticaTaulaWidgetEntity)dashboardItem.getWidget();
        // Mapa de dimensions per filtrar la consulta
        Map<String, List<String>> dimensionsFiltre = widget.getDimensionsValor() != null && !widget.getDimensionsValor().isEmpty()
                ? createDimensionsFiltre(widget.getDimensionsValor())
                : new HashMap<>();
        // Indicadors a calcular
        List<IndicadorAgregacio> indicadorsAgregacio = widget.getColumnes().stream()
                .map(columna -> IndicadorAgregacio.builder()
                        .indicadorCodi(columna.getIndicador().getCodi())
                        .agregacio(columna.getAgregacio())
                        .unitatAgregacio(columna.getUnitatAgregacio())
                        .build())
                .collect(Collectors.toList());
        // Dimensió utilitzada per agrupar
        String dimensioAgrupacioCodi = widget.getDimensioAgrupacio() != null ? widget.getDimensioAgrupacio().getCodi() : null;

        List<Map<String, String>> columnes = new ArrayList<>();
        columnes.add(Map.of("id", "agrupacio", "label", widget.getTitolAgrupament()));
        IntStream.range(0, widget.getColumnes().size()).forEach(index -> {
            var columna = widget.getColumnes().get(index);
            columnes.add(Map.of("id", "col" + index, "label", columna.getTitol()));
        });

        List<Map<String, String>> files = fetRepository.getValorsTaulaAgregat(
                dadesComunsConsulta.getEntornAppId(),
                dadesComunsConsulta.getPeriodeDates().getStart(),
                dadesComunsConsulta.getPeriodeDates().getEnd(),
                dimensionsFiltre,
                indicadorsAgregacio,
                dimensioAgrupacioCodi);

        return InformeWidgetTaulaItem.builder()
                .dashboardItemId(dashboardItem.getId())
                .tipus(WidgetTipus.TAULA)
                .titol(widget.getTitol())
                .titolAgrupament(widget.getDimensioAgrupacio().getDescripcio())
                .columns(columnes)
                .rows(files)
                .atributsVisuals((AtributsVisualsTaula) dadesComunsConsulta.getAtributsVisuals())
                .posX(dashboardItem.getPosX())
                .posY(dashboardItem.getPosY())
                .width(dashboardItem.getWidth())
                .height(dashboardItem.getHeight())
                .build();
    }

    public static String[] getColumnNames(List indicadorsList) {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("agrupacio");

        for (int i = 1; i <= indicadorsList.size(); i++) {
            columnNames.add("col" + i);
        }
        return columnNames.toArray(new String[columnNames.size()]);
    }


    private String calculateValorSimple(EstadisticaSimpleWidgetEntity widget, PeriodeDates periodeConsulta, Long entornAppId) {
        if (periodeConsulta == null || periodeConsulta.start == null || periodeConsulta.end == null) {
            return null;
        }

        // Codi de l'indicator
        String indicadorCodi = widget.getIndicadorInfo().getIndicador().getCodi();

        // Tipus d'agregació a aplicacar a l'indicador
        TableColumnsEnum agregacio = widget.getIndicadorInfo().getAgregacio();
        PeriodeUnitat unitatAgregacio = widget.getIndicadorInfo().getUnitatAgregacio();

        IndicadorAgregacio indicadorAgregacio = IndicadorAgregacio.builder()
                .indicadorCodi(indicadorCodi)
                .agregacio(agregacio)
                .unitatAgregacio(unitatAgregacio)
                .build();

        // Mapa de dimensions per filtrar la consulta
        Map<String, List<String>> dimensionsFiltre = widget.getDimensionsValor() != null && !widget.getDimensionsValor().isEmpty()
                ? createDimensionsFiltre(widget.getDimensionsValor())
                : new HashMap<>();


        // Get the aggregated value directly from the database
        return fetRepository.getValorSimpleAgregat(
                entornAppId,
                periodeConsulta.start,
                periodeConsulta.end,
                dimensionsFiltre,
                indicadorAgregacio);
    }

    private String calculateCanviPercentual(EstadisticaSimpleWidgetEntity widget, String valorConsulta, PeriodeDates periodePrevi, Long entornAppId) {
        if (!widget.isCompararPeriodeAnterior()
                || periodePrevi == null || periodePrevi.start == null || periodePrevi.end == null) {
            return null;
        }
        Double resultatActual = stringToDouble(valorConsulta);
        if (resultatActual == null) {
            return null;
        }

        // Calcula el valor pel període previ
        String valorConsultaPrevia = calculateValorSimple(widget, periodePrevi, entornAppId);
        Double resultatPrevi = stringToDouble(valorConsultaPrevia);
        if (resultatPrevi == null) {
            return null;
        }

        // Calcula el percentage
        return String.format("%.2f", getPercentatgeComparacio(resultatPrevi.doubleValue(), resultatActual.doubleValue()));
    }
    
    

//    public List<InformeWidgetItem> generateDataWidgetGrafic(DashboardItemEntity dashboardItem) {
//
//        EstadisticaGraficWidgetEntity widget = (EstadisticaGraficWidgetEntity)dashboardItem.getWidget();
//
//        Periode periode = widget.getPeriode();
//        PeriodeDates periodeConsulta = PeriodeResolverHelper.resolvePeriod(periode);
//
//        // Get the indicator code
//        String indicadorCodi = widget.getIndicador().getIndicador().getCodi();
//
//        // Get the time unit for grouping
//        PeriodeUnitat tempsAgrupacio = widget.getTempsAgrupacio();
//
//        // Mapa de dimensions per filtrar la consulta
//        Map<String, List<String>> dimensionsFiltre = new HashMap<>();
//        if (widget.getDimensionsValor() != null && !widget.getDimensionsValor().isEmpty()) {
//            for (DimensioValorEntity dimensioValor : widget.getDimensionsValor()) {
//                String dimensioCodi = dimensioValor.getDimensio().getCodi();
//                String valor = dimensioValor.getValor();
//
//                if (!dimensionsFiltre.containsKey(dimensioCodi)) {
//                    dimensionsFiltre.put(dimensioCodi, new ArrayList<>());
//                }
//                dimensionsFiltre.get(dimensioCodi).add(valor);
//            }
//        }
//
//        // Get the statistics for the period and dimensions
//        List<Fet> fets = estadisticaHelper.getEstadistiquesPeriodeAmbDimensions(
//                widget.getAppId(),
//                periodeConsulta.start,
//                periodeConsulta.end,
//                dimensionsFiltre);
//
//        if (fets == null || fets.isEmpty()) {
//            // Return empty chart if no data
//            InformeWidgetGraficItem item = InformeWidgetGraficItem.builder()
//                    .tipus(WidgetTipus.GRAFIC)
//                    .titol(widget.getTitol())
//                    .tipusGrafic(widget.getTipusGrafic())
//                    .tipusValors(widget.getTipusValors())
//                    .llegendaX(widget.getLlegendaX())
//                    .llegendaY(widget.getLlegendaY())
//                    .labels(new ArrayList<>())
//                    .series(new ArrayList<>())
//                    .build();
//            return List.of(item);
//        }
//
//        // Check if we need to decompose by dimension
//        boolean hasDecomposition = widget.getDescomposicioDimensio() != null;
//        String decompositionDimensionCode = hasDecomposition ? widget.getDescomposicioDimensio().getCodi() : null;
//
//        // Group data by time and dimension (if decomposition is enabled)
//        Map<String, Map<String, Double>> dataByTimeAndDimension = new HashMap<>();
//        Set<String> dimensionValues = new HashSet<>();
//        List<String> timeLabels = new ArrayList<>();
//
//        // Process each record
//        for (Fet fet : fets) {
//            // Get the time value formatted according to the time unit
//            String timeLabel = formatTimeLabel(fet.getTemps().getData(), tempsAgrupacio);
//
//            // Add to time labels if not already present
//            if (!timeLabels.contains(timeLabel)) {
//                timeLabels.add(timeLabel);
//            }
//
//            // Get the dimension value if decomposition is enabled
//            String dimensionValue = hasDecomposition ? fet.getDimensionsJson().get(decompositionDimensionCode) : "default";
//            if (dimensionValue == null) {
//                dimensionValue = "default";
//            }
//
//            // Add to dimension values if not already present
//            if (hasDecomposition && !dimensionValues.contains(dimensionValue)) {
//                dimensionValues.add(dimensionValue);
//            }
//
//            // Create time entry if it doesn't exist
//            if (!dataByTimeAndDimension.containsKey(timeLabel)) {
//                dataByTimeAndDimension.put(timeLabel, new HashMap<>());
//            }
//
//            // Get the indicator value
//            if (fet.getIndicadorsJson().containsKey(indicadorCodi)) {
//                double value = fet.getIndicadorsJson().get(indicadorCodi);
//
//                // Add or aggregate the value
//                Map<String, Double> dimensionData = dataByTimeAndDimension.get(timeLabel);
//                double currentValue = dimensionData.getOrDefault(dimensionValue, 0.0);
//                dimensionData.put(dimensionValue, currentValue + value);
//            }
//        }
//
//        // Sort time labels chronologically
//        Collections.sort(timeLabels);
//
//        // Prepare series data
//        List<Map<String, Object>> series = new ArrayList<>();
//
//        if (hasDecomposition) {
//            // Create a series for each dimension value
//            List<String> sortedDimensionValues = new ArrayList<>(dimensionValues);
//            Collections.sort(sortedDimensionValues);
//
//            for (String dimensionValue : sortedDimensionValues) {
//                Map<String, Object> seriesItem = new HashMap<>();
//                seriesItem.put("name", dimensionValue);
//
//                List<Double> data = new ArrayList<>();
//                for (String timeLabel : timeLabels) {
//                    Map<String, Double> dimensionData = dataByTimeAndDimension.get(timeLabel);
//                    double value = dimensionData != null ? dimensionData.getOrDefault(dimensionValue, 0.0) : 0.0;
//                    data.add(value);
//                }
//
//                seriesItem.put("data", data);
//                series.add(seriesItem);
//            }
//        } else {
//            // Create a single series
//            Map<String, Object> seriesItem = new HashMap<>();
//            seriesItem.put("name", indicadorCodi);
//
//            List<Double> data = new ArrayList<>();
//            for (String timeLabel : timeLabels) {
//                Map<String, Double> dimensionData = dataByTimeAndDimension.get(timeLabel);
//                double value = dimensionData != null ? dimensionData.getOrDefault("default", 0.0) : 0.0;
//                data.add(value);
//            }
//
//            seriesItem.put("data", data);
//            series.add(seriesItem);
//        }
//
//        // Calculate percentages if needed
//        if (widget.getTipusValors() == GraficValueTypeEnum.PERCENTAGE) {
//            // For each time point, calculate the total and convert values to percentages
//            for (int i = 0; i < timeLabels.size(); i++) {
//                double total = 0.0;
//
//                // Calculate total for this time point
//                for (Map<String, Object> seriesItem : series) {
//                    List<Double> data = (List<Double>) seriesItem.get("data");
//                    total += data.get(i);
//                }
//
//                // Convert to percentages
//                if (total > 0) {
//                    for (Map<String, Object> seriesItem : series) {
//                        List<Double> data = (List<Double>) seriesItem.get("data");
//                        data.set(i, (data.get(i) / total) * 100);
//                    }
//                }
//            }
//        }
//
//        AtributsVisualsGrafic atributsVisuals = (AtributsVisualsGrafic) resolveAtributsVisuals(dashboardItem);
//
//        // Create the chart item
//        InformeWidgetGraficItem item = InformeWidgetGraficItem.builder()
//                .tipus(WidgetTipus.GRAFIC)
//                .titol(widget.getTitol())
//                .tipusGrafic(widget.getTipusGrafic())
//                .tipusValors(widget.getTipusValors())
//                .llegendaX(widget.getLlegendaX())
//                .llegendaY(widget.getLlegendaY())
//                .labels(timeLabels)
//                .series(series)
//                .dimensionValues(hasDecomposition ? new ArrayList<>(dimensionValues) : null)
//                .atributsVisuals(atributsVisuals)
//                .build();
//
//        return List.of(item);
//    }
//
//    private String formatTimeLabel(LocalDate date, PeriodeUnitat unit) {
//        switch (unit) {
//            case DIA:
//                return date.toString();
//            case SETMANA:
//                return "Week " + date.get(WeekFields.of(Locale.getDefault()).weekOfYear());
//            case MES:
//                return date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + date.getYear();
//            case TRIMESTRE:
//                int quarter = (date.getMonthValue() - 1) / 3 + 1;
//                return "Q" + quarter + " " + date.getYear();
//            case ANY:
//                return String.valueOf(date.getYear());
//            default:
//                return date.toString();
//        }
//    }

    private WidgetTipus determineWidgetType(DashboardItemEntity dashboardItem) throws ReportGenerationException {
        EstadisticaWidgetEntity widget = dashboardItem.getWidget();
        if (widget instanceof EstadisticaSimpleWidgetEntity) {
            return WidgetTipus.SIMPLE;
        } else if (widget instanceof EstadisticaGraficWidgetEntity) {
            return WidgetTipus.GRAFIC;
        } else if (widget instanceof EstadisticaTaulaWidgetEntity) {
            return WidgetTipus.TAULA;
        }
        throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId(), null, "Tipus de widget incorrecte");
    }

    private DadesComunsWidgetConsulta getDadesComunsConsulta(DashboardItemEntity dashboardItem) {
        EstadisticaWidgetEntity widget = dashboardItem.getWidget();
        Long entornAppId = estadisticaClientHelper.entornAppFindByAppAndEntorn(widget.getAppId(), dashboardItem.getEntornId()).getId();
        PeriodeDates periodeDates = PeriodeResolverHelper.resolvePeriod(widget.getPeriode());
        AtributsVisuals atributsVisuals = resolveAtributsVisuals(dashboardItem);

        return DadesComunsWidgetConsulta.builder()
                .entornAppId(entornAppId)
                .periodeDates(periodeDates)
                .atributsVisuals(atributsVisuals)
                .build();
    }

    private AtributsVisuals resolveAtributsVisuals(DashboardItemEntity dashboardItem) {
        var atributsVisualsWidget = atributsVisualsHelper.getAtributsVisuals(dashboardItem.getWidget());
        var atributsVisualsDash = atributsVisualsHelper.getAtributsVisuals(dashboardItem);
        if (atributsVisualsWidget != null && atributsVisualsDash != null) {
            return atributsVisualsDash.merge(atributsVisualsWidget);
        }
        return atributsVisualsDash != null ? atributsVisualsDash : atributsVisualsWidget;
    }

    private Map<String, List<String>> createDimensionsFiltre(List<DimensioValorEntity> dimensioValors) {
        Map<String, List<String>> dimensionFilters = new HashMap<>();
        for (DimensioValorEntity dimensioValor : dimensioValors) {
            String dimensionCode = dimensioValor.getDimensio().getCodi();
            String value = dimensioValor.getValor();
            dimensionFilters.computeIfAbsent(dimensionCode, k -> new ArrayList<>()).add(value);
        }
        return dimensionFilters;
    }

    private Double stringToDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Error converting string to double: {}", value);
            return null;
        }
    }

    private double getPercentatgeComparacio(double valor1, double valor2) {
        if (isZero(valor1)) { // Comprova si valorPrevi és pràcticament 0
            return isZero(valor2) ? 0 : Math.signum(valor2) * 100;
        } else {
            return ((valor2 - valor1) / Math.abs(valor1)) * 100;
        }
    }

    private static boolean isZero(double valor1) {
        final double EPSILON = 1e-9; // Llindar per comparacions
        return Math.abs(valor1) < EPSILON;
    }

    private Fet toFet(FetEntity fetEntity) {
        return Fet.builder()
                .entornAppId(fetEntity.getEntornAppId())
                .temps(Temps.builder().data(fetEntity.getTemps().getData()).build())
                .dimensionsJson(fetEntity.getDimensionsJson())
                .indicadorsJson(fetEntity.getIndicadorsJson())
                .build();
    }
    private List<Fet> toFets(List<FetEntity> fetEntities) {
        return fetEntities.stream().
                map(this::toFet).
                collect(Collectors.toList());
    }

}
