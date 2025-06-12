package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.repository.dialect.FetRepositoryDialectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementació de l'interfície personalitzada FetRepositoryCustom. Proporciona funcionalitats específiques per
 * consultar entitats de tipus FetEntity utilitzant consultes SQL personalitzades segons el dialecte configurat.
 *
 * Aquesta classe utilitza EntityManager per executar consultes natives SQL que inclouen criteris de filtre com
 * l'identificador de l'entorn de l'aplicació, rangs de dates i valors de dimensions especificats.
 *
 * @author Límit Tecnologies
 */
@Repository
public class FetRepositoryCustomImpl implements FetRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FetRepositoryDialectFactory dialectFactory;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    static {
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        NUMBER_FORMAT.setMinimumFractionDigits(0);
    }


    @Override
    public List<FetEntity> findByEntornAppIdAndTempsDataBetweenAndDimensionValue(
            Long entornAppId, 
            LocalDate dataInici, 
            LocalDate dataFi,
            String dimensioCodi,
            String dimensioValor) {

        String sql = dialectFactory.getDialect().getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery();

        Query query = entityManager.createNativeQuery(sql, FetEntity.class);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);
        query.setParameter("dimensioCodi", dimensioCodi);
        query.setParameter("dimensioValor", dimensioValor);

        return query.getResultList();
    }

    @Override
    public List<FetEntity> findByEntornAppIdAndTempsDataBetweenAndDimensionValues(
            Long entornAppId, 
            LocalDate dataInici, 
            LocalDate dataFi,
            String dimensioCodi,
            List<String> valors) {

        String sql = dialectFactory.getDialect().getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery();

        Query query = entityManager.createNativeQuery(sql, FetEntity.class);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);
        query.setParameter("dimensioCodi", dimensioCodi);
        query.setParameter("dimensioValors", valors);

        return query.getResultList();
    }

    @Override
    public List<FetEntity> findByEntornAppIdAndTempsDataAndDimensions(
            Long entornAppId,
            LocalDate data,
            Map<String, List<String>> dimensionsFiltre) {

        String sql = dialectFactory.getDialect().getFindByEntornAppIdAndTempsDataAndDimensionQuery(dimensionsFiltre);

        Query query = entityManager.createNativeQuery(sql, FetEntity.class);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("data", data);

        return query.getResultList();
    }

    @Override
    public List<FetEntity> findByEntornAppIdAndTempsDataBetweenAndDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre) {

        String sql = dialectFactory.getDialect().getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(dimensionsFiltre);

        Query query = entityManager.createNativeQuery(sql, FetEntity.class);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        return query.getResultList();
    }

    @Override
    public String getValorSimpleAgregat(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            IndicadorAgregacio indicadorAgregacio) {

        String indicadorCodi = indicadorAgregacio.getIndicadorCodi();
        TableColumnsEnum agregacio = indicadorAgregacio.getAgregacio();
        PeriodeUnitat unitatAgregacio = indicadorAgregacio.getUnitatAgregacio();

        String sql = dialectFactory.getDialect().getSimpleQuery(dimensionsFiltre, indicadorCodi, agregacio, unitatAgregacio);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        return formatCellValue(query.getSingleResult(), agregacio);
    }

    @Override
    public List<Map<String, String>> getValorsGraficUnIndicador(Long entornAppId, LocalDate dataInici, LocalDate dataFi, Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, PeriodeUnitat tempsAgregacio) {

        String sql = dialectFactory.getDialect().getGraficUnIndicadorQuery(dimensionsFiltre, indicadorAgregacio, tempsAgregacio);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        List<Object[]> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return new ArrayList<>(); // Retorna un Map buit si no hi ha resultats
        }

        // TODO:
        return List.of();
    }

    @Override
    public List<Map<String, String>> getValorsGraficUnIndicadorAmdDescomposicio(Long entornAppId, LocalDate dataInici, LocalDate dataFi, Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi, PeriodeUnitat tempsAgregacio) {

        String sql = dialectFactory.getDialect().getGraficUnIndicadorAmbDescomposicioQuery(dimensionsFiltre, indicadorAgregacio, dimensioDescomposicioCodi, tempsAgregacio);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        List<Object[]> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return new ArrayList<>(); // Retorna un Map buit si no hi ha resultats
        }

        // TODO:
        return List.of();
    }

    @Override
    public List<Map<String, String>> getValorsGraficUnIndicadorAmdDescomposicio(Long entornAppId, LocalDate dataInici, LocalDate dataFi, Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi) {

        String sql = dialectFactory.getDialect().getGraficUnIndicadorAmbDescomposicioQuery(dimensionsFiltre, indicadorAgregacio, dimensioDescomposicioCodi);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        List<Object[]> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return new ArrayList<>(); // Retorna un Map buit si no hi ha resultats
        }

        // TODO:
        return List.of();
    }

    @Override
    public List<Map<String, String>> getValorsGraficVarisIndicadors(Long entornAppId, LocalDate dataInici, LocalDate dataFi, Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, PeriodeUnitat tempsAgregacio) {

        // Filtrar indicadors tipus percentatge
        List<IndicadorAgregacio> indicadorsPercentatge = filterIndicadorsPercentatge(indicadorsAgregacio);
        // Filtrar indicadors per query (excloent percentatges que tinguin altres agregacions del mateix indicador)
        List<IndicadorAgregacio> filteredIndicadors = filterIndicadorsQuery(indicadorsAgregacio);

        // Tots els nom de les columnes
        String[] columnNames = ConsultaEstadisticaHelper.getColumnNames(indicadorsAgregacio);
        // Els noms de les columnes excloent les de percentatges que tinguin altres agregacions del mateix indicador
        String[] columnNamesForQuery = createColumnNamesArray(columnNames, filteredIndicadors, indicadorsAgregacio);

        String sql = dialectFactory.getDialect().getGraficVarisIndicadorsQuery(dimensionsFiltre, filteredIndicadors, tempsAgregacio);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        List<Object[]> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return new ArrayList<>(); // Retorna un Map buit si no hi ha resultats
        }

        // TODO:
        return List.of();
    }

    @Override
    public List<Map<String, String>> getValorsTaulaAgregat(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            List<IndicadorAgregacio> indicadorsAgregacio,
            String dimensioAgrupacioCodi) {

        // Filtrar indicadors tipus percentatge
        List<IndicadorAgregacio> indicadorsPercentatge = filterIndicadorsPercentatge(indicadorsAgregacio);
        // Filtrar indicadors per query (excloent percentatges que tinguin altres agregacions del mateix indicador)
        List<IndicadorAgregacio> filteredIndicadors = filterIndicadorsQuery(indicadorsAgregacio);

        // Tots els nom de les columnes
        String[] columnNames = ConsultaEstadisticaHelper.getColumnNames(indicadorsAgregacio);
        // Els noms de les columnes excloent les de percentatges que tinguin altres agregacions del mateix indicador
        String[] columnNamesForQuery = createColumnNamesArray(columnNames, filteredIndicadors, indicadorsAgregacio);

        String sql = dialectFactory.getDialect().getTaulaQuery(dimensionsFiltre, filteredIndicadors, dimensioAgrupacioCodi);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        List<Object[]> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return new ArrayList<>(); // Retorna un Map buit si no hi ha resultats
        }

        List<Map<String, String>> result = resultList.stream()
                .map(rowArray -> convertRowToMap(rowArray, columnNamesForQuery, indicadorsAgregacio))
                .collect(Collectors.toList());

        processPercentages(result, indicadorsPercentatge, filteredIndicadors, columnNames, indicadorsAgregacio);
        return result;
    }

    private List<IndicadorAgregacio> filterIndicadorsPercentatge(List<IndicadorAgregacio> indicadorsAgregacio) {
        return indicadorsAgregacio.stream()
                .filter(i -> TableColumnsEnum.PERCENTAGE.equals(i.getAgregacio()))
                .collect(Collectors.toList());
    }

    private List<IndicadorAgregacio> filterIndicadorsQuery(List<IndicadorAgregacio> indicadorsAgregacio) {
        return indicadorsAgregacio.stream()
                .filter(i -> !TableColumnsEnum.PERCENTAGE.equals(i.getAgregacio()) ||
                        indicadorsAgregacio.stream()
                                .filter(j -> j.getIndicadorCodi().equals(i.getIndicadorCodi()))
                                .allMatch(j -> TableColumnsEnum.PERCENTAGE.equals(j.getAgregacio())
                                        || TableColumnsEnum.AVERAGE.equals(j.getAgregacio())))
                .collect(Collectors.toList());
    }

    private String[] createColumnNamesArray(String[] columnNames, List<IndicadorAgregacio> filteredIndicadors, List<IndicadorAgregacio> allIndicadors) {
        String[] columnNamesQuery = new String[filteredIndicadors.size() + 1];
        columnNamesQuery[0] = columnNames[0];
        int queryIndex = 1;
        for (int i = 1; i < columnNames.length; i++) {
            if (filteredIndicadors.contains(allIndicadors.get(i - 1))) {
                columnNamesQuery[queryIndex++] = columnNames[i];
            }
        }
        return columnNamesQuery;
    }

    private void processPercentages(List<Map<String, String>> result,
                                    List<IndicadorAgregacio> indicadorsPercentatge,
                                    List<IndicadorAgregacio> filteredIndicadors,
                                    String[] columnNames,
                                    List<IndicadorAgregacio> allIndicadors) {
        for (IndicadorAgregacio indPerc : indicadorsPercentatge) {
            int percIndex = allIndicadors.indexOf(indPerc);
            String percColumnName = columnNames[percIndex + 1];
            if (filteredIndicadors.contains(indPerc)) {
                // Calcular percentatges per indicadors sense altres agregacions
                calculatePercentages(result, percColumnName);
            } else {
                // Càlcul percentatges a partir d'altres agregacions
                calculateDependentPercentages(result, indPerc, allIndicadors, columnNames);
            }
        }
    }

    private void calculatePercentages(List<Map<String, String>> result, String columnName) {
        double total = result.stream()
                .mapToDouble(row -> parseRowValue(row, columnName))
                .sum();
        if (total == 0) {
            return;
        }
        for (Map<String, String> row : result) {
            double value = parseRowValue(row, columnName);
            row.put(columnName, NUMBER_FORMAT.format((value / total) * 100));
        }
    }

    private void calculateDependentPercentages(List<Map<String, String>> result,
                                               IndicadorAgregacio indPerc,
                                               List<IndicadorAgregacio> allIndicadors,
                                               String[] columnNames) {
        int percIndex = allIndicadors.indexOf(indPerc);
        int baseIndex = findBaseIndex(indPerc, allIndicadors);
        if (baseIndex >= 0) {
            String baseColumnName = columnNames[baseIndex + 1];
            String percColumnName = columnNames[percIndex + 1];
            double total = result.stream()
                    .mapToDouble(row -> parseRowValue(row, baseColumnName))
                    .sum();
            for (Map<String, String> row : result) {
                double baseValue = parseRowValue(row, baseColumnName);
                String resultValue = total == 0 ? "0" : NUMBER_FORMAT.format((baseValue / total) * 100);
                row.put(percColumnName, resultValue);
            }
        }
    }

    private int findBaseIndex(IndicadorAgregacio indPerc, List<IndicadorAgregacio> allIndicadors) {
        for (int i = 0; i < allIndicadors.size(); i++) {
            if (allIndicadors.get(i).getIndicadorCodi().equals(indPerc.getIndicadorCodi())
                    && !TableColumnsEnum.PERCENTAGE.equals(allIndicadors.get(i).getAgregacio())) {
                return i;
            }
        }
        return -1;
    }

    private double parseRowValue(Map<String, String> row, String columnName) {
        return Double.parseDouble(row.get(columnName).replace(",", "."));
    }


    // Helper per convertir una fila del resultat de la query en un map
    private Map<String, String> convertRowToMap(Object[] rowArray, String[] columnNames, List<IndicadorAgregacio> indicadorsAgregacio) {
        Map<String, String> fila = new HashMap<>();
        for (int i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            String value;

            if (i == 0) {
                value = Objects.toString(rowArray[i], null);
            } else {
                IndicadorAgregacio indicador = indicadorsAgregacio.get(i - 1);
                value = formatCellValue(rowArray[i], indicador.getAgregacio());
            }
            fila.put(columnName, value);
        }
        return fila;
    }

    private String formatCellValue(Object cellValue, TableColumnsEnum agregacio) {
        if (cellValue == null) {
            return (TableColumnsEnum.FIRST_SEEN.equals(agregacio) || TableColumnsEnum.LAST_SEEN.equals(agregacio)) ? null : "0";
        }
        if (TableColumnsEnum.FIRST_SEEN.equals(agregacio) || TableColumnsEnum.LAST_SEEN.equals(agregacio)) {
            LocalDate date = ((Timestamp) cellValue).toLocalDateTime().toLocalDate();
            return date.format(DATE_FORMATTER);
        }
        return NUMBER_FORMAT.format(((BigDecimal) cellValue).doubleValue());
    }

}
