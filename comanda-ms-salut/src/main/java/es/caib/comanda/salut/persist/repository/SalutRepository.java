package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem;
import es.caib.comanda.salut.logic.intf.model.SalutInformeLatenciaItem;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositori per a la gestió d'informacions de salut.
 * 
 * @author Límit Tecnologies
 */
public interface SalutRepository extends BaseRepository<SalutEntity, Long> {

    SalutEntity findTopByEntornAppIdAndTipusRegistreOrderByIdDesc(Long entornAppId, TipusRegistreSalut tipusRegistre);
	List<SalutEntity> findByEntornAppIdAndTipusRegistreAndDataBefore(Long entornAppId, TipusRegistreSalut tipusRegistre, LocalDateTime data);
    @Query("SELECT s.id FROM SalutEntity s WHERE s.entornAppId = :entornAppId AND s.tipusRegistre = :tipusRegistre AND s.data < :data")
    List<Long> findIdsByEntornAppIdAndTipusRegistreAndDataBefore(
            @Param("entornAppId") Long entornAppId,
            @Param("tipusRegistre") TipusRegistreSalut tipusRegistre,
            @Param("data") LocalDateTime data
    );

	void deleteByDataBefore(LocalDateTime data);


	String SALUT_ESTAT_ITEM_CLASS = "SELECT new es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem( ";
	String SALUT_LATENCIA_ITEM_CLASS = "SELECT new es.caib.comanda.salut.logic.intf.model.SalutInformeLatenciaItem( ";

	String SALUT_ESTAT_ITEM_SELECT =
			"COUNT((CASE WHEN (s.appEstat = 'UP') THEN 1 ELSE NULL END)), " +
			"COUNT((CASE WHEN (s.appEstat = 'WARN') THEN 1 ELSE NULL END)), " +
			"COUNT((CASE WHEN (s.appEstat = 'DEGRADED') THEN 1 ELSE NULL END)), " +
			"COUNT((CASE WHEN (s.appEstat = 'DOWN') OR (s.appEstat = 'ERROR') THEN 1 ELSE NULL END)), " +
			"COUNT((CASE WHEN (s.appEstat = 'MAINTENANCE') THEN 1 ELSE NULL END)), " +
			"COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 1 ELSE NULL END))) ";
	String SALUT_LATENCIA_ITEM_SELECT = "AVG(s.appLatencia)) ";
	String COMMON_WHERE_CLAUSE =
			"FROM SalutEntity AS s " +
			"WHERE (:entornAppId is null) or (:entornAppId is not null and s.entornAppId = :entornAppId) " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi ";

	String SALUT_ESTAT_SELECT_FROM = SALUT_ESTAT_ITEM_SELECT + COMMON_WHERE_CLAUSE;
	String SALUT_LATENCIA_SELECT_FROM = SALUT_LATENCIA_ITEM_SELECT + COMMON_WHERE_CLAUSE;

	@Query( " FROM SalutEntity s1 " +
			"WHERE s1.data in (SELECT MAX(s2.data) from SalutEntity s2 where s1.entornAppId = s2.entornAppId AND s2.data < :data) " +
			"  AND s1.entornAppId in (:entornAppIds) " +
			"ORDER BY s1.entornAppId ASC")
	List<SalutEntity> informeSalutLast(
			@Param("entornAppIds") List<Long> entornAppIds,
			@Param("data") LocalDateTime data);

