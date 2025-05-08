package es.caib.comanda.estadistica.persist.repository.dialect;

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

}