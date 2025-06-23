package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementació d'Oracle del FetRepositoryDialect.
 * Proveeix consultes SQL específiques per a la base de dades Oracle.
 * Gestiona la construcció de consultes per obtenir estadístiques i fets amb diferents nivells d'agrupació i filtres dimensionals.
 */
@Component
public class OracleFetRepositoryDialect implements FetRepositoryDialect {

    private static final String BASE_JOIN = " FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id ";
    private static final String BASE_WHERE_ENTORN = " WHERE f.entorn_app_id = :entornAppId ";
    private static final String FILTER_BETWEEN = " AND t.data BETWEEN :dataInici AND :dataFi ";
    private static final String FILTER_DATE = " AND t.data = :data ";
    private static final String BASE_WHERE = BASE_WHERE_ENTORN + FILTER_BETWEEN;
    private static final String SUM_INDICADOR_TEMPLATE = " SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"%s\"'))) AS sum_fets";
    private static final String DIMENSION_VALUE_TEMPLATE = " JSON_VALUE(f.dimensions_json, '$.\"%s\"') ";


    /**
     * Genera la consulta SQL per obtenir dades basades en el codi d'entorn de l'aplicació (entornAppId), un rang de dates i un valor de dimensió específic.
     * La consulta limita els resultats basant-se en els paràmetres proporcionats.
     *
     * @return Una cadena que conté la consulta SQL per obtenir els resultats filtrats segons entornAppId, rang de dates i valor de dimensió.
     */
    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery() {
        return "SELECT f.*" +
                BASE_JOIN +
                BASE_WHERE +
                "AND" + getDimensionValueQuery("' || :dimensioCodi || '") + "= :dimensioValor";
    }

    /**
     * Genera la consulta SQL per obtenir fets basats en l'entornAppId, un rang de dates i múltiples valors de dimensions.
     * Aquesta consulta selecciona els resultats de la taula `cmd_est_fet` filtrats per condicions específiques.
     *
     * Els filtres inclouen:
     * - Identificador de l'entorn d'aplicació (entornAppId).
     * - Rang de dates utilitzant les columnes de la taula de temps associada (cmd_est_temps).
     * - Valor específic de dimensions expressat com una estructura JSON.
     *
     * @return Una cadena amb la consulta SQL per obtenir els resultats filtrats segons entornAppId, rang de dates i múltiples valors de dimensió.
     */
    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery() {
        return "SELECT f.* " +
                BASE_JOIN +
                BASE_WHERE +
                "AND" + getDimensionValueQuery("' || :dimensioCodi || '") + "IN (:dimensioValor)";
    }

    /**
     * Genera una consulta SQL per obtenir dades basades en l'entorn d'aplicació (entornAppId), una data concreta i diverses
     * dimensions indicades (amb els seus valors associats).
     * La consulta inclou condicions bàsiques per entornAppId i la data proporcionada, i opcionalment, afegeix condicions
     * addicionals basades en dimensions específiques passades com a paràmetre.
     *
     * @param dimensionsFiltre Un mapa que representa les dimensions i els seus valors per aplicar com a filtre. Cada clau
     *                         és el codi d'una dimensió, i el valor associat és una llista que conté els valors possibles
     *                         per aquesta dimensió. Si no hi ha dimensions o valors específics, només s'apliquen les
     *                         condicions base.
     * @return Una cadena amb la consulta SQL generada amb les condicions proporcionades. Inclou sempre els filtres per
     *         entornAppId i data, i opcionalment les condicions definides per les dimensions.
     */
    @Override
    public String getFindByEntornAppIdAndTempsDataAndDimensionQuery(Map<String, List<String>> dimensionsFiltre) {
        String query = "SELECT f.* " +
                BASE_JOIN +
                BASE_WHERE_ENTORN +
                FILTER_DATE;

        String conditions = generateDimensionConditions(dimensionsFiltre);
        return query + conditions;
    }

