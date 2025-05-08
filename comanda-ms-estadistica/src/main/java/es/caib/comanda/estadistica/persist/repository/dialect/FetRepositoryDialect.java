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

    String getFindByEntornAppIdAndTempsDataAndDimensionQuery(Map<String, List<String>> dimensionsFiltre);

    String getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(Map<String, List<String>> dimensionsFiltre);

}