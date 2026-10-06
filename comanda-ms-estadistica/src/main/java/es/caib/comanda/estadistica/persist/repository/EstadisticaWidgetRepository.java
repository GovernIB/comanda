package es.caib.comanda.estadistica.persist.repository;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.ms.persist.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositori per a accedir i gestionar entitats de tipus `EstadisticaWidgetEntity` a la base de dades.
 *
 * Aquesta interfície extén de `BaseRepository` i proporciona funcionalitats bàsiques per a persistència i consulta de dades
 * relatives als widgets d'estadística en format taula. Les operacions inclouen la creació, lectura, actualització i eliminació
 * d'entitats `EstadisticaTaulaWidgetEntity`.
 *
 * Autor: Límit Tecnologies
 */
public interface EstadisticaWidgetRepository extends BaseRepository<EstadisticaWidgetEntity, Long> {
    EstadisticaWidgetEntity findByAppIdAndTitol(Long appId, String titol);

    List<EstadisticaWidgetEntity> findByAppId(Long appId);

    @Modifying
    @Query("DELETE FROM EstadisticaWidgetEntity w WHERE w.appId = :appId")
    void deleteByAppId(@Param("appId") Long appId);

    /**
     * Retorna els identificadors dels widgets que referencien algun element del catàleg (indicadors,
     * dimensions o valors de dimensió) d'un entorn d'aplicació.
     *
     * @param entornAppId identificador de l'entorn de l'aplicació
     */
    @Query("SELECT w.id FROM EstadisticaWidgetEntity w WHERE " +
            "w.id IN (SELECT it.widget.id FROM IndicadorTaulaEntity it WHERE it.indicador.entornAppId = :entornAppId) " +
            "OR w.id IN (SELECT wdv.id FROM EstadisticaWidgetEntity wdv JOIN wdv.dimensionsValor dv WHERE dv.dimensio.entornAppId = :entornAppId) " +
            "OR w.id IN (SELECT wt.id FROM EstadisticaTaulaWidgetEntity wt WHERE wt.dimensioAgrupacio.entornAppId = :entornAppId) " +
            "OR w.id IN (SELECT wg.id FROM EstadisticaGraficWidgetEntity wg WHERE wg.descomposicioDimensio.entornAppId = :entornAppId)")
    List<Long> findIdsByCatalegEntornAppId(@Param("entornAppId") Long entornAppId);

    // Nota de compatibilitat: Les següents consultes són natives (nativeQuery = true) per a
    // permetre operacions massives directes sobre taules d'unió (@JoinTable) i columnes de
    // subclasses (SINGLE_TABLE) sense carregar entitats a memòria. Tot i ser consultes natives,
    // segueixen estrictament la sintaxi ANSI SQL estàndard (UPDATE/DELETE amb subqueries IN),
    // de manera que són 100% compatibles i portables tant a PostgreSQL com a Oracle.

    /**
     * Desvincula l'indicador històric dels widgets associats a l'entorn indicat per mantenir
     * la integritat referencial (clau forana com_widget_indicador_fk).
     * Utilitza sintaxi ANSI SQL estàndard compatible tant amb PostgreSQL com amb Oracle.
     *
     * @param entornAppId identificador de l'entorn de l'aplicació
     */
    @Modifying
    @Query(value = "UPDATE " + BaseConfig.DB_PREFIX + "est_widget SET indicador_id = NULL WHERE indicador_id IN (SELECT id FROM " + BaseConfig.DB_PREFIX + "est_indicador WHERE entorn_app_id = :entornAppId)", nativeQuery = true)
    void clearIndicadorByEntornAppId(@Param("entornAppId") Long entornAppId);

    /**
     * Desvincula la dimensió d'agrupació dels widgets en format taula associats a l'entorn indicat.
     * Utilitza sintaxi ANSI SQL estàndard compatible tant amb PostgreSQL com amb Oracle.
     *
     * @param entornAppId identificador de l'entorn de l'aplicació
     */
    @Modifying
    @Query(value = "UPDATE " + BaseConfig.DB_PREFIX + "est_widget SET agrupament_dimensio_id = NULL WHERE agrupament_dimensio_id IN (SELECT id FROM " + BaseConfig.DB_PREFIX + "est_dimensio WHERE entorn_app_id = :entornAppId)", nativeQuery = true)
    void clearTaulaDimensioAgrupacioByEntornAppId(@Param("entornAppId") Long entornAppId);

    /**
     * Desvincula la dimensió de descomposició dels widgets gràfics associats a l'entorn indicat.
     * Utilitza sintaxi ANSI SQL estàndard compatible tant amb PostgreSQL com amb Oracle.
     *
     * @param entornAppId identificador de l'entorn de l'aplicació
     */
    @Modifying
    @Query(value = "UPDATE " + BaseConfig.DB_PREFIX + "est_widget SET descomposicio_dimensio_id = NULL WHERE descomposicio_dimensio_id IN (SELECT id FROM " + BaseConfig.DB_PREFIX + "est_dimensio WHERE entorn_app_id = :entornAppId)", nativeQuery = true)
    void clearGraficDescomposicioDimensioByEntornAppId(@Param("entornAppId") Long entornAppId);

    /**
     * Elimina les relacions de dimensions i valors dels widgets de l'aplicació a la taula d'unió (@JoinTable).
     * Utilitza sintaxi ANSI SQL estàndard compatible tant amb PostgreSQL com amb Oracle.
     *
     * @param appId identificador de l'aplicació
     */
    @Modifying
    @Query(value = "DELETE FROM " + BaseConfig.DB_PREFIX + "est_widget_dim_valor WHERE widget_id IN (SELECT id FROM " + BaseConfig.DB_PREFIX + "est_widget WHERE app_id = :appId)", nativeQuery = true)
    void deleteWidgetDimValorByAppId(@Param("appId") Long appId);

    /**
     * Elimina les relacions de dimensions i valors vinculades a l'entorn indicat a la taula d'unió (@JoinTable).
     * Utilitza sintaxi ANSI SQL estàndard compatible tant amb PostgreSQL com amb Oracle.
     *
     * @param entornAppId identificador de l'entorn de l'aplicació
     */
    @Modifying
    @Query(value = "DELETE FROM " + BaseConfig.DB_PREFIX + "est_widget_dim_valor WHERE dimensio_valor_id IN (SELECT v.id FROM " + BaseConfig.DB_PREFIX + "est_dimensio_valor v JOIN " + BaseConfig.DB_PREFIX + "est_dimensio d ON v.dimensio_id = d.id WHERE d.entorn_app_id = :entornAppId)", nativeQuery = true)
    void deleteWidgetDimValorByEntornAppId(@Param("entornAppId") Long entornAppId);

}