    /**
     * Genera una consulta SQL per obtenir fets en base a l'identificador de l'entorn d'aplicació (entornAppId), un rang de
     * dates i els valors de diverses dimensions especificades.
     * La consulta inclou condicions específiques per filtrar les dades segons els criteris proporcionats mitjançant el mapa
     * de dimensions.
     *
     * @param dimensionsFiltre Un mapa que conté les dimensions i els seus valors de filtre. Cada clau correspon a un codi
     *                         de dimensió, mentre que el valor associat és una llista que inclou els possibles valors per
     *                         aquesta dimensió. Si el mapa és null o buit, es retorna una consulta que només inclou els
     *                         filtres d'entornAppId i el rang de dates.
     * @return Una cadena que representa la consulta SQL completa, amb els filtres per entornAppId, dates i, opcionalment,
     *         les condicions generades per les dimensions indicades.
     */
    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(Map<String, List<String>> dimensionsFiltre) {
        String query = "SELECT f.* " +
                BASE_JOIN +
                BASE_WHERE;

        String conditions = generateDimensionConditions(dimensionsFiltre);
        return query + conditions;
    }

    /**
     * Genera una consulta SQL per obtenir un valor agregat d'un indicador específic basat en l'entornAppId, 
     * un rang de dates específic, valors dimensionals i un tipus d'agregació.
     * Aquesta consulta aplica l'agregació directament a la base de dades, optimitzant el rendiment.
     *
     * @param dimensionsFiltre Un mapa on cada clau representa el codi d'una dimensió i el valor és una llista de valors
     *                         a filtrar. Si el mapa és null o buit, es generen només les condicions per entornAppId i rang
     *                         de dates.
     * @param indicadorCodi El codi de l'indicador sobre el qual s'aplicarà l'agregació.
     * @param agregacio El tipus d'agregació a aplicar (COUNT, SUM, AVERAGE, etc.).
     * @return Una cadena de text que representa la consulta SQL generada per obtenir el valor agregat.
     */
    @Override
    public String getSimpleQuery(Map<String, List<String>> dimensionsFiltre, String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {

        String querySelect = getSimpleQuerySelect(agregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGroupConditions(TableColumnsEnum.AVERAGE.equals(agregacio), unitatAgregacio);

        return "SELECT " + querySelect +
                " FROM ( SELECT " +
                (TableColumnsEnum.AVERAGE. equals(agregacio) ? "" : "t.data as data, ") +
                getSumIndicadorQuery(indicadorCodi) +
                BASE_JOIN +
                BASE_WHERE +
                queryConditions +
                queryGrouping +
                ")";
    }

    /**
     * Genera una consulta SQL per obtenir les dades d'un gràfic per a un indicador específic.
     *
     * @param dimensionsFiltre un mapa que conté les dimensions i els seus respectius valors per aplicar els filtres corresponents.
     * @param indicadorAgregacio l'objecte IndicadorAgregacio que conté informació sobre l'indicador a processar.
     * @param tempsAgregacio la unitat de període utilitzada per agrupar les dades temporalment.
     * @return la consulta SQL generada com a cadena de text, preparada per obtenir dades aplicant els filtres i agrupacions específiques.
     */
    @Override
    public String getGraficUnIndicadorQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, PeriodeUnitat tempsAgregacio) {

        String indicadorCodi = indicadorAgregacio.getIndicadorCodi();
        String querySelect = getGraficQuerySelect(indicadorAgregacio);
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGroupConditions(tempsAgregacio).replace("t.", "");
        String querySubGrouping = generateGroupConditions(indicadorAgregacio.getUnitatAgregacio() != null
                ? indicadorAgregacio.getUnitatAgregacio()
                : tempsAgregacio);


        return "SELECT " + queryAgrupacio + " as agrupacio, " +
                querySelect +
                " FROM ( SELECT " +
                querySubGrouping + ", " +
                getSumIndicadorQuery(indicadorCodi) +
                BASE_JOIN +
                BASE_WHERE +
                queryConditions +
                "GROUP BY " + querySubGrouping +
                ") " +
                "GROUP BY " + queryGrouping + " " +
                "ORDER BY agrupacio";
    }

    /**
     * Genera una consulta SQL per obtenir dades gràfiques d'un indicador amb descomposició en funció de les dimensions i la unitat de temps agregada.
     *
     * @param dimensionsFiltre representació de les dimensions de filtratge amb les seves respectives llistes de valors.
     * @param indicadorAgregacio indicadors d'agregació que conté el codi de l'indicador a consultar.
     * @param dimensioDescomposicioCodi codi de la dimensió utilitzada per fer la descomposició en el resultat de la consulta.
     * @param tempsAgregacio unitat de temps que defineix com s'agreguen els períodes (diari, mensual, anual, etc.) en la consulta.
     * @return cadena de text que conté la consulta SQL generada.
     */
    @Override
    public String getGraficUnIndicadorAmbDescomposicioAndAgrupacioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi, PeriodeUnitat tempsAgregacio) {

        String indicadorCodi = indicadorAgregacio.getIndicadorCodi();
        String querySelect = getGraficQuerySelect(indicadorAgregacio);
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGroupConditions(tempsAgregacio).replace("t.", "");
        String queryDescomposicio = getDimensionValueQuery(dimensioDescomposicioCodi);
        String querySubGrouping = generateGroupConditions(indicadorAgregacio.getUnitatAgregacio() != null
                ? indicadorAgregacio.getUnitatAgregacio()
                : tempsAgregacio);


        return "SELECT " + queryAgrupacio + " as agrupacio, " +
                "descomposicio, " +
                querySelect +
                " FROM ( SELECT " +
                querySubGrouping + ", " +
                queryDescomposicio + "AS descomposicio," +
                getSumIndicadorQuery(indicadorCodi) +
                BASE_JOIN +
                BASE_WHERE +
                queryConditions +
                "GROUP BY " + querySubGrouping + "," + queryDescomposicio +
                ") " +
                "GROUP BY " + queryGrouping + ", descomposicio " +
                "ORDER BY agrupacio, descomposicio";
    }

