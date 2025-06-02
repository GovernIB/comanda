package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.PeriodeResolverHelper;
import es.caib.comanda.estadistica.logic.helper.PeriodeResolverHelper.PeriodeDates;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.GraficValueTypeEnum;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetGraficItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetSimpleItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetTaulaItem;
import es.caib.comanda.estadistica.logic.intf.model.periode.Periode;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.service.DashboardItemService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.ms.estadistica.model.Format;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Implementació del servei per gestionar la lògica de negoci relacionada amb els dashboards.
 * Aquesta classe extend BaseReadonlyResourceService i implementa la interfície DashboardService.
 *
 * Proporciona funcionalitats específiques per treballar amb el model de dades Dashboard,
 * interactuant amb l'entitat persistent DashboardEntity.
 *
 * Aquesta classe utilitza anotacions de Spring per ser detectada com a servei,
 * i registra logs mitjançant Lombok.
 *
 * @author Límit Tecnologies
 */
    @Slf4j
@Service
public class DashboardItemServiceImpl extends BaseMutableResourceService<DashboardItem, Long, DashboardItemEntity> implements DashboardItemService {

    @Autowired
    private EstadisticaHelper estadisticaHelper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        register(DashboardItem.WIDGET_REPORT, new InformeWidget());
    }

    public class InformeWidget implements ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem> {

        @Override
        public List<InformeWidgetItem> generateData(
                String code,
                DashboardItemEntity entity,
                InformeWidgetParams params) throws ReportGenerationException {

            DashboardItemEntity dashboardItem = getDashboardItem(code, entity);
            WidgetTipus tipus = determineWidgetType(dashboardItem, entity, code);

            return generateDataWidget(dashboardItem, tipus, entity.getEntornId());
        }

        private DashboardItemEntity getDashboardItem(String code, DashboardItemEntity entity) {
            DashboardItemEntity dashboardItem = entityRepository.findById(entity.getId())
                    .orElseThrow(() -> new ReportGenerationException(DashboardItem.class, entity.getId(), code, "No existeix"));
            return dashboardItem;
        }

        private WidgetTipus determineWidgetType(DashboardItemEntity dashboardItem, DashboardItemEntity entity, String code) throws ReportGenerationException {
            Object widget = dashboardItem.getWidget();
            if (widget instanceof EstadisticaSimpleWidgetEntity) {
                return WidgetTipus.SIMPLE;
            } else if (widget instanceof EstadisticaGraficWidgetEntity) {
                return WidgetTipus.GRAFIC;
            } else if (widget instanceof EstadisticaTaulaWidgetEntity) {
                return WidgetTipus.TAULA;
            }
            throw new ReportGenerationException(DashboardItem.class, entity.getId(), code, "Tipus de widget incorrecte");
        }

        public List<InformeWidgetItem> generateDataWidget(DashboardItemEntity dashboardItem, WidgetTipus tipus, Long entornId) {
            EstadisticaWidgetEntity widget = dashboardItem.getWidget();
            switch (tipus) {
                case SIMPLE:
                    return generateDataWidgetSimple(dashboardItem);
                case GRAFIC:
                    return generateDataWidgetGrafic(dashboardItem);
                case TAULA:
                    return generateDataWidgetTaula(dashboardItem);
                default:
                    return List.of();
            }
        }

        public List<InformeWidgetItem> generateDataWidgetSimple(DashboardItemEntity dashboardItem) {

            EstadisticaSimpleWidgetEntity widget = (EstadisticaSimpleWidgetEntity)dashboardItem.getWidget();

            Periode periode = widget.getPeriode();
            PeriodeDates periodeConsulta = PeriodeResolverHelper.resolvePeriod(periode);
            PeriodeDates periodePrevi = widget.isCompararPeriodeAnterior()
                    ? PeriodeResolverHelper.resolvePreviousPeriod(periode, periodeConsulta)
                    : null;
            AtributsVisualsSimple atributsVisuals = resolveAtributsVisualsSimple(dashboardItem, widget);

            Object valorConsulta = calculateValorSimple(widget, periodeConsulta);
            String valorConsultaFormat = valorConsulta != null ? formatValorSimple(valorConsulta, widget.getIndicadorInfo()) : "";
            String valorConsultaPreviaFormat = null;
            if (valorConsulta != null && widget.isCompararPeriodeAnterior() && valorConsulta instanceof Number) {
                Double valorConsultaPrevia = calculateCanviPercentual(widget, valorConsulta, periodePrevi);
                valorConsultaPreviaFormat = valorConsulta != null ? formatPercent(valorConsultaPrevia) : null;
            }

            InformeWidgetSimpleItem item = InformeWidgetSimpleItem.builder()
                    .tipus(WidgetTipus.SIMPLE)
                    .titol(widget.getTitol())
                    .valor(valorConsultaFormat)
                    .unitat(widget.getUnitat())
                    .descripcio(widget.getDescripcio())
                    .canviPercentual(valorConsultaPreviaFormat)
                    .atributsVisuals(atributsVisuals)
                    .build();

            return List.of(item);
        }

        private AtributsVisualsSimple resolveAtributsVisualsSimple(DashboardItemEntity dashboardItem, EstadisticaSimpleWidgetEntity widget) {
            var atributsVisualsWidget = AtributsVisualsSimple.toAtributsVisuals(widget.getAtributsVisuals());
            var atributsVisualsDash = AtributsVisualsSimple.toAtributsVisuals(dashboardItem.getAtributsVisuals());
            if (atributsVisualsWidget != null && atributsVisualsDash != null) {
                return (AtributsVisualsSimple) atributsVisualsDash.merge(atributsVisualsWidget);
            }
            return atributsVisualsDash != null ? atributsVisualsDash : atributsVisualsWidget;
        }


        private Object calculateValorSimple(EstadisticaSimpleWidgetEntity widget, PeriodeDates periodeConsulta) {
            if (periodeConsulta == null || periodeConsulta.start == null || periodeConsulta.end == null) {
                return null;
            }

            // Codi de l'indicator
            String indicadorCodi = widget.getIndicadorInfo().getIndicador().getCodi();

            // Tipus d'agregació a aplicacar a l'indicador
            TableColumnsEnum agregacio = widget.getIndicadorInfo().getAgregacio();
            PeriodeUnitat unitatAgregacio = widget.getIndicadorInfo().getUnitatAgregacio();

            // Mapa de dimensions per filtrar la consulta
            Map<String, List<String>> dimensionsFiltre = new HashMap<>();
            if (widget.getDimensionsValor() != null && !widget.getDimensionsValor().isEmpty()) {
                for (DimensioValorEntity dimensioValor : widget.getDimensionsValor()) {
                    String dimensioCodi = dimensioValor.getDimensio().getCodi();
                    String valor = dimensioValor.getValor();

                    if (!dimensionsFiltre.containsKey(dimensioCodi)) {
                        dimensionsFiltre.put(dimensioCodi, new ArrayList<>());
                    }
                    dimensionsFiltre.get(dimensioCodi).add(valor);
                }
            }

            // Get the aggregated value directly from the database
            return estadisticaHelper.getValorAgregatPeriodeAmbDimensions(
                    widget.getAppId(),
                    periodeConsulta.start,
                    periodeConsulta.end,
                    dimensionsFiltre,
                    indicadorCodi,
                    agregacio,
                    unitatAgregacio);
        }

        private Double calculateCanviPercentual(EstadisticaSimpleWidgetEntity widget, Object valorConsulta, PeriodeDates periodePrevi) {
            if (!widget.isCompararPeriodeAnterior()
                    || periodePrevi == null || periodePrevi.start == null || periodePrevi.end == null
                    || valorConsulta == null || !(valorConsulta instanceof Number)) {
                return null;
            }

            Number valorActual = (Number) valorConsulta;
            // Calculate the value for the previous period
            Number valorPrevi = (Number) calculateValorSimple(widget, periodePrevi);

            if (valorPrevi == null || valorPrevi.doubleValue() == 0) {
                return null;
            }

            // Calculate the percentage change
            return ((valorActual.doubleValue() - valorPrevi.doubleValue()) / valorPrevi.doubleValue()) * 100;
        }

        private String formatValorSimple(Object valor, IndicadorTaulaEntity indicadorInfo) {
            if (valor == null) {
                return "";
            }

            Format format = indicadorInfo.getIndicador().getFormat();
            if (valor instanceof LocalDate) {
                format = Format.DATE;
            }
            if (format == null) {
                return String.valueOf(valor);
            }

            DecimalFormat df;
            switch (format) {
                case LONG:
                    return String.valueOf(((Number) valor).longValue());
                case DECIMAL:
                    df = new DecimalFormat("#,##0.00");
                    return df.format(valor);
                case PERCENTAGE:
                    df = new DecimalFormat("#,##0.0'%'");
                    return df.format(valor);
                case CURRENCY:
                    df = new DecimalFormat("€ #,##0.00");
                    return df.format(valor);
                case DATE:
                case DATETIME:
                case BOOLEAN:
                default:
                    return String.valueOf(valor);
            }
        }

        private String formatPercent(Double percent) {
            if (percent == null) {
                return null;
            }

            DecimalFormat df = new DecimalFormat("+#,##0.0'%';-#,##0.0'%'");
            return df.format(percent);
        }

        public List<InformeWidgetItem> generateDataWidgetGrafic(DashboardItemEntity dashboardItem) {

            EstadisticaGraficWidgetEntity widget = (EstadisticaGraficWidgetEntity)dashboardItem.getWidget();

            Periode periode = widget.getPeriode();
            PeriodeDates periodeConsulta = PeriodeResolverHelper.resolvePeriod(periode);

            // Get the indicator code
            String indicadorCodi = widget.getIndicador().getIndicador().getCodi();

            // Get the time unit for grouping
            PeriodeUnitat tempsAgrupacio = widget.getTempsAgrupacio();

            // Mapa de dimensions per filtrar la consulta
            Map<String, List<String>> dimensionsFiltre = new HashMap<>();
            if (widget.getDimensionsValor() != null && !widget.getDimensionsValor().isEmpty()) {
                for (DimensioValorEntity dimensioValor : widget.getDimensionsValor()) {
                    String dimensioCodi = dimensioValor.getDimensio().getCodi();
                    String valor = dimensioValor.getValor();

                    if (!dimensionsFiltre.containsKey(dimensioCodi)) {
                        dimensionsFiltre.put(dimensioCodi, new ArrayList<>());
                    }
                    dimensionsFiltre.get(dimensioCodi).add(valor);
                }
            }

            // Get the statistics for the period and dimensions
            List<Fet> fets = estadisticaHelper.getEstadistiquesPeriodeAmbDimensions(
                    widget.getAppId(),
                    periodeConsulta.start,
                    periodeConsulta.end,
                    dimensionsFiltre);

            if (fets == null || fets.isEmpty()) {
                // Return empty chart if no data
                InformeWidgetGraficItem item = InformeWidgetGraficItem.builder()
                        .tipus(WidgetTipus.GRAFIC)
                        .titol(widget.getTitol())
                        .tipusGrafic(widget.getTipusGrafic())
                        .tipusValors(widget.getTipusValors())
                        .llegendaX(widget.getLlegendaX())
                        .llegendaY(widget.getLlegendaY())
                        .labels(new ArrayList<>())
                        .series(new ArrayList<>())
                        .build();
                return List.of(item);
            }

            // Check if we need to decompose by dimension
            boolean hasDecomposition = widget.getDescomposicioDimensio() != null;
            String decompositionDimensionCode = hasDecomposition ? widget.getDescomposicioDimensio().getCodi() : null;

            // Group data by time and dimension (if decomposition is enabled)
            Map<String, Map<String, Double>> dataByTimeAndDimension = new HashMap<>();
            Set<String> dimensionValues = new HashSet<>();
            List<String> timeLabels = new ArrayList<>();

            // Process each record
            for (Fet fet : fets) {
                // Get the time value formatted according to the time unit
                String timeLabel = formatTimeLabel(fet.getTemps().getData(), tempsAgrupacio);

                // Add to time labels if not already present
                if (!timeLabels.contains(timeLabel)) {
                    timeLabels.add(timeLabel);
                }

                // Get the dimension value if decomposition is enabled
                String dimensionValue = hasDecomposition ? fet.getDimensionsJson().get(decompositionDimensionCode) : "default";
                if (dimensionValue == null) {
                    dimensionValue = "default";
                }

                // Add to dimension values if not already present
                if (hasDecomposition && !dimensionValues.contains(dimensionValue)) {
                    dimensionValues.add(dimensionValue);
                }

                // Create time entry if it doesn't exist
                if (!dataByTimeAndDimension.containsKey(timeLabel)) {
                    dataByTimeAndDimension.put(timeLabel, new HashMap<>());
                }

                // Get the indicator value
                if (fet.getIndicadorsJson().containsKey(indicadorCodi)) {
                    double value = fet.getIndicadorsJson().get(indicadorCodi);

                    // Add or aggregate the value
                    Map<String, Double> dimensionData = dataByTimeAndDimension.get(timeLabel);
                    double currentValue = dimensionData.getOrDefault(dimensionValue, 0.0);
                    dimensionData.put(dimensionValue, currentValue + value);
                }
            }

            // Sort time labels chronologically
            Collections.sort(timeLabels);

            // Prepare series data
            List<Map<String, Object>> series = new ArrayList<>();

            if (hasDecomposition) {
                // Create a series for each dimension value
                List<String> sortedDimensionValues = new ArrayList<>(dimensionValues);
                Collections.sort(sortedDimensionValues);

                for (String dimensionValue : sortedDimensionValues) {
                    Map<String, Object> seriesItem = new HashMap<>();
                    seriesItem.put("name", dimensionValue);

                    List<Double> data = new ArrayList<>();
                    for (String timeLabel : timeLabels) {
                        Map<String, Double> dimensionData = dataByTimeAndDimension.get(timeLabel);
                        double value = dimensionData != null ? dimensionData.getOrDefault(dimensionValue, 0.0) : 0.0;
                        data.add(value);
                    }

                    seriesItem.put("data", data);
                    series.add(seriesItem);
                }
            } else {
                // Create a single series
                Map<String, Object> seriesItem = new HashMap<>();
                seriesItem.put("name", indicadorCodi);

                List<Double> data = new ArrayList<>();
                for (String timeLabel : timeLabels) {
                    Map<String, Double> dimensionData = dataByTimeAndDimension.get(timeLabel);
                    double value = dimensionData != null ? dimensionData.getOrDefault("default", 0.0) : 0.0;
                    data.add(value);
                }

                seriesItem.put("data", data);
                series.add(seriesItem);
            }

            // Calculate percentages if needed
            if (widget.getTipusValors() == GraficValueTypeEnum.PERCENTAGE) {
                // For each time point, calculate the total and convert values to percentages
                for (int i = 0; i < timeLabels.size(); i++) {
                    double total = 0.0;

                    // Calculate total for this time point
                    for (Map<String, Object> seriesItem : series) {
                        List<Double> data = (List<Double>) seriesItem.get("data");
                        total += data.get(i);
                    }

                    // Convert to percentages
                    if (total > 0) {
                        for (Map<String, Object> seriesItem : series) {
                            List<Double> data = (List<Double>) seriesItem.get("data");
                            data.set(i, (data.get(i) / total) * 100);
                        }
                    }
                }
            }

            AtributsVisualsGrafic atributsVisuals = resolveAtributsVisualsGrafic(dashboardItem, widget);

            // Create the chart item
            InformeWidgetGraficItem item = InformeWidgetGraficItem.builder()
                    .tipus(WidgetTipus.GRAFIC)
                    .titol(widget.getTitol())
                    .tipusGrafic(widget.getTipusGrafic())
                    .tipusValors(widget.getTipusValors())
                    .llegendaX(widget.getLlegendaX())
                    .llegendaY(widget.getLlegendaY())
                    .labels(timeLabels)
                    .series(series)
                    .dimensionValues(hasDecomposition ? new ArrayList<>(dimensionValues) : null)
                    .atributsVisuals(atributsVisuals)
                    .build();

            return List.of(item);
        }

        private AtributsVisualsGrafic resolveAtributsVisualsGrafic(DashboardItemEntity dashboardItem, EstadisticaGraficWidgetEntity widget) {
            var atributsVisualsWidget = AtributsVisualsGrafic.toAtributsVisuals(widget.getAtributsVisuals());
            var atributsVisualsDash = AtributsVisualsGrafic.toAtributsVisuals(dashboardItem.getAtributsVisuals());
            if (atributsVisualsWidget != null && atributsVisualsDash != null) {
                return (AtributsVisualsGrafic) atributsVisualsDash.merge(atributsVisualsWidget);
            }
            return atributsVisualsDash != null ? atributsVisualsDash : atributsVisualsWidget;
        }

        private String formatTimeLabel(LocalDate date, PeriodeUnitat unit) {
            switch (unit) {
                case DIA:
                    return date.toString();
                case SETMANA:
                    return "Week " + date.get(WeekFields.of(Locale.getDefault()).weekOfYear());
                case MES:
                    return date.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) + " " + date.getYear();
                case TRIMESTRE:
                    int quarter = (date.getMonthValue() - 1) / 3 + 1;
                    return "Q" + quarter + " " + date.getYear();
                case ANY:
                    return String.valueOf(date.getYear());
                default:
                    return date.toString();
            }
        }

        public List<InformeWidgetItem> generateDataWidgetTaula(DashboardItemEntity dashboardItem) {

            EstadisticaTaulaWidgetEntity widget = (EstadisticaTaulaWidgetEntity)dashboardItem.getWidget();

            Periode periode = widget.getPeriode();
            PeriodeDates periodeConsulta = PeriodeResolverHelper.resolvePeriod(periode);

            // Get the dimension for grouping
            String dimensioAgrupacioCode = widget.getDimensioAgrupacio().getCodi();

            // Mapa de dimensions per filtrar la consulta
            Map<String, List<String>> dimensionsFiltre = new HashMap<>();
            if (widget.getDimensionsValor() != null && !widget.getDimensionsValor().isEmpty()) {
                for (DimensioValorEntity dimensioValor : widget.getDimensionsValor()) {
                    String dimensioCodi = dimensioValor.getDimensio().getCodi();
                    String valor = dimensioValor.getValor();

                    if (!dimensionsFiltre.containsKey(dimensioCodi)) {
                        dimensionsFiltre.put(dimensioCodi, new ArrayList<>());
                    }
                    dimensionsFiltre.get(dimensioCodi).add(valor);
                }
            }

            // Get the statistics for the period and dimensions
            List<Fet> fets = estadisticaHelper.getEstadistiquesPeriodeAmbDimensions(
                    widget.getAppId(),
                    periodeConsulta.start,
                    periodeConsulta.end,
                    dimensionsFiltre);

            if (fets == null || fets.isEmpty()) {
                // Return empty table if no data
                InformeWidgetTaulaItem item = InformeWidgetTaulaItem.builder()
                        .tipus(WidgetTipus.TAULA)
                        .titol(widget.getTitol())
                        .titolAgrupament(widget.getTitolAgrupament())
                        .columns(new ArrayList<>())
                        .rows(new ArrayList<>())
                        .build();
                return List.of(item);
            }

            // Get the columns (indicators)
            List<IndicadorTaulaEntity> columnes = widget.getColumnes();

            // Prepare column definitions
            List<Map<String, Object>> columns = new ArrayList<>();

            // First column is the grouping dimension
            Map<String, Object> groupColumn = new HashMap<>();
            groupColumn.put("field", "dimensio");
            groupColumn.put("headerName", widget.getTitolAgrupament() != null ? widget.getTitolAgrupament() : dimensioAgrupacioCode);
            columns.add(groupColumn);

            // Add a column for each indicator
            for (IndicadorTaulaEntity indicador : columnes) {
                Map<String, Object> column = new HashMap<>();
                column.put("field", indicador.getIndicador().getCodi());
                column.put("headerName", indicador.getIndicador().getNom());
                column.put("type", indicador.getIndicador().getFormat().toString());
                column.put("isPercentage", indicador.getAgregacio() == TableColumnsEnum.PERCENTAGE);
                columns.add(column);
            }

            // Group data by dimension value
            Map<String, Map<String, Object>> rowsByDimension = new HashMap<>();

            // Process each record
            for (Fet fet : fets) {
                // Get the dimension value
                String dimensionValue = fet.getDimensionsJson().get(dimensioAgrupacioCode);
                if (dimensionValue == null) {
                    continue;
                }

                // Create row if it doesn't exist
                if (!rowsByDimension.containsKey(dimensionValue)) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("dimensio", dimensionValue);
                    rowsByDimension.put(dimensionValue, row);
                }

                // Add indicator values to the row
                Map<String, Object> row = rowsByDimension.get(dimensionValue);
                for (IndicadorTaulaEntity indicador : columnes) {
                    String indicadorCodi = indicador.getIndicador().getCodi();
                    if (fet.getIndicadorsJson().containsKey(indicadorCodi)) {
                        // If the indicator is already in the row, aggregate it
                        Object currentValue = row.getOrDefault(indicadorCodi, 0.0);
                        double newValue = fet.getIndicadorsJson().get(indicadorCodi);

                        if (currentValue instanceof Number) {
                            double aggregatedValue = ((Number) currentValue).doubleValue() + newValue;
                            row.put(indicadorCodi, aggregatedValue);
                        } else {
                            row.put(indicadorCodi, newValue);
                        }
                    }
                }
            }

            // Convert to list of rows
            List<Map<String, Object>> rows = new ArrayList<>(rowsByDimension.values());

            // Calculate percentages if needed
            for (IndicadorTaulaEntity indicador : columnes) {
                if (indicador.getAgregacio() == TableColumnsEnum.PERCENTAGE) {
                    String indicadorCodi = indicador.getIndicador().getCodi();

                    // Calculate total
                    double total = 0.0;
                    for (Map<String, Object> row : rows) {
                        if (row.containsKey(indicadorCodi) && row.get(indicadorCodi) instanceof Number) {
                            total += ((Number) row.get(indicadorCodi)).doubleValue();
                        }
                    }

                    // Calculate percentages
                    if (total > 0) {
                        for (Map<String, Object> row : rows) {
                            if (row.containsKey(indicadorCodi) && row.get(indicadorCodi) instanceof Number) {
                                double value = ((Number) row.get(indicadorCodi)).doubleValue();
                                double percentage = (value / total) * 100;
                                row.put(indicadorCodi, percentage);
                            }
                        }
                    }
                }
            }

            AtributsVisualsTaula atributsVisuals = resolveAtributsVisualsTaula(dashboardItem, widget);

            // Create the table item
            InformeWidgetTaulaItem item = InformeWidgetTaulaItem.builder()
                    .tipus(WidgetTipus.TAULA)
                    .titol(widget.getTitol())
                    .titolAgrupament(widget.getTitolAgrupament())
                    .columns(columns)
                    .rows(rows)
                    .dimensionValues(new ArrayList<>(rowsByDimension.keySet()))
                    .atributsVisuals(atributsVisuals)
                    .build();

            return List.of(item);
        }

        private AtributsVisualsTaula resolveAtributsVisualsTaula(DashboardItemEntity dashboardItem, EstadisticaTaulaWidgetEntity widget) {
            var atributsVisualsWidget = AtributsVisualsTaula.toAtributsVisuals(widget.getAtributsVisuals());
            var atributsVisualsDash = AtributsVisualsTaula.toAtributsVisuals(dashboardItem.getAtributsVisuals());
            if (atributsVisualsWidget != null && atributsVisualsDash != null) {
                return (AtributsVisualsTaula) atributsVisualsDash.merge(atributsVisualsWidget);
            }
            return atributsVisualsDash != null ? atributsVisualsDash : atributsVisualsWidget;
        }

        @Override
        public void onChange(Serializable id, InformeWidgetParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, InformeWidgetParams target) {
        }

    }

}
