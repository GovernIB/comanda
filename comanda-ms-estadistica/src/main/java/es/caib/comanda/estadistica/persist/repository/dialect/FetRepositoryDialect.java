package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;

import java.util.List;
import java.util.Map;

/**
 * Interfície per proveir consultes SQL específiques del dialecte per FetRepository.
 * Les implementacions d'aquesta interfície proporcionen consultes SQL per dialectes específics de base de dades.
 */
public interface FetRepositoryDialect {

    /**
     * Obté la consulta SQL per trobar fets per entornAppId, període de temps i valor de dimensió.
     *
     * @return La cadena de consulta SQL
     */
    String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery();

    /**
     * Obté la consulta SQL per trobar fets per entornAppId, període de temps i múltiples valors de dimensió.
     *
     * @return La cadena de consulta SQL
     */
    String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery();

    /**
     * Genera la consulta SQL per trobar fets basant-se en l'entornAppId, la data concreta i una sèrie de dimensions
     * proporcionades com a filtres (amb els seus valors corresponents).
     *
     * @param dimensionsFiltre Un mapa on cada clau representa un nom de dimensió i el valor correspon a una llista
     *                         de valors per filtrar. Pot incloure múltiples dimensions amb varis valors opcionals.
     * @return Una cadena que conté la consulta SQL generada amb els filtres aplicats segons les dimensions
     *         proporcionades. Si el mapa és buit o null, la consulta només inclou les condicions base.
     */
    String getFindByEntornAppIdAndTempsDataAndDimensionQuery(Map<String, List<String>> dimensionsFiltre);

    /**
     * Genera una consulta SQL per obtenir fets basats en l'entornAppId, un rang de dates específic i valors dimensionals.
     * Aquesta consulta inclou opcionalment filtres per dimensions i els seus corresponents valors.
     *
     * @param dimensionsFiltre Un mapa on cada clau representa el codi d'una dimensió i el valor és una llista de valors
     *                         a filtrar. Si el mapa és null o buit, es generen només les condicions per entornAppId i rang
     *                         de dates.
     * @return Una cadena de text que representa la consulta SQL generada amb els filtres aplicats segons l'entornAppId,
     *         el rang de dates i les dimensions proporcionades.
     */
    String getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(Map<String, List<String>> dimensionsFiltre);

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
    String getSimpleQuery(Map<String, List<String>> dimensionsFiltre, String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio);

    String getGraficUnIndicadorQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, PeriodeUnitat tempsAgregacio);

    // SELECT
    //     t.dia || '/' || t.mes || + '/' || t.anualitat as agrupacio,
    //     JSON_VALUE(f.dimensions_json, '$."ORG"') AS descomposicio,
    //     SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$."NOT_ENV"'))) AS sum_fets_per_data
    // FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id
    // WHERE f.entorn_app_id = 1
    //   AND t.data BETWEEN TO_DATE('2025-04-30', 'YYYY-MM-DD') AND TO_DATE('2025-05-30', 'YYYY-MM-DD')
    //   AND JSON_VALUE(f.dimensions_json, '$."ENT"') = '1641'
    // GROUP BY t.anualitat, t.mes, t.dia, JSON_VALUE(f.dimensions_json, '$."ORG"')
    // ORDER BY agrupacio, descomposicio
    String getGraficUnIndicadorAmbDescomposicioAndAgrupacioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi, PeriodeUnitat tempsAgregacio);

    // -- Consulta SQL d'exemple per a widget grafic 1 indicador amb descomposició, agrupant per la descomposició
    // SELECT
    //    agrupacio,
    //    SUM(sum_fets_per_data) AS total_sum,
    //    AVG(sum_fets_per_data) AS average_result
    // FROM (
    //         -- Subconsulta per calcular les sumes per data
    //         SELECT
    //             JSON_VALUE(f.dimensions_json, '$."ORG"') AS agrupacio,
    //             SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$."NOT_ENV"'))) AS sum_fets_per_data
    //         FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id
    //         WHERE f.entorn_app_id = 1
    //           AND t.data BETWEEN TO_DATE('2025-04-30', 'YYYY-MM-DD') AND TO_DATE('2025-05-30', 'YYYY-MM-DD')
    //           AND JSON_VALUE(f.dimensions_json, '$."ENT"') = '1641'
    //         GROUP BY JSON_VALUE(f.dimensions_json, '$."ORG"')
    //     )
    // GROUP BY agrupacio;
    String getGraficUnIndicadorAmbDescomposicioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi);

    String getGraficVarisIndicadorsQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, PeriodeUnitat tempsAgregacio);

    String getTaulaQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, String dimensioAgrupacioCode);

}