    /**
     * Genera una consulta SQL per obtenir dades d'un indicador específic amb descomposició per una dimensió determinada.
     *
     * @param dimensionsFiltre Mapa amb les dimensions i els seus valors a filtrar en la consulta.
     * @param indicadorAgregacio Objecte que conté la informació de l'indicador agregat, inclòs el seu codi identificador.
     * @param dimensioDescomposicioCodi Codi de la dimensió sobre la qual s'aplicarà la descomposició.
     * @return Consulta SQL generada com a cadena de text per obtenir dades amb descomposició per l'indicador especificat.
     */
    @Override
    public String getGraficUnIndicadorAmbDescomposicioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi) {

        String indicadorCodi = indicadorAgregacio.getIndicadorCodi();
        String querySelect = getGraficQuerySelect(indicadorAgregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryDescomposicio = getDimensionValueQuery(dimensioDescomposicioCodi);

        return "SELECT " + queryDescomposicio + " AS agrupacio, " +
                getSumIndicadorQuery(indicadorCodi) +
                BASE_JOIN +
                BASE_WHERE +
                queryConditions +
                "GROUP BY " + queryDescomposicio +
                "ORDER BY agrupacio";
    }

    /**
     * Genera la consulta SQL per obtenir dades d'un gràfic amb múltiples indicadors agregats segons un període temporal i filtrat per dimensions.
     *
     * @param dimensionsFiltre mapa que conté les dimensions i els seus valors per aplicar com a criteris de filtre a la consulta
     * @param indicadorsAgregacio llista d'indicadors amb informació sobre l'agregació i la unitat temporal associada
     * @param tempsAgregacio unitat temporal per a l'agregació principal de les dades
     * @return consulta SQL com a cadena de text per obtenir les dades del gràfic
     */
    @Override
    public String getGraficVarisIndicadorsQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, PeriodeUnitat tempsAgregacio) {

        boolean hasAverage = indicadorsAgregacio.stream().anyMatch(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()));
        boolean hasDataCols = indicadorsAgregacio.stream().anyMatch(ind -> TableColumnsEnum.FIRST_SEEN.equals(ind.getAgregacio()) || TableColumnsEnum.LAST_SEEN.equals(ind.getAgregacio()));

        PeriodeUnitat avgUnitat = indicadorsAgregacio.get(0).getUnitatAgregacio();
        if (hasAverage) {
            boolean thereAreDifferentUnitatAgregacio = indicadorsAgregacio.stream()
                    .skip(1) // Ignora el primer element
                    .anyMatch(indicador -> !indicador.getUnitatAgregacio().equals(avgUnitat));

            // TODO: Afegir validació per a no permetre diferents unitats d'agregació
            // Si hi ha columnes tipus AVERAGE amb diferents períodes, les separam per unitatAgregacio i fem UNION
            if (thereAreDifferentUnitatAgregacio) {
                // TODO: Modificar per funcionar semblant a taula (si es permeten difirents unitats d'agregació)
                List<List<IndicadorAgregacio>> indicadorsAgregacioByPeriode = indicadorsAgregacio.stream()
                        .collect(Collectors.groupingBy(IndicadorAgregacio::getUnitatAgregacio))
                        .values()
                        .stream()
                        .collect(Collectors.toList());

                return indicadorsAgregacioByPeriode.stream()
                        .map(listaIndicadors -> getGraficVarisIndicadorsQuery(dimensionsFiltre, listaIndicadors, tempsAgregacio))
                        .collect(Collectors.joining(" UNION "));
            }
        }

        String querySelect = getTaulaQuerySelect(indicadorsAgregacio);
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);
        String subQuerySelects = getTaulaSubQuerySelects(indicadorsAgregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String subQueryGrouping = generateGroupConditions(avgUnitat);
        String queryGrouping = generateGroupConditions(tempsAgregacio);


        return  "SELECT agrupacio, " + querySelect +
                " FROM ( SELECT " +
                (hasDataCols ? "t.data, " : "") +
                (hasAverage ? "" : generateGroupConditions(avgUnitat) + ", ") +
                queryAgrupacio + " AS agrupacio," +
                subQuerySelects +
                BASE_JOIN +
                BASE_WHERE +
                queryConditions +
                "GROUP BY " + (hasDataCols ? "t.data, " : "") + subQueryGrouping + ") " +
                "GROUP BY agrupacio " +
                "ORDER BY agrupacio"; // + queryGrouping;
    }

    @Override
    public String getTaulaQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, String dimensioAgrupacioCodi) {

        boolean hasAverage = indicadorsAgregacio.stream().anyMatch(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()));
        boolean hasDataCols = indicadorsAgregacio.stream().anyMatch(ind -> TableColumnsEnum.FIRST_SEEN.equals(ind.getAgregacio()) || TableColumnsEnum.LAST_SEEN.equals(ind.getAgregacio()));

        if (hasAverage && hasDataCols) {
            // Dividim la consulta i fem UNION posteriorment
            List<IndicadorAgregacio> indicadorsAverage = indicadorsAgregacio.stream().filter(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio())).collect(Collectors.toList());
            List<IndicadorAgregacio> indicadorsNotAverage = indicadorsAgregacio.stream().filter(ind -> !TableColumnsEnum.AVERAGE.equals(ind.getAgregacio())).collect(Collectors.toList());

            // Generem una consulta amb el format especial per a UNION
            return generaMixedUnionQuery(dimensionsFiltre, indicadorsAverage, indicadorsNotAverage, dimensioAgrupacioCodi);
        }

        PeriodeUnitat unitat = indicadorsAgregacio.get(0).getUnitatAgregacio();
        if (hasAverage) {
            boolean thereAreDifferentUnitatAgregacio = indicadorsAgregacio.stream()
                    .skip(1) // Ignora el primer element
                    .anyMatch(indicador -> !indicador.getUnitatAgregacio().equals(unitat));

            // Si hi ha columnes tipus AVERAGE amb diferents períodes, les separam per unitatAgregacio i fem UNION
            if (thereAreDifferentUnitatAgregacio) {
                List<List<IndicadorAgregacio>> indicadorsAgrupatsByPeriode = indicadorsAgregacio.stream()
                        .collect(Collectors.groupingBy(IndicadorAgregacio::getUnitatAgregacio))
                        .values()
                        .stream()
                        .collect(Collectors.toList());

                // Generem una consulta amb el format especial per a UNION
                return generaAvgUnionQuery(dimensionsFiltre, indicadorsAgrupatsByPeriode, dimensioAgrupacioCodi);
            }
        }

        IndicadorAgregacio primerIndicador = indicadorsAgregacio.get(0);
        String querySelect = getTaulaQuerySelect(indicadorsAgregacio);
        String subQuerySelects = getTaulaSubQuerySelects(indicadorsAgregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGroupConditions(hasAverage, primerIndicador.getUnitatAgregacio());
        String queryAgrupacio = getDimensionValueQuery(dimensioAgrupacioCodi);


        return "SELECT agrupacio, " + querySelect +
                " FROM ( SELECT " +
                (hasAverage ? generateGroupConditions(unitat) + ", " : "t.data as data, ") +
                queryAgrupacio + " AS agrupacio," +
                subQuerySelects +
                BASE_JOIN +
                BASE_WHERE +
                queryConditions +
                queryGrouping + "," + queryAgrupacio + ") " +
                "GROUP BY agrupacio " +
                "ORDER BY agrupacio";
    }

    /**
     * Genera una consulta SQL amb format especial per a UNION entre diferents llistes d'indicadors.
     * 
     * @param dimensionsFiltre Filtre de dimensions
     * @param indicadorsLists Llista de llistes d'indicadors agrupats per algun criteri
     * @param dimensioAgrupacioCodi Codi de la dimensió d'agrupació
     * @return Consulta SQL amb format especial per a UNION
     */
    private String generaAvgUnionQuery(Map<String, List<String>> dimensionsFiltre, List<List<IndicadorAgregacio>> indicadorsLists, String dimensioAgrupacioCodi) {
        // Obtenim tots els indicadors en l'ordre original
        List<IndicadorAgregacio> allIndicadors = indicadorsLists.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Generem les subconsultes per a cada llista d'indicadors
        String unionSubqueries = indicadorsLists.stream()
                .map(indicadors -> generateUnionSubquery(dimensionsFiltre, indicadors, dimensioAgrupacioCodi, allIndicadors))
                .collect(Collectors.joining(" UNION ALL "));

        return generaUnionQuery(allIndicadors, unionSubqueries);
    }

    /**
     * Genera una consulta SQL amb format especial per a UNION entre dues llistes d'indicadors.
     *
     * @param dimensionsFiltre Filtre de dimensions
     * @param indicadorsAverage Llista d'indicadors amb agregació AVERAGE
     * @param indicadorsNotAverage Llista d'indicadors sense agregació AVERAGE
     * @param dimensioAgrupacioCodi Codi de la dimensió d'agrupació
     * @return Consulta SQL amb format especial per a UNION
     */
    private String generaMixedUnionQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAverage, List<IndicadorAgregacio> indicadorsNotAverage, String dimensioAgrupacioCodi) {
        // Obtenim tots els indicadors en l'ordre original
        List<IndicadorAgregacio> allIndicadors = new java.util.ArrayList<>();
        allIndicadors.addAll(indicadorsAverage);
        allIndicadors.addAll(indicadorsNotAverage);

        // Generem les subconsultes per a cada tipus d'indicador
        String averageSubquery = generateUnionSubquery(dimensionsFiltre, indicadorsAverage, dimensioAgrupacioCodi, allIndicadors);
        String notAverageSubquery = generateUnionSubquery(dimensionsFiltre, indicadorsNotAverage, dimensioAgrupacioCodi, allIndicadors);
        String unionSubqueries = averageSubquery + " UNION ALL " + notAverageSubquery;

        return generaUnionQuery(allIndicadors, unionSubqueries);
    }

    private static String generaUnionQuery(List<IndicadorAgregacio> allIndicadors, String unionSubqueries) {
        // Obtenim els noms de columnes en l'ordre original dels indicadors
        Set<String> allResultColumns = allIndicadors.stream()
                .map(ind -> {
                    switch (ind.getAgregacio()) {
                        case AVERAGE: return "average_result" + getIndicadorSuffix(ind.getIndicadorCodi());
                        case FIRST_SEEN: return "first_seen" + getIndicadorSuffix(ind.getIndicadorCodi());
                        case LAST_SEEN: return "last_seen" + getIndicadorSuffix(ind.getIndicadorCodi());
                        default: return "total_sum" + getIndicadorSuffix(ind.getIndicadorCodi());
                    }
                })
                .collect(Collectors.toSet());

        // Generem el SELECT exterior amb MAX per a cada columna
        String outerSelect = allResultColumns.stream()
                .map(col -> "MAX(" + col + ") as " + col)
                .collect(Collectors.joining(", "));

        // Retornem la consulta completa
        return "SELECT agrupacio, " + outerSelect +
                " FROM (" + unionSubqueries + ")" +
                " GROUP BY agrupacio" +
                " ORDER BY agrupacio";
    }

    /**
     * Genera una subconsulta per a una llista d'indicadors, incloent columnes NULL per a indicadors que no estan a la llista.
     * 
     * @param dimensionsFiltre Filtre de dimensions
     * @param indicadors Llista d'indicadors
     * @param dimensioAgrupacioCodi Codi de la dimensió d'agrupació
     * @return Subconsulta SQL
     */
    private String generateUnionSubquery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadors, String dimensioAgrupacioCodi, List<IndicadorAgregacio> allIndicadors) {
        if (indicadors.isEmpty()) {
            return "";
        }

        IndicadorAgregacio primerIndicador = indicadors.get(0);
        boolean hasDataQuery = indicadors.stream().anyMatch(ind -> TableColumnsEnum.FIRST_SEEN.equals(ind.getAgregacio()) || TableColumnsEnum.LAST_SEEN.equals(ind.getAgregacio()));
        PeriodeUnitat avgUnitat = primerIndicador.getUnitatAgregacio();

        String querySelect = allIndicadors != null
                ? allIndicadors.stream()
                .map(indicador -> {
                    if (indicadors.contains(indicador)) {
                        return getSimpleQuerySelect(indicador.getAgregacio(), indicador.getIndicadorCodi());
                    } else if (TableColumnsEnum.AVERAGE.equals(indicador.getAgregacio())) {
                        return " null AS average_result" + getIndicadorSuffix(indicador.getIndicadorCodi());
                    } else if (TableColumnsEnum.FIRST_SEEN.equals(indicador.getAgregacio())) {
                        return " null AS first_seen" + getIndicadorSuffix(indicador.getIndicadorCodi());
                    } else if (TableColumnsEnum.LAST_SEEN.equals(indicador.getAgregacio())) {
                        return " null AS last_seen" + getIndicadorSuffix(indicador.getIndicadorCodi());
                    } else {
                        return " null AS total_sum" + getIndicadorSuffix(indicador.getIndicadorCodi());
                    }
                })
                .collect(Collectors.joining(", "))
                : "";

        String subQuerySelects = getTaulaSubQuerySelects(indicadors);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGroupConditions(avgUnitat) + ", ";
        String queryAgrupacio = getDimensionValueQuery(dimensioAgrupacioCodi);

        return "SELECT agrupacio, " + querySelect +
                " FROM ( SELECT " +
                (hasDataQuery ?  "t.data as data, " : queryGrouping) +
                queryAgrupacio + " AS agrupacio," +
                subQuerySelects +
                BASE_JOIN +
                BASE_WHERE +
                queryConditions +
                "GROUP BY " + (hasDataQuery ?  "t.data, " : queryGrouping) + queryAgrupacio + ") " +
                "GROUP BY agrupacio";
    }

    private String getTaulaSubQuerySelects(List<IndicadorAgregacio> indicadorsAgregacio) {
        // Només volem un select per indicadorCodi
        return indicadorsAgregacio.stream()
                .map(IndicadorAgregacio::getIndicadorCodi)
                .distinct()
                .map(indicadorCodi -> getSumIndicadorQuery(indicadorCodi) + getIndicadorSuffix(indicadorCodi))
                .collect(Collectors.joining(", "));
    }

    private String getTaulaQuerySelect(List<IndicadorAgregacio> indicadorsAgregacio) {
        return indicadorsAgregacio.stream()
                .map(ind -> getSimpleQuerySelect(ind.getAgregacio(), ind.getIndicadorCodi()) )
                .collect(Collectors.joining(", "));
    }



    private String getGraficQuerySelect(IndicadorAgregacio indicadorAgregacio) {
        return getSimpleQuerySelect(indicadorAgregacio.getAgregacio());
    }

    private String getSimpleQuerySelect(TableColumnsEnum agregacio) {
        return getSimpleQuerySelect(agregacio, null);
    }
    private String getSimpleQuerySelect(TableColumnsEnum agregacio, String indicadorCodi) {
        String suffix = getIndicadorSuffix(indicadorCodi);
        switch (agregacio) {
            case AVERAGE:
                return "AVG(sum_fets" + suffix + ") AS average_result" + suffix;
            case FIRST_SEEN:
                return "CASE WHEN SUM(sum_fets" + suffix + ") > 0 THEN MIN(data) ELSE NULL END AS first_seen" + suffix;
            case LAST_SEEN:
                return "CASE WHEN SUM(sum_fets" + suffix + ") > 0 THEN MAX(data) ELSE NULL END AS last_seen" + suffix;
            default:
                return "SUM(sum_fets" + suffix + ") AS total_sum" + suffix;
        }
    }

    private static String getSumIndicadorQuery(String indicadorCodi) {
        return String.format(SUM_INDICADOR_TEMPLATE, indicadorCodi);
    }

    private static String getDimensionValueQuery(String dimensioCodi) {
        return String.format(DIMENSION_VALUE_TEMPLATE, dimensioCodi);
    }


    /*
     * Genera la condició SQL per filtrar resultats segons valors de dimensions especificats.
     * Construeix una condició SQL que inclou els valors de filtre proporcionats per a cada dimensió.
     *
     * @param dimensionsFiltre Un mapa on la clau és el codi de la dimensió i el valor és una llista de valors a filtrar.
     *                         Si el mapa és null o buit, es retorna una cadena buida.
     * @return Una cadena que representa la condició SQL generada. Si no hi ha condicions vàlides, retorna una cadena buida.
     */
    static String generateDimensionConditions(Map<String, List<String>> dimensionsFiltre) {
        if (dimensionsFiltre == null || dimensionsFiltre.isEmpty()) {
            return "";
        }

        return dimensionsFiltre.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> {
                    String codi = entry.getKey();
                    List<String> valors = entry.getValue();

                    if (valors.size() == 1) {
                        return "AND" + getDimensionValueQuery(codi) + "= '" + valors.get(0) + "' ";
                    } else {
                        String valorsStr = valors.stream().map(valor -> "'" + valor + "'").collect(Collectors.joining(","));
                        return "AND" + getDimensionValueQuery(codi) + "IN (" + valorsStr + ") ";
                    }
                })
                .collect(Collectors.joining(" "));
    }

    // TODO: Girar i posar any/mes/dia
    private static String generateGraficAgrupacioConditions(PeriodeUnitat tempsAgregacio) {
        switch (tempsAgregacio) {
            case SETMANA: return "setmana || '/' || anualitat";
            case MES: return "mes || '/' || anualitat";
            case TRIMESTRE: return "trimestre || '/' || anualitat";
            case ANY: return "anualitat";
            default: return "dia || '/' || mes || '/' || anualitat";
        }
    }

    private static String generateGroupConditions(PeriodeUnitat tempsAgregacio) {
        if (tempsAgregacio == null)
            return "t.anualitat, t.trimestre, t.mes, t.setmana, t.dia";

        switch (tempsAgregacio) {
            case SETMANA: return "t.anualitat, t.trimestre, t.mes, t.setmana";
            case MES: return "t.anualitat, t.trimestre, t.mes";
            case TRIMESTRE: return "t.anualitat, t.trimestre";
            case ANY: return "t.anualitat";
            default: return "t.anualitat, t.trimestre, t.mes, t.setmana, t.dia";
        }
    }

    private static String generateGroupConditions(boolean average, PeriodeUnitat unitatAgregacio) {

        if (average) {
            return "GROUP BY " + getGrupping(unitatAgregacio);
        } else {
            return "GROUP BY t.data";
        }
    }

    private static String getGrupping(PeriodeUnitat unitatAgregacio) {
        if (unitatAgregacio != null) {
            switch (unitatAgregacio) {
                case DIA: return "t.anualitat, t.trimestre, t.mes, t.setmana, t.dia";
                case SETMANA: return "t.anualitat, t.trimestre, t.mes, t.setmana";
                case MES: return "t.anualitat, t.trimestre, t.mes";
                case TRIMESTRE: return "t.anualitat, t.trimestre";
                case ANY: return "t.anualitat";
            }
        }
        return "t.data";
    }

    private static String getIndicadorSuffix(String indicadorCodi) {
        return indicadorCodi != null ? "_" + indicadorCodi : "";
    }

}
