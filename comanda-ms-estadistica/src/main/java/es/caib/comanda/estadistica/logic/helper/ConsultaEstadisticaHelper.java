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
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum.*;
import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE;

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
    private final DashboardItemRepository dashboardItemRepository;

    private static DateTimeFormatter DMYYYY_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");


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

    @Cacheable(value = DASHBOARD_WIDGET_CACHE, key = "#dashboardItem.id + '_' + T(java.time.LocalDate).now()")
    public InformeWidgetItem getDadesWidget(DashboardItemEntity dashboardItem) {

        try {
            // Recarregam l'item, ja que estem en una nova transacció.
            dashboardItem = dashboardItemRepository.findById(dashboardItem.getId()).orElseThrow();
            WidgetTipus tipus = determineWidgetType(dashboardItem);
            DadesComunsWidgetConsulta dadesComunsConsulta = getDadesComunsConsulta(dashboardItem);

            switch (tipus) {
                case SIMPLE:
                    return getDadesWidgetSimple(dashboardItem, dadesComunsConsulta);
                case GRAFIC:
                    return getDadesWidgetGrafic(dashboardItem, dadesComunsConsulta);
                case TAULA:
                    return getDadesWidgetTaula(dashboardItem, dadesComunsConsulta);
            }
        } catch (Exception e) {
            log.error("Error obtnint dades de dashboard widget: " + e.getMessage(), e);
            throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId().toString(), e.getMessage(), e.getCause());
        }

        throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId(), null, "Tipus de widget incorrecte");
    }

    private InformeWidgetItem getDadesWidgetSimple(DashboardItemEntity dashboardItem, DadesComunsWidgetConsulta dadesComunsConsulta) {

        EstadisticaSimpleWidgetEntity widget = (EstadisticaSimpleWidgetEntity)dashboardItem.getWidget();
        TableColumnsEnum agregacio = widget.getIndicadorInfo().getAgregacio();
//        Format format = widget.getIndicadorInfo().getIndicador().getFormat();
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
                .entornCodi(dadesComunsConsulta.getEntornCodi())
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

        List<Map<String, String>> labels = new ArrayList<>();
        List<Map<String, String>> files = new ArrayList<>();

        if (UN_INDICADOR.equals(widget.getTipusDades()) || UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades()) || DOS_INDICADORS.equals(widget.getTipusDades())) {

            IndicadorTaulaEntity indicadorInfo = widget.getIndicadorsInfo() != null ? widget.getIndicadorsInfo().get(0) : null;
            IndicadorAgregacio indicadorAgregacio = indicadorInfo != null ?
                    IndicadorAgregacio.builder()
                            .indicadorCodi(indicadorInfo.getIndicador().getCodi())
                            .agregacio(indicadorInfo.getAgregacio())
                            .unitatAgregacio(indicadorInfo.getUnitatAgregacio())
                            .build()
                    : null;

            if (UN_INDICADOR.equals(widget.getTipusDades())) {
                labels.add(Map.of("id", "agrupacio", "label", getLabelAgrupacioTemporal(tempsAgrupacio)));
                labels.add(Map.of("id", indicadorAgregacio.getIndicadorCodi(), "label", indicadorInfo.getTitol()));
                files = fetRepository.getValorsGraficUnIndicador(
                        dadesComunsConsulta.getEntornAppId(),
                        dadesComunsConsulta.getPeriodeDates().getStart(),
                        dadesComunsConsulta.getPeriodeDates().getEnd(),
                        dimensionsFiltre,
                        indicadorAgregacio,
                        tempsAgrupacio);
                // files: [{'agrupacio': '', 'indicadorAgregacio.getIndicadorCodi()': ''}]

            } else if (UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades())) {

                DimensioEntity descomposicioDimensio = widget.getDescomposicioDimensio() != null ? widget.getDescomposicioDimensio() : null;
                boolean agruparPerDimensioDescomposicio = Boolean.TRUE.equals(widget.getAgruparPerDimensioDescomposicio());
                if (agruparPerDimensioDescomposicio) {
                    labels.add(Map.of("id", "agrupacio", "label", descomposicioDimensio.getNom()));
                    labels.add(Map.of("id", indicadorAgregacio.getIndicadorCodi(), "label", indicadorInfo.getTitol()));
                    files = fetRepository.getValorsGraficUnIndicadorAmdDescomposicio(
                            dadesComunsConsulta.getEntornAppId(),
                            dadesComunsConsulta.getPeriodeDates().getStart(),
                            dadesComunsConsulta.getPeriodeDates().getEnd(),
                            dimensionsFiltre,
                            indicadorAgregacio,
                            descomposicioDimensio.getCodi());
                    // files: [{'agrupacio': '', 'indicadorAgregacio.getIndicadorCodi()': ''}]

                } else {
                    labels.add(Map.of("id", "agrupacio", "label", getLabelAgrupacioTemporal(tempsAgrupacio)));
                    labels.add(Map.of("id", "descomposicio", "label", descomposicioDimensio.getNom()));
                    labels.add(Map.of("id", indicadorAgregacio.getIndicadorCodi(), "label", indicadorInfo.getTitol()));
                    files = fetRepository.getValorsGraficUnIndicadorAmdDescomposicio(
                            dadesComunsConsulta.getEntornAppId(),
                            dadesComunsConsulta.getPeriodeDates().getStart(),
                            dadesComunsConsulta.getPeriodeDates().getEnd(),
                            dimensionsFiltre,
                            indicadorAgregacio,
                            descomposicioDimensio.getCodi(),
                            tempsAgrupacio);
                    // files: [{'agrupacio': '', 'descomposicio': '', 'indicadorAgregacio.getIndicadorCodi()': ''}]

                }

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

                labels.add(Map.of("id", "agrupacio", "label", getLabelAgrupacioTemporal(tempsAgrupacio)));
                IntStream.range(0, widget.getIndicadorsInfo().size()).forEach(index -> {
                    var indicador = widget.getIndicadorsInfo().get(index);
                    labels.add(Map.of("id", "col" + (index + 1), "label", indicador.getTitol()));
                });

            files = fetRepository.getValorsGraficVarisIndicadors(
                    dadesComunsConsulta.getEntornAppId(),
                    dadesComunsConsulta.getPeriodeDates().getStart(),
                    dadesComunsConsulta.getPeriodeDates().getEnd(),
                    dimensionsFiltre,
                    indicadorsAgregacio,
                    tempsAgrupacio);

            // files: [{'agrupacio': '', 'col1': '', .. , 'colN': ''}]

        } else {
            throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId(), null, "Tipus de dades incorrecte");
        }

        String columnaAgrupacio = "agrupacio";
        return InformeWidgetGraficItem.builder()
                .dashboardItemId(dashboardItem.getId())
                .tipus(WidgetTipus.GRAFIC)
                .entornCodi(dadesComunsConsulta.getEntornCodi())
                .titol(widget.getTitol())
                .descripcio(widget.getDescripcio())
                .tipusGrafic(widget.getTipusGrafic())
                .tipusDades(widget.getTipusDades())
                .labels(labels)
                .dades(filesToSeries(files, widget.getTipusGrafic(), widget.getTipusDades()))
                .columnaAgregacio(columnaAgrupacio)
                .llegendaX(widget.getLlegendaX())
