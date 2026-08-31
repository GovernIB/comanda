package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.consulta.SeguretatFiltreSql;
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
     * @param agregacio El tipus d'agregació a aplicar (COUNT, SUM, AVERAGE, FIRST_SEEN, LAST_SEEN, etc.).
     * @param unitatAgregacio La unitat de temps per a l'agregació interna (pot ser null per a agregacions globals).
     * @return Una cadena de text que representa la consulta SQL generada per obtenir el valor agregat.
     */
    String getSimpleQuery(Map<String, List<String>> dimensionsFiltre, String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio, SeguretatFiltreSql seguretat);
    //String getSimpleQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, SeguretatFiltreSql seguretat); TODO realment necessit el codi perque

    /**
     * Genera una consulta SQL per obtenir les dades d'un gràfic per a un únic indicador.
     * Aplica l'agregació temporal especificada i els filtres de dimensions proporcionats.
     *
     * @param dimensionsFiltre Un mapa que conté les dimensions i els seus respectius valors per aplicar els filtres corresponents.
     * @param indicadorAgregacio L'objecte {@link IndicadorAgregacio} que conté informació sobre l'indicador a processar (codi, tipus d'agregació, unitat).
     * @param tempsAgregacio La unitat de període utilitzada per agrupar les dades temporalment al gràfic (ex: DIA, MES, ANY).
     * @return La consulta SQL generada com a cadena de text, preparada per obtenir dades aplicant els filtres i agrupacions específiques.
     */
    String getGraficUnIndicadorQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, PeriodeUnitat tempsAgregacio, SeguretatFiltreSql seguretat);

    /**
     * Genera una consulta SQL per obtenir dades gràfiques d'un únic indicador amb descomposició per una dimensió específica,
     * mantenint l'agrupació temporal principal.
     * <p>
     * Exemple de resultat esperat: Agrupat per temps (ex: mes) i descomposat per dimensió (ex: organització),
     * mostrant l'evolució temporal de cada valor de la dimensió (ideal per a gràfics de línies o barres apilades).
     *
     * @param dimensionsFiltre Representació de les dimensions de filtratge amb les seves respectives llistes de valors.
     * @param indicadorAgregacio Indicador d'agregació que conté el codi de l'indicador a consultar i el seu tipus d'agregació.
     * @param dimensioDescomposicioCodi Codi de la dimensió utilitzada per fer la descomposició en el resultat de la consulta.
     * @param tempsAgregacio Unitat de temps que defineix com s'agreguen els períodes (diari, mensual, anual, etc.) en la consulta.
     * @return Cadena de text que conté la consulta SQL generada.
     */
    String getGraficUnIndicadorAmbDescomposicioAndAgrupacioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi, PeriodeUnitat tempsAgregacio, SeguretatFiltreSql seguretat);

    /**
     * Genera una consulta SQL per obtenir dades d'un gràfic d'un únic indicador amb descomposició per una dimensió determinada,
     * sense agrupació temporal addicional (només agrupat per la dimensió de descomposició).
     * <p>
     * Exemple de resultat esperat: Un gràfic de pastís o barres que mostra el total acumulat del període desglossat
     * per la dimensió seleccionada, sense eix temporal.
     *
     * @param dimensionsFiltre Mapa amb les dimensions i els seus valors a filtrar en la consulta.
     * @param indicadorAgregacio Objecte que conté la informació de l'indicador agregat, inclòs el seu codi identificador.
     * @param dimensioDescomposicioCodi Codi de la dimensió sobre la qual s'aplicarà la descomposició.
     * @return Consulta SQL generada com a cadena de text per obtenir dades amb descomposició per l'indicador especificat.
     */
    String getGraficUnIndicadorAmbDescomposicioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi, SeguretatFiltreSql seguretat);

    /**
     * Genera la consulta SQL per obtenir dades d'un gràfic amb múltiples indicadors agregats segons un període temporal i filtrat per dimensions.
     * <p>
     * Aquest mètode és capaç de gestionar indicadors amb diferents unitats d'agregació temporal dins de la mateixa consulta,
     * utilitzant {@code UNION ALL} internament quan és necessari per garantir la coherència dels càlculs (ex: barrejar sumes diàries i mitjanes mensuals).
     *
     * @param dimensionsFiltre Mapa que conté les dimensions i els seus valors per aplicar com a criteris de filtre a la consulta.
     * @param indicadorsAgregacio Llista d'indicadors amb informació sobre l'agregació i la unitat temporal associada a cadascun.
     * @param tempsAgregacio Unitat temporal per a l'agregació principal de les dades al gràfic.
     * @return Consulta SQL com a cadena de text per obtenir les dades del gràfic.
     */
    String getGraficVarisIndicadorsQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, PeriodeUnitat tempsAgregacio, SeguretatFiltreSql seguretat);

    /**
     * Genera la consulta SQL per obtenir dades d'una taula amb múltiples indicadors agregats, agrupats per una dimensió específica.
     * <p>
     * Similar al mètode de gràfics múltiples, aquest mètode gestiona automàticament les diferències en les unitats d'agregació
     * dels indicadors (ex: sumar tot el període vs. fer una mitjana mensual) mitjançant subconsultes i {@code UNION ALL}
     * per assegurar que cada indicador es calculi amb la granularitat correcta abans de presentar-se a la taula.
     *
     * @param dimensionsFiltre Mapa que conté les dimensions i els seus valors per aplicar com a criteris de filtre a la consulta.
     * @param indicadorsAgregacio Llista d'indicadors amb informació sobre l'agregació i la unitat temporal associada a cadascun.
     * @param dimensioAgrupacioCodi Codi de la dimensió que s'utilitzarà com a eix principal d'agrupació de la taula (ex: codi d'entitat, departament).
     * @return Consulta SQL com a cadena de text per obtenir les files i columnes de la taula agregada.
     */
    String getTaulaQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, String dimensioAgrupacioCodi, SeguretatFiltreSql seguretat);

}
