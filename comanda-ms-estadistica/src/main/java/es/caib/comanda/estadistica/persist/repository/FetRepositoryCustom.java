package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;

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

    /**
     * Obté un valor agregat d'un indicador específic basat en l'entornAppId, un rang de dates específic, 
     * valors dimensionals i un tipus d'agregació.
     * Aquesta consulta aplica l'agregació directament a la base de dades, optimitzant el rendiment.
     *
     * @param entornAppId Identificador de l'entorn de l'aplicació pel qual es vol fer la cerca.
     * @param dataInici Data inicial del rang de dates per a la cerca.
     * @param dataFi Data final del rang de dates per a la cerca.
     * @param dimensionsFiltre Map que conté com a clau el codi de cada dimensió i com a valor la llista de valors associats
     *                         a aquesta dimensió per aplicar com a criteri de filtre.
     * @param indicadorAgregacio Informació de l'indicador sobre el qual s'aplicarà l'agregació.
     * @return El valor agregat calculat directament a la base de dades.
     */
    String getValorSimpleAgregat(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            IndicadorAgregacio indicadorAgregacio);

    List<Map<String, String>> getValorsGraficUnIndicador(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            IndicadorAgregacio indicadorAgregacio,
            PeriodeUnitat tempsAgregacio);

    List<Map<String, String>> getValorsGraficUnIndicadorAmdDescomposicio(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            IndicadorAgregacio indicadorAgregacio,
            String dimensioDescomposicioCodi,
            PeriodeUnitat tempsAgregacio);

    List<Map<String, String>> getValorsGraficUnIndicadorAmdDescomposicio(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            IndicadorAgregacio indicadorAgregacio,
            String dimensioDescomposicioCodi);

    List<Map<String, String>> getValorsGraficVarisIndicadors(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            List<IndicadorAgregacio> indicadorsAgregacio,
            PeriodeUnitat tempsAgregacio);

    List<Map<String, String>> getValorsTaulaAgregat(
            Long entornAppId,
            LocalDate dataInici,
            LocalDate dataFi,
            Map<String, List<String>> dimensionsFiltre,
            List<IndicadorAgregacio> indicadorsAgregacio,
            String dimensioAgrupacioCodi);

}