	// Consultes per a informes d'estat (agrupat per períodes)
	String INFORME_ESTAT_ANY = SALUT_ESTAT_ITEM_CLASS + "TO_DATE(s.year, 'YYYY'), " + SALUT_ESTAT_SELECT_FROM + "GROUP BY s.year ORDER BY s.year ASC";
	String INFORME_ESTAT_MES = SALUT_ESTAT_ITEM_CLASS + "TO_DATE(s.yearMonth, 'YYYYMM'), " + SALUT_ESTAT_SELECT_FROM + "GROUP BY s.yearMonth ORDER BY s.yearMonth ASC";
	String INFORME_ESTAT_DIA = SALUT_ESTAT_ITEM_CLASS + "TO_DATE(s.yearMonthDay, 'YYYYMMDD'), " + SALUT_ESTAT_SELECT_FROM + "GROUP BY s.yearMonthDay ORDER BY s.yearMonthDay ASC";
	String INFORME_ESTAT_HORA = SALUT_ESTAT_ITEM_CLASS + "TO_DATE(s.yearMonthDayHour, 'YYYYMMDDHH24'), " + SALUT_ESTAT_SELECT_FROM + "GROUP BY s.yearMonthDayHour ORDER BY s.yearMonthDayHour ASC";
	String INFORME_ESTAT_MINUT = SALUT_ESTAT_ITEM_CLASS + "TO_DATE(s.yearMonthDayHourMinute, 'YYYYMMDDHH24MI'), " + SALUT_ESTAT_SELECT_FROM + "GROUP BY s.yearMonthDayHourMinute ORDER BY s.yearMonthDayHourMinute ASC";

	@Query(INFORME_ESTAT_ANY)
	List<SalutInformeEstatItem> informeEstatAny(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_ESTAT_MES)
	List<SalutInformeEstatItem> informeEstatMes(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_ESTAT_DIA)
	List<SalutInformeEstatItem> informeEstatDia(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_ESTAT_HORA)
	List<SalutInformeEstatItem> informeEstatHora(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_ESTAT_MINUT)
	List<SalutInformeEstatItem> informeEstatMinut(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);

	String INFORME_LATENCIA_ANY = SALUT_LATENCIA_ITEM_CLASS + "TO_DATE(s.year, 'YYYY'), " + SALUT_LATENCIA_SELECT_FROM + "GROUP BY s.year ORDER BY s.year ASC";
	String INFORME_LATENCIA_MES = SALUT_LATENCIA_ITEM_CLASS + "TO_DATE(s.yearMonth, 'YYYYMM'), " + SALUT_LATENCIA_SELECT_FROM + "GROUP BY s.yearMonth ORDER BY s.yearMonth ASC";
	String INFORME_LATENCIA_DIA = SALUT_LATENCIA_ITEM_CLASS + "TO_DATE(s.yearMonthDay, 'YYYYMMDD'), " + SALUT_LATENCIA_SELECT_FROM + "GROUP BY s.yearMonthDay ORDER BY s.yearMonthDay ASC";
	String INFORME_LATENCIA_HORA = SALUT_LATENCIA_ITEM_CLASS + "TO_DATE(s.yearMonthDayHour, 'YYYYMMDDHH24'), " + SALUT_LATENCIA_SELECT_FROM + "GROUP BY s.yearMonthDayHour ORDER BY s.yearMonthDayHour ASC";
	String INFORME_LATENCIA_MINUT = SALUT_LATENCIA_ITEM_CLASS + "TO_DATE(s.yearMonthDayHourMinute, 'YYYYMMDDHH24MI'), " + SALUT_LATENCIA_SELECT_FROM + "GROUP BY s.yearMonthDayHourMinute ORDER BY s.yearMonthDayHourMinute ASC";

	@Query(INFORME_LATENCIA_ANY)
	List<SalutInformeLatenciaItem> informeLatenciaAny(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_LATENCIA_MES)
	List<SalutInformeLatenciaItem> informeLatenciaMes(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_LATENCIA_DIA)
	List<SalutInformeLatenciaItem> informeLatenciaDia(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_LATENCIA_HORA)
	List<SalutInformeLatenciaItem> informeLatenciaHora(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_LATENCIA_MINUT)
	List<SalutInformeLatenciaItem> informeLatenciaMinut(
			@Param("entornAppId") Long entornAppId,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);

}
