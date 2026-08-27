package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.helper.PeriodeResolverHelper.PeriodeDates;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTaula;
import es.caib.comanda.estadistica.logic.intf.model.consulta.*;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OrdreDireccioEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Fet;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Temps;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.periode.Periode;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.*;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaGrupPaletesEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
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

    private static final int CODI_IN_QUERY_BATCH_SIZE = 900;

    private final FetRepository fetRepository;
    private final DashboardItemRepository dashboardItemRepository;
    private final UnitatOrganitzativaRepository unitatOrganitzativaRepository;
    private final DimensioRepository dimensioRepository;
    private final IndicadorRepository indicadorRepository;
    private final IndicadorFormulaTermeRepository indicadorFormulaTermeRepository;

    private final AtributsVisualsHelper atributsVisualsHelper;
    private final DashboardStyleResolverHelper dashboardStyleResolverHelper;
    private final EstadisticaClientHelper estadisticaClientHelper;
    private final DashboardSeguretatHelper dashboardSeguretatHelper;
    private final es.caib.comanda.ms.logic.helper.AuthenticationHelper authenticationHelper;

    private static DateTimeFormatter DMYYYY_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");

    /**
     * Si l'indicador (codi, entornAppId) és de tipus FORMULA, resol els seus termes (codi de l'indicador
     * component + operador) perquè la capa de generació de SQL (FetRepositoryDialect) pugui expandir-lo
     * a una suma/resta de JSON_VALUE en lloc d'un únic valor. Retorna null per a indicadors SIMPLE (o
     * inexistents), en què la consulta es genera igual que sempre.
     */
    private List<IndicadorFormulaTermeResolt> resoldreTermesFormula(String indicadorCodi, Long entornAppId) {
        if (indicadorCodi == null || entornAppId == null) {
            return null;
        }
        IndicadorEntity indicador = indicadorRepository.findByCodiAndEntornAppId(indicadorCodi, entornAppId).orElse(null);
        if (indicador == null || indicador.getTipus() != IndicadorTipus.FORMULA) {
            return null;
        }
        List<IndicadorFormulaTermeEntity> termes = indicadorFormulaTermeRepository.findByIndicadorFormulaIdOrderByOrdreAsc(indicador.getId());
        if (termes == null || termes.isEmpty()) {
            return null;
        }
        return termes.stream()
            .map(terme -> IndicadorFormulaTermeResolt.builder()
                .indicadorCodi(terme.getIndicadorComponent().getCodi())
                .operador(terme.getOperador())
                .build())
            .collect(Collectors.toList());
    }


    // CONSULTA ESTADISTIQUES
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Recupera una llista de fets estadístics corresponents a un entorn d'aplicació específic i a un període de dates especificat.
     * Els fets estadístics inclouen informació agregada relativa a dimensions, indicadors i temps associats.
     *
     * @param entornAppId Identificador de l'entorn d'aplicació pel qual es volen recuperar els fets estadístics.
     * @param dataInici   Data d'inici del període pel qual es volen recuperar els fets estadístics.
     * @param dataFi      Data de finalització del període pel qual es volen recuperar els fets estadístics.
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
     * @param entornAppId      Identificador de l'entorn d'aplicació pel qual es volen recuperar els fets estadístics.
     * @param dataInici        Data d'inici del període pel qual es volen recuperar els fets estadístics.
     * @param dataFi           Data de finalització del període pel qual es volen recuperar els fets estadístics.
     * @param dimensionsFiltre Un mapa que conté les dimensions a filtrar, on la clau és el nom de la dimensió i el valor és
     *                         una llista de valors que s'han de considerar per aquesta dimensió.
     * @return Una llista d'objectes {@link Fet} que representen els fets estadístics associats al període, l'entorn i
     * les dimensions especificades en els filtres.
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

    // La clau de cache HA d'incloure l'usuari: el resultat depèn dels seus permisos d'entitat/òrgan (vegeu
    // DashboardSeguretatHelper), així que usuaris diferents no es poden compartir la mateixa entrada de cache.
    @Cacheable(value = DASHBOARD_WIDGET_CACHE, key = "#dashboardItem.id + '_' + #temaFosc + '_' + (#filtreSeleccio != null ? #filtreSeleccio.cacheKey() : '') + '_' + @authenticationHelper.getCurrentUserName() + '_' + T(java.time.LocalDate).now()")
    public InformeWidgetItem getDadesWidget(DashboardItemEntity dashboardItem,
                                            boolean temaFosc,
                                            DashboardFiltreSeleccio filtreSeleccio) {

        try {
            // Recarregam l'item, ja que estem en una nova transacció.
            dashboardItem = dashboardItemRepository.findById(dashboardItem.getId()).orElseThrow();
            WidgetTipus tipus = determineWidgetType(dashboardItem);
            DadesComunsWidgetConsulta dadesComunsConsulta = getDadesComunsConsulta(dashboardItem, temaFosc, filtreSeleccio);

            SeguretatDadesResultat seguretat = dashboardSeguretatHelper.resoldre(dadesComunsConsulta.getEntornAppId());
            if (seguretat.isSensePermisos()) {
                return buildSenseAccesItem(dashboardItem, tipus);
            }
            SeguretatFiltreSql filtreSql = seguretat.getFiltreSql();

            switch (tipus) {
                case SIMPLE:
                    return getDadesWidgetSimple(dashboardItem, dadesComunsConsulta, filtreSeleccio, filtreSql);
                case GRAFIC:
                    return getDadesWidgetGrafic(dashboardItem, dadesComunsConsulta, filtreSeleccio, filtreSql);
                case TAULA:
                    return getDadesWidgetTaula(dashboardItem, dadesComunsConsulta, filtreSeleccio, filtreSql);
            }
        } catch (ReportGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error obtnint dades de dashboard widget: " + e.getMessage(), e);
            throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId().toString(), e.getMessage(), e.getCause());
        }

        throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId(), null, "Tipus de widget incorrecte");
    }

    /**
     * Item "buit" retornat quan l'usuari no té cap permís d'entitat ni d'òrgan enlloc del sistema (vegeu
     * DashboardSeguretatHelper) - el frontend ha de mostrar un missatge informatiu en lloc d'intentar renderitzar
     * dades (que, deliberadament, no s'arriben ni a consultar).
     */
    private InformeWidgetItem buildSenseAccesItem(DashboardItemEntity dashboardItem, WidgetTipus tipus) {
        return InformeWidgetItem.builder()
            .dashboardItemId(dashboardItem.getId())
            .widgetId(dashboardItem.getWidget().getId())
            .tipus(tipus)
            .titol(dashboardItem.getWidget().getTitol())
            .posX(dashboardItem.getPosX())
            .posY(dashboardItem.getPosY())
            .width(dashboardItem.getWidth())
            .height(dashboardItem.getHeight())
            .destacat(Boolean.TRUE.equals(dashboardItem.getDestacat()))
            .senseAccesDades(true)
            .build();
    }

    private InformeWidgetItem getDadesWidgetSimple(DashboardItemEntity dashboardItem,
                                                   DadesComunsWidgetConsulta dadesComunsConsulta,
                                                   DashboardFiltreSeleccio filtreSeleccio,
                                                   SeguretatFiltreSql seguretat) {

        EstadisticaSimpleWidgetEntity widget = (EstadisticaSimpleWidgetEntity) dashboardItem.getWidget();
        TableColumnsEnum agregacio = widget.getIndicadorInfo().getAgregacio();
//        Format format = widget.getIndicadorInfo().getIndicador().getFormat();
        boolean compararPeriodeAnterior = widget.isCompararPeriodeAnterior() && !TableColumnsEnum.FIRST_SEEN.equals(agregacio) && !TableColumnsEnum.LAST_SEEN.equals(agregacio);
        String valorConsulta = calculateValorSimple(widget, dadesComunsConsulta.getPeriodeDates(), dadesComunsConsulta.getEntornAppId(), filtreSeleccio, seguretat);
        PeriodeDates periodePrevi = compararPeriodeAnterior
            ? PeriodeResolverHelper.resolvePreviousPeriod(widget.getPeriode(), dadesComunsConsulta.getPeriodeDates())
            : null;
        String valorConsultaPrevia = compararPeriodeAnterior
            ? calculateCanviPercentual(widget, valorConsulta, periodePrevi, dadesComunsConsulta.getEntornAppId(), filtreSeleccio, seguretat)
            : null;

        return InformeWidgetSimpleItem.builder()
            .dashboardItemId(dashboardItem.getId())
            .widgetId(dashboardItem.getWidget().getId())
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
            .destacat(Boolean.TRUE.equals(dashboardItem.getDestacat()))
            .build();
    }

    private InformeWidgetItem getDadesWidgetGrafic(DashboardItemEntity dashboardItem,
                                                   DadesComunsWidgetConsulta dadesComunsConsulta,
                                                   DashboardFiltreSeleccio filtreSeleccio,
                                                   SeguretatFiltreSql seguretat) {

        EstadisticaGraficWidgetEntity widget = (EstadisticaGraficWidgetEntity) dashboardItem.getWidget();
        PeriodeUnitat tempsAgrupacio = widget.getTempsAgrupacio();
        // Mapa de dimensions per filtrar la consulta (pròpies del widget + selecció de filtres del dashboard)
        Map<String, List<String>> dimensionsFiltre = resolveDimensionsFiltre(widget, dadesComunsConsulta.getEntornAppId(), filtreSeleccio);

        List<Map<String, String>> labels = new ArrayList<>();
        List<Map<String, String>> files = new ArrayList<>();

        if (UN_INDICADOR.equals(widget.getTipusDades()) || UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades()) || DOS_INDICADORS.equals(widget.getTipusDades())) {

            IndicadorTaulaEntity indicadorInfo = widget.getIndicadorsInfo() != null ? widget.getIndicadorsInfo().get(0) : null;
            IndicadorAgregacio indicadorAgregacio = indicadorInfo != null ?
                IndicadorAgregacio.builder()
                    .indicadorCodi(indicadorInfo.getIndicador().getCodi())
                    .agregacio(indicadorInfo.getAgregacio())
                    .unitatAgregacio(indicadorInfo.getUnitatAgregacio())
                    .termesFormula(resoldreTermesFormula(indicadorInfo.getIndicador().getCodi(), dadesComunsConsulta.getEntornAppId()))
                    .build()
                : null;

            if (UN_INDICADOR.equals(widget.getTipusDades())) {
                labels.add(Map.of("id", "agrupacio", "label", getLabelAgrupacioTemporal(tempsAgrupacio)));
                labels.add(Map.of("id", indicadorAgregacio.getIndicadorCodi(), "label", StringUtils.defaultString(indicadorInfo.getTitol())));
                files = fetRepository.getValorsGraficUnIndicador(
                    dadesComunsConsulta.getEntornAppId(),
                    dadesComunsConsulta.getPeriodeDates().getStart(),
                    dadesComunsConsulta.getPeriodeDates().getEnd(),
                    dimensionsFiltre,
                    indicadorAgregacio,
                    tempsAgrupacio,
                    seguretat);
                // files: [{'agrupacio': '', 'indicadorAgregacio.getIndicadorCodi()': ''}]

            } else if (UN_INDICADOR_AMB_DESCOMPOSICIO.equals(widget.getTipusDades())) {

                DimensioEntity descomposicioDimensio = widget.getDescomposicioDimensio() != null ? widget.getDescomposicioDimensio() : null;
                boolean agruparPerDimensioDescomposicio = Boolean.TRUE.equals(widget.getAgruparPerDimensioDescomposicio());
                if (agruparPerDimensioDescomposicio) {
                    labels.add(Map.of("id", "agrupacio", "label", descomposicioDimensio.getNom()));
                    labels.add(Map.of("id", indicadorAgregacio.getIndicadorCodi(), "label", StringUtils.defaultString(indicadorInfo.getTitol())));
                    files = fetRepository.getValorsGraficUnIndicadorAmdDescomposicio(
                        dadesComunsConsulta.getEntornAppId(),
                        dadesComunsConsulta.getPeriodeDates().getStart(),
                        dadesComunsConsulta.getPeriodeDates().getEnd(),
                        dimensionsFiltre,
                        indicadorAgregacio,
                        descomposicioDimensio.getCodi(),
                        seguretat);
                    // files: [{'agrupacio': '', 'indicadorAgregacio.getIndicadorCodi()': ''}]

                } else {
                    labels.add(Map.of("id", "agrupacio", "label", getLabelAgrupacioTemporal(tempsAgrupacio)));
                    labels.add(Map.of("id", "descomposicio", "label", descomposicioDimensio.getNom()));
                    labels.add(Map.of("id", indicadorAgregacio.getIndicadorCodi(), "label", StringUtils.defaultString(indicadorInfo.getTitol())));
                    files = fetRepository.getValorsGraficUnIndicadorAmdDescomposicio(
                        dadesComunsConsulta.getEntornAppId(),
                        dadesComunsConsulta.getPeriodeDates().getStart(),
                        dadesComunsConsulta.getPeriodeDates().getEnd(),
                        dimensionsFiltre,
                        indicadorAgregacio,
                        descomposicioDimensio.getCodi(),
                        tempsAgrupacio,
                        seguretat);
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
                    .termesFormula(resoldreTermesFormula(columna.getIndicador().getCodi(), dadesComunsConsulta.getEntornAppId()))
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
                tempsAgrupacio,
                seguretat);

            // files: [{'agrupacio': '', 'col1': '', .. , 'colN': ''}]

        } else {
            throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId(), null, "Tipus de dades incorrecte");
        }

        String columnaAgrupacio = "agrupacio";
        return InformeWidgetGraficItem.builder()
            .dashboardItemId(dashboardItem.getId())
            .widgetId(dashboardItem.getWidget().getId())
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
            .destacat(Boolean.TRUE.equals(dashboardItem.getDestacat()))
            .build();
    }

    private List<Map<String, Object>> filesToSeries(List<Map<String, String>> files,
                                                    TipusGraficEnum tipusGrafic,
                                                    TipusGraficDataEnum tipusDades) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        String agrupacioKey = "agrupacio";
        switch (tipusGrafic) {
            case BAR_CHART:
            case LINE_CHART:
            case
                SPARK_LINE_CHART://Si se cambia la respuesta por el de una lista de valores en lugar de un mapa de {x: value, y: number} es necessario editar el front.
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

    private List<Map<String, Object>> convertToPieChartSeriesSimple(List<Map<String, String>> files,
                                                                    String agrupacioKey,
                                                                    String valueKey) {
        return files.stream()
            .map(f -> Map.of("label", f.get(agrupacioKey), "value", (Object) toDouble(f.get(valueKey))))
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> convertToGaugeChartSeriesSimple(List<Map<String, String>> files,
                                                                      String agrupacioKey,
                                                                      String valueKey) {
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

    private List<Map<String, Object>> convertToChartSeriesSimple(List<Map<String, String>> files,
                                                                 String agrupacioKey,
                                                                 String valueKey) {
        return files.stream()
            .map(f -> Map.of(agrupacioKey, f.get(agrupacioKey), valueKey, (Object) toDouble(f.get(valueKey))))
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> groupByAndAggregate(List<Map<String, String>> files,
                                                          String groupByKey,
                                                          String aggregateKey) {
        return files.stream()
            .collect(Collectors.groupingBy(
                f -> f.get(groupByKey),
                Collectors.summingDouble(f -> toDouble(f.get(aggregateKey)))
            ))
            .entrySet().stream()
            .map(entry -> Map.<String, Object>of("label", entry.getKey(), "value", entry.getValue()))
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> groupByAndMapToSeries(List<Map<String, String>> files,
                                                            String agrupacioKey,
                                                            String descomposicioKey,
                                                            String valueKey) {
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

    private List<Map<String, Object>> convertFilesToSeriesWithKeys(List<Map<String, String>> files,
                                                                   List<String> keys,
                                                                   String agrupacioKey,
                                                                   TipusGraficEnum tipusGrafic) {
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


    private InformeWidgetItem getDadesWidgetTaula(DashboardItemEntity dashboardItem,
                                                  DadesComunsWidgetConsulta dadesComunsConsulta,
                                                  DashboardFiltreSeleccio filtreSeleccio,
                                                  SeguretatFiltreSql seguretat) throws ReportGenerationException {
        EstadisticaTaulaWidgetEntity widget = (EstadisticaTaulaWidgetEntity) dashboardItem.getWidget();
        if (widget.getDimensioAgrupacio() == null) {
            throw new ReportGenerationException(DashboardItem.class, dashboardItem.getId(), null, "El widget de taula no té cap dimensió d'agrupació configurada");
        }
        // Mapa de dimensions per filtrar la consulta (pròpies del widget + selecció de filtres del dashboard)
        Map<String, List<String>> dimensionsFiltre = resolveDimensionsFiltre(widget, dadesComunsConsulta.getEntornAppId(), filtreSeleccio);
        // Indicadors a calcular
        List<IndicadorAgregacio> indicadorsAgregacio = widget.getColumnes().stream()
            .map(columna -> IndicadorAgregacio.builder()
                .indicadorCodi(columna.getIndicador().getCodi())
                .agregacio(columna.getAgregacio())
                .unitatAgregacio(columna.getUnitatAgregacio())
                .termesFormula(resoldreTermesFormula(columna.getIndicador().getCodi(), dadesComunsConsulta.getEntornAppId()))
                .build())
            .collect(Collectors.toList());
        // Dimensió utilitzada per agrupar
        String dimensioAgrupacioCodi = widget.getDimensioAgrupacio().getCodi();
        // Títol efectiu de l'agrupament: sobreescriptura del widget si n'hi ha, si no el nom de la dimensió
        // (abans es retornava widget.getDimensioAgrupacio().getDescripcio(), que no coincidia amb l'etiqueta de columna real)
        String titolAgrupacioEfectiu = widget.getTitolAgrupament() != null && !widget.getTitolAgrupament().isBlank()
            ? widget.getTitolAgrupament()
            : widget.getDimensioAgrupacio().getNom();

        List<Map<String, String>> columnes = new ArrayList<>();
        columnes.add(Map.of("id", "agrupacio", "label", titolAgrupacioEfectiu));
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
            dimensioAgrupacioCodi,
            seguretat);

        DimensioEntity dimensioEntity = dimensioRepository.findByCodiAndEntornAppId(dimensioAgrupacioCodi, dadesComunsConsulta.getEntornAppId()).orElse(null);
        if (dimensioEntity != null && TipusDimensioEnum.TIPUS_AMB_UNITAT_ORG.contains(dimensioEntity.getTipus())) {
            Set<String> codisAgrupacio = files.stream()
                .map(c -> c.get("agrupacio"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            Map<String, String> codiNomPerCodi = findUnitatsOrganitzativesCodiNomByCodiInBatches(codisAgrupacio);
            files = files.stream().peek(c -> {
                String codi = c.get("agrupacio");
                if (codi != null && codiNomPerCodi.containsKey(codi)) {
                    c.put("agrupacio", codiNomPerCodi.get(codi));
                }
            }).collect(Collectors.toList());
        }

        files = applyFilesFilterSortLimit(files, widget);

        return InformeWidgetTaulaItem.builder()
            .dashboardItemId(dashboardItem.getId())
            .widgetId(dashboardItem.getWidget().getId())
            .tipus(WidgetTipus.TAULA)
            .entornCodi(dadesComunsConsulta.getEntornCodi())
            .titol(widget.getTitol())
            .titolAgrupament(titolAgrupacioEfectiu)
            .columnes(columnes)
            .files(files)
            .atributsVisuals((AtributsVisualsTaula) dadesComunsConsulta.getAtributsVisuals())
            .posX(dashboardItem.getPosX())
            .posY(dashboardItem.getPosY())
            .width(dashboardItem.getWidth())
            .height(dashboardItem.getHeight())
            .destacat(Boolean.TRUE.equals(dashboardItem.getDestacat()))
            .build();
    }

    private Map<String, String> findUnitatsOrganitzativesCodiNomByCodiInBatches(Set<String> codis) {
        if (codis == null || codis.isEmpty()) {
            return Map.of();
        }

        List<String> codiList = new ArrayList<>(codis);
        Map<String, String> result = new HashMap<>();

        for (int fromIndex = 0; fromIndex < codiList.size(); fromIndex += CODI_IN_QUERY_BATCH_SIZE) {
            int toIndex = Math.min(fromIndex + CODI_IN_QUERY_BATCH_SIZE, codiList.size());
            List<String> batch = codiList.subList(fromIndex, toIndex);

            unitatOrganitzativaRepository.findByCodiIn(batch).forEach(uo -> {
                if (uo.getCodi() != null) {
                    result.putIfAbsent(uo.getCodi(), uo.getCodiNom());
                }
            });
        }

        return result;
    }

    /**
     * Aplica les opcions de tractament de resultats configurades al widget de taula (vegeu
     * {@link es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaTaulaWidget}): amagar les files
     * amb totes les columnes d'indicador a zero, ordenar per una columna concreta i limitar el nombre de
     * files retornades. S'apliquen en aquest ordre — filtrar abans d'ordenar/limitar, perquè el límit s'ha
     * d'aplicar sobre les files rellevants, no sobre les que ja s'amagaran.
     */
    static List<Map<String, String>> applyFilesFilterSortLimit(List<Map<String, String>> files,
                                                               EstadisticaTaulaWidgetEntity widget) {
        List<Map<String, String>> result = files;
        if (Boolean.TRUE.equals(widget.getAmagarFilesZero())) {
            result = result.stream().filter(row -> !isTotAZero(row)).collect(Collectors.toList());
        }
        if (widget.getColumnaOrdenacio() != null) {
            String columnName = "col" + (widget.getColumnaOrdenacio() + 1);
            boolean descendent = !OrdreDireccioEnum.ASC.equals(widget.getDireccioOrdenacio());
            Comparator<Map<String, String>> comparator = Comparator.comparingDouble(row -> parseNumericValue(row.get(columnName)));
            result = result.stream()
                .sorted(descendent ? comparator.reversed() : comparator)
                .collect(Collectors.toList());
            if (widget.getLimitResultats() != null && widget.getLimitResultats() > 0 && result.size() > widget.getLimitResultats()) {
                result = new ArrayList<>(result.subList(0, widget.getLimitResultats()));
            }
        }
        return result;
    }

    private static boolean isTotAZero(Map<String, String> row) {
        return row.entrySet().stream()
            .filter(entry -> !"agrupacio".equals(entry.getKey()))
            .allMatch(entry -> parseNumericValue(entry.getValue()) == 0);
    }

    private static double parseNumericValue(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String[] getColumnNames(List indicadorsList) {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("agrupacio");

        for (int i = 1; i <= indicadorsList.size(); i++) {
            columnNames.add("col" + i);
        }
        return columnNames.toArray(new String[columnNames.size()]);
    }


    private String calculateValorSimple(EstadisticaSimpleWidgetEntity widget,
                                        PeriodeDates periodeConsulta,
                                        Long entornAppId,
                                        DashboardFiltreSeleccio filtreSeleccio,
                                        SeguretatFiltreSql seguretat) {
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
            .termesFormula(resoldreTermesFormula(indicadorCodi, entornAppId))
            .build();

        // Mapa de dimensions per filtrar la consulta (pròpies del widget + selecció de filtres del dashboard)
        Map<String, List<String>> dimensionsFiltre = resolveDimensionsFiltre(widget, entornAppId, filtreSeleccio);

        // Get the aggregated value directly from the database
        return fetRepository.getValorSimpleAgregat(
            entornAppId,
            periodeConsulta.start,
            periodeConsulta.end,
            dimensionsFiltre,
            indicadorAgregacio,
            seguretat);
    }

    private String calculateCanviPercentual(EstadisticaSimpleWidgetEntity widget,
                                            String valorConsulta,
                                            PeriodeDates periodePrevi,
                                            Long entornAppId,
                                            DashboardFiltreSeleccio filtreSeleccio,
                                            SeguretatFiltreSql seguretat) {
        if (!widget.isCompararPeriodeAnterior()
            || periodePrevi == null || periodePrevi.start == null || periodePrevi.end == null) {
            return null;
        }
        Double resultatActual = toDouble(valorConsulta);
        if (resultatActual == null) {
            return null;
        }

        // Calcula el valor pel període previ
        String valorConsultaPrevia = calculateValorSimple(widget, periodePrevi, entornAppId, filtreSeleccio, seguretat);
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

    private DadesComunsWidgetConsulta getDadesComunsConsulta(DashboardItemEntity dashboardItem,
                                                             boolean temaFosc,
                                                             DashboardFiltreSeleccio filtreSeleccio) {
        EstadisticaWidgetEntity widget = dashboardItem.getWidget();
        var entornApp = estadisticaClientHelper.entornAppFindByAppAndEntorn(widget.getAppId(), dashboardItem.getEntornId());
        var entorn = estadisticaClientHelper.entornById(entornApp.getEntorn().getId());
        // El període seleccionat pel filtre de capçalera del dashboard, si n'hi ha, sobreescriu el període propi del widget.
        Periode periodeEfectiu = filtreSeleccio != null && filtreSeleccio.hasPeriodeOverride()
            ? filtreSeleccio.getPeriode()
            : widget.getPeriode();
        PeriodeDates periodeDates = PeriodeResolverHelper.resolvePeriod(periodeEfectiu);
        AtributsVisuals atributsVisuals = resolveAtributsVisuals(dashboardItem, temaFosc);

        return DadesComunsWidgetConsulta.builder()
            .entornAppId(entornApp.getId())
            .entornCodi(entorn.getCodi())
            .periodeDates(periodeDates)
            .atributsVisuals(atributsVisuals)
            .build();
    }

    /**
     * Combina el filtre de dimensions propi del widget amb la selecció de filtres de capçalera del dashboard.
     * Un filtre de dashboard només s'aplica si el widget pertany a un entorn d'aplicació que realment té una
     * dimensió amb aquest codi - si no, el widget pertany a una altra app i el filtre se n'ignora (no es buida
     * el widget mostrant zero resultats per un filtre que no li és aplicable).
     */
    private Map<String, List<String>> resolveDimensionsFiltre(EstadisticaWidgetEntity widget,
                                                              Long entornAppId,
                                                              DashboardFiltreSeleccio filtreSeleccio) {
        Map<String, List<String>> result = widget.getDimensionsValor() != null && !widget.getDimensionsValor().isEmpty()
            ? createDimensionsFiltre(widget.getDimensionsValor())
            : new LinkedHashMap<>();
        if (filtreSeleccio == null || filtreSeleccio.getDimensions() == null) {
            return result;
        }
        filtreSeleccio.getDimensions().forEach((codi, valors) -> {
            if (codi == null || valors == null || valors.isEmpty()) {
                return;
            }
            if (dimensioRepository.findByCodiAndEntornAppId(codi, entornAppId).isPresent()) {
                result.put(codi, valors);
            }
        });
        return result;
    }

    public AtributsVisuals resolveAtributsVisuals(DashboardItemEntity dashboardItem, boolean temaFosc) {
        AtributsVisuals resolved = ensureAtributsVisualsType(dashboardItem, null);
        // Els camps propis del widget/dashboardItem només sobreescriuen la plantilla si l'usuari ha
        // activat "personalitzat" explícitament; en cas contrari (encara que hi hagi valors residuals
        // guardats) s'ha d'aplicar sempre la plantilla amb prioritat (i el seu tema destacat).
        if (Boolean.TRUE.equals(dashboardItem.getPersonalitzat())) {
            AtributsVisuals atributsVisualsDash = atributsVisualsHelper.getAtributsVisuals(dashboardItem);
            if (atributsVisualsDash != null) {
                resolved = resolved.merge(atributsVisualsDash);
            }
            AtributsVisuals atributsVisualsWidget = atributsVisualsHelper.getAtributsVisuals(dashboardItem.getWidget());
            if (atributsVisualsWidget != null) {
                resolved = resolved.merge(atributsVisualsWidget);
            }
        }
        PlantillaEntity plantilla = dashboardItem.getPlantilla() != null
            ? dashboardItem.getPlantilla()
            : dashboardItem.getDashboard() != null ? dashboardItem.getDashboard().getPlantilla() : null;
        log.debug(
            "resolveAtributsVisuals dashboardItem={} personalitzat={} destacat={} plantillaId={} plantillaNom={}",
            dashboardItem.getId(), dashboardItem.getPersonalitzat(), dashboardItem.getDestacat(),
            plantilla != null ? plantilla.getId() : null, plantilla != null ? plantilla.getNom() : null);
        if (plantilla != null) {
            boolean destacat = Boolean.TRUE.equals(dashboardItem.getDestacat());
            PaletteGroupType groupType = temaFosc
                ? (destacat ? PaletteGroupType.DARK_HIGHLIGHTED : PaletteGroupType.DARK)
                : (destacat ? PaletteGroupType.LIGHT_HIGHLIGHTED : PaletteGroupType.LIGHT);
            log.debug(
                "resolveAtributsVisuals dashboardItem={} groupType={} paletteGroups={} styleProperties={}",
                dashboardItem.getId(), groupType,
                plantilla.getPaletteGroups() != null
                    ? plantilla.getPaletteGroups().stream().map(PlantillaGrupPaletesEntity::getGroupType).collect(Collectors.toList())
                    : null,
                plantilla.getStyleProperties() != null
                    ? plantilla.getStyleProperties().stream().map(p -> p.getScope() + ":" + p.getPropertyName()).collect(Collectors.toList())
                    : null);
            dashboardStyleResolverHelper.applyTemplateDefaults(resolved, plantilla, groupType, widgetStyleScope(dashboardItem));
            log.debug("resolveAtributsVisuals dashboardItem={} resolved={}", dashboardItem.getId(), resolved);
        }
        return ensureAtributsVisualsType(dashboardItem, resolved);
    }

    private AtributsVisuals ensureAtributsVisualsType(DashboardItemEntity dashboardItem, AtributsVisuals current) {
        if (current != null) {
            return current;
        }
        if (dashboardItem.getWidget() instanceof EstadisticaSimpleWidgetEntity) {
            return new AtributsVisualsSimple();
        }
        if (dashboardItem.getWidget() instanceof EstadisticaGraficWidgetEntity) {
            return new AtributsVisualsGrafic();
        }
        if (dashboardItem.getWidget() instanceof EstadisticaTaulaWidgetEntity) {
            return new AtributsVisualsTaula();
        }
        return null;
    }

    private WidgetStyleScope widgetStyleScope(DashboardItemEntity dashboardItem) {
        if (dashboardItem.getWidget() instanceof EstadisticaSimpleWidgetEntity) {
            return WidgetStyleScope.SIMPLE;
        }
        if (dashboardItem.getWidget() instanceof EstadisticaGraficWidgetEntity) {
            return WidgetStyleScope.GRAFIC;
        }
        if (dashboardItem.getWidget() instanceof EstadisticaTaulaWidgetEntity) {
            return WidgetStyleScope.TAULA;
        }
        return WidgetStyleScope.COMMON;
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
