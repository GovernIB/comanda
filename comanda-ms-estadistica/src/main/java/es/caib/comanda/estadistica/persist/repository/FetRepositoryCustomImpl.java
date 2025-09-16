package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.repository.dialect.FetRepositoryDialectFactory;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.ENGLISH);

    static {
        NUMBER_FORMAT.setMaximumFractionDigits(2);
        NUMBER_FORMAT.setMinimumFractionDigits(0);
		NUMBER_FORMAT.setGroupingUsed(false);
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

        List<Map<String, String>> result = resultList.stream()
                .map(rowArray -> Map.of(
                        "agrupacio", Objects.toString(rowArray[0]),
                        indicadorAgregacio.getIndicadorCodi(), formatCellValue(rowArray[1], indicadorAgregacio.getAgregacio())))
                .collect(Collectors.toList());

        if (TableColumnsEnum.PERCENTAGE.equals(indicadorAgregacio.getAgregacio())) {
            calculatePercentages(result, indicadorAgregacio.getIndicadorCodi());
        }
        return result;
    }

    @Override
    public List<Map<String, String>> getValorsGraficUnIndicadorAmdDescomposicio(Long entornAppId, LocalDate dataInici, LocalDate dataFi, Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi, PeriodeUnitat tempsAgregacio) {

        String sql = dialectFactory.getDialect().getGraficUnIndicadorAmbDescomposicioAndAgrupacioQuery(dimensionsFiltre, indicadorAgregacio, dimensioDescomposicioCodi, tempsAgregacio);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        List<Object[]> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return new ArrayList<>(); // Retorna un Map buit si no hi ha resultats
        }

        List<Map<String, String>> result = resultList.stream()
                .map(rowArray -> Map.of(
                        "agrupacio", Objects.toString(rowArray[0]),
                        "descomposicio", Objects.toString(rowArray[1]),
                        indicadorAgregacio.getIndicadorCodi(), formatCellValue(rowArray[2], indicadorAgregacio.getAgregacio())))
                .collect(Collectors.toList());

        if (TableColumnsEnum.PERCENTAGE.equals(indicadorAgregacio.getAgregacio())) {
            calculatePercentages(result, indicadorAgregacio.getIndicadorCodi());
        }
        return result;
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

        List<Map<String, String>> result = resultList.stream()
                .map(rowArray -> Map.of(
                        "agrupacio", Objects.toString(rowArray[0]),
                        indicadorAgregacio.getIndicadorCodi(), formatCellValue(rowArray[1], indicadorAgregacio.getAgregacio())))
                .collect(Collectors.toList());

        if (TableColumnsEnum.PERCENTAGE.equals(indicadorAgregacio.getAgregacio())) {
            calculatePercentages(result, indicadorAgregacio.getIndicadorCodi());
        }
        return result;
    }

    @Override
    public List<Map<String, String>> getValorsGraficVarisIndicadors(Long entornAppId, LocalDate dataInici, LocalDate dataFi, Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, PeriodeUnitat tempsAgregacio) {

        ColumnesConsulta columnesConsulta = new ColumnesConsulta(indicadorsAgregacio);
        String sql = dialectFactory.getDialect().getGraficVarisIndicadorsQuery(dimensionsFiltre, columnesConsulta.getIndicadorsFiltrats(), tempsAgregacio);

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("entornAppId", entornAppId);
        query.setParameter("dataInici", dataInici);
        query.setParameter("dataFi", dataFi);

        List<Object[]> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return new ArrayList<>(); // Retorna un Map buit si no hi ha resultats
        }

        List<Map<String, String>> result = resultList.stream()
                .map(rowArray -> convertRowToMap(
                        rowArray,
                        columnesConsulta.getColumnNames(),
                        indicadorsAgregacio,
                        columnesConsulta.getIndexColumnesFiltrades()))
                .collect(Collectors.toList());

        processPercentages(result,
                columnesConsulta.getIndicadorsPercentatge(),
                columnesConsulta.getIndicadorsFiltrats(),
                columnesConsulta.getColumnNames(),
                indicadorsAgregacio);
        return result;
    }

    @Override
    public List<Map<String, String>> getValorsTaulaAgregat(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            List<IndicadorAgregacio> indicadorsAgregacio,
            String dimensioAgrupacioCodi) throws ReportGenerationException {

        try {
            ColumnesConsulta columnesConsulta = new ColumnesConsulta(indicadorsAgregacio);
            String sql = dialectFactory.getDialect().getTaulaQuery(dimensionsFiltre, columnesConsulta.getIndicadorsFiltrats(), dimensioAgrupacioCodi);

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("entornAppId", entornAppId);
            query.setParameter("dataInici", dataInici);
            query.setParameter("dataFi", dataFi);

            List<Object[]> resultList = query.getResultList();
            if (resultList.isEmpty()) {
                return new ArrayList<>(); // Retorna un Map buit si no hi ha resultats
            }

            List<Map<String, String>> result = resultList.stream()
                    .map(rowArray -> convertRowToMap(
                            rowArray,
                            columnesConsulta.getColumnNames(),
                            indicadorsAgregacio,
                            columnesConsulta.getIndexColumnesFiltrades()))
                    .collect(Collectors.toList());

            processPercentages(result,
                    columnesConsulta.getIndicadorsPercentatge(),
                    columnesConsulta.getIndicadorsFiltrats(),
                    columnesConsulta.getColumnNames(),
                    indicadorsAgregacio);
            return result;
        } catch (Exception e) {
            throw new ReportGenerationException(DashboardItem.class, e.getMessage(), e.getCause());
        }
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
    private Map<String, String> convertRowToMap(Object[] rowArray, String[] columnNames, List<IndicadorAgregacio> indicadorsAgregacio, List<Integer> indexColumnesFiltrades) {
        Map<String, String> fila = new LinkedHashMap<>();
        fila.put(ColumnesConsulta.GROUP_COLUMN_NAME, Objects.toString(rowArray[0], null));

        int rowIndex = 1; // Índex per accedir al valor corresponent
        for (int colIndex : indexColumnesFiltrades) {
            IndicadorAgregacio indicador = indicadorsAgregacio.get(colIndex - 1);
            var value = formatCellValue(rowArray[rowIndex], indicador.getAgregacio());
            fila.put(columnNames[colIndex], value);
            rowIndex++; // Incrementem de manera explícita després del processament
        }
        return fila;
    }

    private String formatCellValue(Object cellValue, TableColumnsEnum agregacio) {
        if (cellValue == null) {
            return (TableColumnsEnum.FIRST_SEEN.equals(agregacio) || TableColumnsEnum.LAST_SEEN.equals(agregacio)) ? null : "0";
        }
        return cellFormat(cellValue);
    }

    private String cellFormat(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof String && ((String) valor).isEmpty()) {
            return null;
        }
        if (valor instanceof Timestamp) {
            return ((Timestamp) valor).toLocalDateTime().toLocalDate().format(DATE_FORMATTER);
        }
        if (valor instanceof Number) {
            return NUMBER_FORMAT.format(((Number) valor).doubleValue());
        }
        return valor.toString();
    }

    @Getter
    private static class ColumnesConsulta {
        private static final String GROUP_COLUMN_NAME = "agrupacio";

        List<IndicadorAgregacio> allIndicadors;
        List<IndicadorAgregacio> indicadorsPercentatge;
        List<IndicadorAgregacio> indicadorsFiltrats;
        String[] columnNames;
        String[] columnNamesForQuery;
        List<Integer> indexColumnesFiltrades;

        public ColumnesConsulta(List<IndicadorAgregacio> allIndicadors) {
            this.allIndicadors = allIndicadors;
            this.indicadorsPercentatge = filterIndicadorsPercentatge(allIndicadors);
            this.indicadorsFiltrats = filterIndicadorsQuery(allIndicadors);
            this.columnNames = generateColumnNames(allIndicadors);
            this.columnNamesForQuery = generateFilteredColumnNames(columnNames, indicadorsFiltrats, allIndicadors);
            this.indexColumnesFiltrades = getFilteredColumnIndexes(columnNames, columnNamesForQuery);
        }

        private List<IndicadorAgregacio> filterIndicadorsPercentatge(List<IndicadorAgregacio> indicadors) {
            return indicadors.stream()
                    .filter(i -> TableColumnsEnum.PERCENTAGE.equals(i.getAgregacio()))
                    .collect(Collectors.toList());
        }

        private List<IndicadorAgregacio> filterIndicadorsQuery(List<IndicadorAgregacio> indicadors) {
            return indicadors.stream()
                    .filter(i -> !TableColumnsEnum.PERCENTAGE.equals(i.getAgregacio()) ||
                            indicadors.stream()
                                    .filter(j -> j.getIndicadorCodi().equals(i.getIndicadorCodi()))
                                    .allMatch(j -> isPercentageOrAverage(j.getAgregacio())))
                    .collect(Collectors.toList());
        }

        private String[] generateColumnNames(List<IndicadorAgregacio> indicadors) {
            List<String> columnNames = new ArrayList<>();
            columnNames.add(GROUP_COLUMN_NAME);
            for (int i = 1; i <= indicadors.size(); i++) {
                columnNames.add("col" + i);
            }
            return columnNames.toArray(new String[0]);
        }

        private String[] generateFilteredColumnNames(String[] columnNames, List<IndicadorAgregacio> filteredIndicadors, List<IndicadorAgregacio> allIndicadors) {
            String[] result = new String[filteredIndicadors.size() + 1];
            result[0] = GROUP_COLUMN_NAME;
            int queryIndex = 1;
            for (int i = 1; i < columnNames.length; i++) {
                if (filteredIndicadors.contains(allIndicadors.get(i - 1))) {
                    result[queryIndex++] = columnNames[i];
                }
            }
            return result;
        }

        private List<Integer> getFilteredColumnIndexes(String[] columnNames, String[] columnNamesForQuery) {
            List<Integer> indexList = new ArrayList<>();
            for (int i = 0; i < columnNames.length; i++) {
                if (isColumnPresent(columnNamesForQuery, columnNames[i])) {
                    indexList.add(i);
                }
            }
            // Eliminal la columna de agrupació
            indexList.remove(0);
            return indexList;
        }

        private boolean isColumnPresent(String[] filteredColumnNames, String columnName) {
            return Arrays.asList(filteredColumnNames).contains(columnName);
        }

        private boolean isPercentageOrAverage(TableColumnsEnum agregacio) {
            return TableColumnsEnum.PERCENTAGE.equals(agregacio) || TableColumnsEnum.AVERAGE.equals(agregacio);
        }
    }

}
