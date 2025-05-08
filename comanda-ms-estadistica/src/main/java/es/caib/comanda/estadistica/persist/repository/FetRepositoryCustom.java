package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.persist.entity.FetEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Interfície de repositori personalitzada per la implementació de FetEntity. Proporciona mètodes de consulta amb SQL
 * específic del dialecte.
 *
 * @author Limit Tecnologies
 */
public interface FetRepositoryCustom {

    /**
     * Busca entitats de tipus FetEntity filtrant per l'identificador de l'entorn de l'aplicació, un rang de dates,
     * i un valor específic d'una dimensió determinada. Utilitza SQL específic segons el dialecte per accedir al valor
     * de la dimensió des del camp JSON corresponent.
     *
     * @param entornAppId Identificador de l'entorn de l'aplicació pel qual es vol fer la cerca.
     * @param dataInici Data inicial del rang de dates per a la cerca.
     * @param dataFi Data final del rang de dates per a la cerca.
     * @param dimensioCodi Codi de la dimensió a ser utilitzada com a criteri de filtre.
     * @param dimensioValor Valor específic de la dimensió a ser utilitzat com a criteri de filtre.
     * @return Llista d'entitats FetEntity
     */
    List<FetEntity> findByEntornAppIdAndTempsDataBetweenAndDimensionValue(
            Long entornAppId, 
            LocalDate dataInici, 
            LocalDate dataFi,
            String dimensioCodi,
            String dimensioValor);

    /**
     * Busca entitats de tipus FetEntity filtrant per l'identificador de l'entorn de l'aplicació, un rang de dates específic,
     * un codi de dimensió i una llista de valors associats a aquesta dimensió.
     *
     * @param entornAppId Identificador de l'entorn de l'aplicació pel qual es vol fer la cerca.
     * @param dataInici Data inicial del rang de dates per a la cerca.
     * @param dataFi Data final del rang de dates per a la cerca.
     * @param dimensioCodi Codi de la dimensió que s'utilitza com a criteri de filtre.
     * @param valors Llista de valors associats a la dimensió utilitzada com a criteri de filtre.
     * @return Llista d'entitats FetEntity que compleixen els criteris especificats.
     */
    List<FetEntity> findByEntornAppIdAndTempsDataBetweenAndDimensionValues(
            Long entornAppId, 
            LocalDate dataInici, 
            LocalDate dataFi,
            String dimensioCodi,
            List<String> valors);

    /**
     * Busca entitats de tipus FetEntity filtrant per l'identificador de l'entorn de l'aplicació, una data específica i un conjunt de dimensions amb els
     * seus valors associats.
     *
     * @param entornAppId Identificador de l'entorn de l'aplicació pel qual es vol fer la cerca.
     * @param data Data específica per a la cerca.
     * @param dimensionsFiltre Map que conté com a clau el codi de cada dimensió i com a valor la llista de valors associats a aquesta dimensió per
     *                         aplicar com a criteri de filtre.
     * @return Llista d'entitats FetEntity que compleixen els criteris de cerca especificats.
     */
    List<FetEntity> findByEntornAppIdAndTempsDataAndDimensions(
            Long entornAppId,
            LocalDate data,
            Map<String, List<String>> dimensionsFiltre);

    /**
     * Busca entitats de tipus FetEntity filtrant per l'identificador de l'entorn de l'aplicació, un rang de dates determinat,
     * i un conjunt de dimensions amb els seus valors associats.
     *
     * @param entornAppId Identificador de l'entorn de l'aplicació pel qual es vol fer la cerca.
     * @param dataInici Data inicial del rang de dates per a la cerca.
     * @param dataFi Data final del rang de dates per a la cerca.
     * @param dimensionsFiltre Map que conté com a clau el codi de cada dimensió i com a valor la llista de valors associats
     *                         a aquesta dimensió per aplicar com a criteri de filtre.
     * @return Llista d'entitats FetEntity que compleixen els criteris de cerca especificats.
     */
    List<FetEntity> findByEntornAppIdAndTempsDataBetweenAndDimensions(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre);

//    /**
//     * Find annual statistics by entornAppId
//     * Uses dialect-specific SQL to access values from dimensions_json and indicadors_json
//     */
//    List<Object[]> findAnnualStatsByEntornAppId(Long entornAppId);
//
//    /**
//     * Find statistics by entornAppId and period
//     * Uses dialect-specific SQL to access values from dimensions_json and indicadors_json
//     */
//    List<Object[]> findStatsByEntornAppIdAndPeriod(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            String nivellAgrupacio);
//
//    /**
//     * Find statistics by entornAppId, period, and dimensions
//     * Uses dialect-specific SQL to access values from dimensions_json and indicadors_json
//     * and to filter by dimensions
//     */
//    List<Object[]> findStatsByEntornAppIdAndPeriodAndDimensions(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            String nivellAgrupacio,
//            String dimensioNom,
//            List<String> dimensioValors);
//
//    /**
//     * Find statistics by entornAppId, period, and multiple dimensions
//     * Uses dialect-specific SQL to access values from dimensions_json and indicadors_json
//     * and to filter by multiple dimensions with multiple values
//     */
//    List<Object[]> findStatsByEntornAppIdAndPeriodAndMultipleDimensions(
//            Long entornAppId,
//            LocalDate dataInici,
//            LocalDate dataFi,
//            String nivellAgrupacio,
//            String dimensionsJson);
}