//                .llegendaY(widget.getLlegendaY())

                .atributsVisuals((AtributsVisualsGrafic) dadesComunsConsulta.getAtributsVisuals())
                .posX(dashboardItem.getPosX())
                .posY(dashboardItem.getPosY())
                .width(dashboardItem.getWidth())
                .height(dashboardItem.getHeight())
                .build();
    }

    private List<Map<String, Object>> filesToSeries(List<Map<String, String>> files, TipusGraficEnum tipusGrafic, TipusGraficDataEnum tipusDades) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        String agrupacioKey = "agrupacio";
        switch (tipusGrafic) {
            case BAR_CHART:
            case LINE_CHART:
            case SPARK_LINE_CHART://Si se cambia la respuesta por el de una lista de valores en lugar de un mapa de {x: value, y: number} es necessario editar el front.
            case PIE_CHART:
            case GAUGE_CHART:
                boolean isSimpleMapping = files.get(0).size() == 2;

                if (isSimpleMapping) {
                    String key = extractKeyExcluding(files.get(0), agrupacioKey);
                    switch (tipusGrafic) {
                        case PIE_CHART:
                            return convertToPieChartSeriesSimple(files, agrupacioKey, key);
                        case GAUGE_CHART:
                            return convertToGaugeChartSeriesSimple(files, agrupacioKey, key);
                        default:
                            return convertToChartSeriesSimple(files, agrupacioKey, key);
                    }
                }

                List<String> keys = files.get(0).keySet().stream()
                        .filter(k -> !k.equals(agrupacioKey))
                        .collect(Collectors.toList());

                if (keys.contains("descomposicio")) {
                    String valueKey = keys.stream()
                            .filter(k -> !"descomposicio".equals(k))
                            .findFirst()
                            .orElse(null);

                    return tipusGrafic == TipusGraficEnum.PIE_CHART
                            ? groupByAndAggregate(files, "descomposicio", valueKey)
                            : groupByAndMapToSeries(files, agrupacioKey, "descomposicio", valueKey);
                }

                return convertFilesToSeriesWithKeys(files, keys, agrupacioKey, tipusGrafic);
            default:
                throw new NotImplementedException("Tipus de grafic no implementat");
        }
    }

    private String extractKeyExcluding(Map<String, String> map, String excludedKey) {
        return map.keySet().stream()
                .filter(k -> !k.equals(excludedKey))
                .findFirst()
                .orElse(null);
    }

    private List<Map<String, Object>> convertToPieChartSeriesSimple(List<Map<String, String>> files, String agrupacioKey, String valueKey) {
        return files.stream()
                .map(f -> Map.of("label", f.get(agrupacioKey), "value", (Object) toDouble(f.get(valueKey))))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertToGaugeChartSeriesSimple(List<Map<String, String>> files, String agrupacioKey, String valueKey) {
        return files.stream()
            .map(file -> {
                double total = file.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(agrupacioKey))
                    .mapToDouble(entry -> toDouble(entry.getValue()))
                    .sum();
                return Map.of("value", (Object) total);
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertToChartSeriesSimple(List<Map<String, String>> files, String agrupacioKey, String valueKey) {
        return files.stream()
                .map(f -> Map.of(agrupacioKey, f.get(agrupacioKey), valueKey, (Object) toDouble(f.get(valueKey))))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> groupByAndAggregate(List<Map<String, String>> files, String groupByKey, String aggregateKey) {
        return files.stream()
                .collect(Collectors.groupingBy(
                        f -> f.get(groupByKey),
                        Collectors.summingDouble(f -> toDouble(f.get(aggregateKey)))
                ))
                .entrySet().stream()
                .map(entry -> Map.<String, Object>of("label", entry.getKey(), "value", entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> groupByAndMapToSeries(List<Map<String, String>> files, String agrupacioKey, String descomposicioKey, String valueKey) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        var agrupacioElement = files.get(0).get(agrupacioKey);
        var isNumeric = isNumeric(agrupacioElement);
        var isDate = isDate(agrupacioElement);

        return files.stream()
                .collect(Collectors.groupingBy(f -> f.get(agrupacioKey)))
//                        LinkedHashMap::new,
//                        Collectors.toList()))
                .entrySet().stream()
                .sorted((e1, e2) -> {
                    if (isDate)
                        return LocalDate.parse(e1.getKey(), formatter).compareTo(LocalDate.parse(e2.getKey(), formatter));
                    if (isNumeric)
                        return toDouble(e1.getKey()).compareTo(toDouble(e2.getKey()));
                    return e1.getKey().compareTo(e2.getKey());
                })
                .map(entry -> {
                    Map<String, Object> mapped = new LinkedHashMap<>();
                    mapped.put(agrupacioKey, entry.getKey());
                    entry.getValue().forEach(f -> mapped.put(f.get(descomposicioKey), toDouble(f.get(valueKey))));
                    return mapped;
                })
                .collect(Collectors.toList());
    }

    private boolean isNumeric(String valor) {
        if (valor == null || valor.isEmpty()) return false;
        return valor.matches("-?\\d+(\\.\\d+)?");
    }
    private boolean isDate(String valor) {
        if (valor == null || valor.isEmpty()) return false;
        try {
            // Intenta analitzar la data; si fallés, llençarà una excepció
            LocalDate.parse(valor, DMYYYY_FORMATTER);
            return true; // És una data vàlida
        } catch (DateTimeParseException e) {
            return false; // No és una data vàlida
        }

    }

    private List<Map<String, Object>> convertFilesToSeriesWithKeys(List<Map<String, String>> files, List<String> keys, String agrupacioKey, TipusGraficEnum tipusGrafic) {
        if (tipusGrafic == TipusGraficEnum.PIE_CHART) {
            return keys.stream()
                    .map(key -> {
                        double sum = files.stream()
                                .mapToDouble(row -> toDouble(row.get(key)) != null ? toDouble(row.get(key)) : 0.0)
                                .sum();
                        return Map.<String, Object>of("label", key, "value", sum);
                    })
                    .collect(Collectors.toList());
        }

        return files.stream()
                .map(f -> {
                    Map<String, Object> mapped = new LinkedHashMap<>();
                    mapped.put(agrupacioKey, f.get(agrupacioKey));
                    keys.forEach(k -> mapped.put(k, toDouble(f.get(k))));
                    return mapped;
                })
                .collect(Collectors.toList());
    }


    private InformeWidgetItem getDadesWidgetTaula(DashboardItemEntity dashboardItem, DadesComunsWidgetConsulta dadesComunsConsulta) throws ReportGenerationException {
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
        columnes.add(Map.of("id", "agrupacio", "label", widget.getTitolAgrupament() != null ? widget.getTitolAgrupament() : widget.getDimensioAgrupacio().getNom()));
        IntStream.range(0, widget.getColumnes().size()).forEach(index -> {
            var columna = widget.getColumnes().get(index);
            columnes.add(Map.of("id", "col" + (index + 1), "label", columna.getTitol()));
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
                .entornCodi(dadesComunsConsulta.getEntornCodi())
                .titol(widget.getTitol())
                .titolAgrupament(widget.getDimensioAgrupacio().getDescripcio())
                .columnes(columnes)
                .files(files)
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
        Double resultatActual = toDouble(valorConsulta);
        if (resultatActual == null) {
            return null;
        }

        // Calcula el valor pel període previ
        String valorConsultaPrevia = calculateValorSimple(widget, periodePrevi, entornAppId);
        Double resultatPrevi = toDouble(valorConsultaPrevia);
        if (resultatPrevi == null) {
            return null;
        }

        // Calcula el percentage
        return String.format("%.2f", getPercentatgeComparacio(resultatPrevi.doubleValue(), resultatActual.doubleValue()));
    }

    public WidgetTipus determineWidgetType(DashboardItemEntity dashboardItem) throws ReportGenerationException {
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
        var entornApp = estadisticaClientHelper.entornAppFindByAppAndEntorn(widget.getAppId(), dashboardItem.getEntornId());
        var entorn = estadisticaClientHelper.entornById(entornApp.getEntorn().getId());
        PeriodeDates periodeDates = PeriodeResolverHelper.resolvePeriod(widget.getPeriode());
        AtributsVisuals atributsVisuals = resolveAtributsVisuals(dashboardItem);

        return DadesComunsWidgetConsulta.builder()
                .entornAppId(entornApp.getId())
                .entornCodi(entorn.getCodi())
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
        Map<String, List<String>> dimensionFilters = new LinkedHashMap<>();
        for (DimensioValorEntity dimensioValor : dimensioValors) {
            String dimensionCode = dimensioValor.getDimensio().getCodi();
            String value = dimensioValor.getValor();
            dimensionFilters.computeIfAbsent(dimensionCode, k -> new ArrayList<>()).add(value);
        }
        return dimensionFilters;
    }

    private Double toDouble(String value) {
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

    private String getLabelAgrupacioTemporal(PeriodeUnitat tempsAgrupacio) {
        switch (tempsAgrupacio) {
            case DIA:
                return "Dia";
            case SETMANA:
                return "Setmana";
            case MES:
                return "Mes";
            case TRIMESTRE:
                return "Trimestre";
            case ANY:
                return "Any";
            default:
                return tempsAgrupacio.name();
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
