package es.caib.comanda.salut.persist.repository;

import es.caib.comanda.ms.persist.repository.BaseRepository;
import es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem;
import es.caib.comanda.salut.logic.intf.model.SalutInformeLatenciaItem;
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

	String SALUT_INFORME_ESTAT_ITEM_CLASS_NAME = "es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem";
	String SALUT_INFORME_LATENCIA_ITEM_CLASS_NAME = "es.caib.comanda.salut.logic.intf.model.SalutInformeLatenciaItem";

	@Query("FROM " +
			"    SalutEntity s1 " +
			"WHERE " +
			"    s1.data in (SELECT MAX(s2.data) from SalutEntity s2 where s1.entornAppId = s2.entornAppId AND s2.data < :data) " +
			"AND (:entornAppId IS NULL or s1.entornAppId = :entornAppId) " +
			"ORDER BY " +
			"    s1.entornAppId ASC")
	List<SalutEntity> informeSalutLast(
			@Param("entornAppId") Long entornAppId,
			@Param("data") LocalDateTime data);

	String INFORME_ESTAT_ANY = "SELECT " +
			"new " + SALUT_INFORME_ESTAT_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.year, 'YYYY')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.year " +
			"ORDER BY " +
			"    s.year ASC";
	String INFORME_ESTAT_MES = "SELECT " +
			"new " + SALUT_INFORME_ESTAT_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.yearMonth, 'YYYYMM')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonth " +
			"ORDER BY " +
			"    s.yearMonth ASC";
	String INFORME_ESTAT_DIA = "SELECT " +
			"new " + SALUT_INFORME_ESTAT_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.yearMonthDay, 'YYYYMMDD')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDay " +
			"ORDER BY " +
			"    s.yearMonthDay ASC";
	String INFORME_ESTAT_HORA = "SELECT " +
			"new " + SALUT_INFORME_ESTAT_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.yearMonthDayHour, 'YYYYMMDDHH24')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDayHour " +
			"ORDER BY " +
			"    s.yearMonthDayHour ASC";
	String INFORME_ESTAT_MINUT = "SELECT " +
			"new " + SALUT_INFORME_ESTAT_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.yearMonthDayHourMinute, 'YYYYMMDDHH24MI')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDayHourMinute " +
			"ORDER BY " +
			"    s.yearMonthDayHourMinute ASC";

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

	String INFORME_LATENCIA_ANY = "SELECT " +
			"new " + SALUT_INFORME_LATENCIA_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.year, 'YYYY')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.year " +
			"ORDER BY " +
			"    s.year ASC";
	String INFORME_LATENCIA_MES = "SELECT " +
			"new " + SALUT_INFORME_LATENCIA_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.yearMonth, 'YYYYMM')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonth " +
			"ORDER BY " +
			"    s.yearMonth ASC";
	String INFORME_LATENCIA_DIA = "SELECT " +
			"new " + SALUT_INFORME_LATENCIA_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.yearMonthDay, 'YYYYMMDD')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDay " +
			"ORDER BY " +
			"    s.yearMonthDay ASC";
	String INFORME_LATENCIA_HORA = "SELECT " +
			"new " + SALUT_INFORME_LATENCIA_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.yearMonthDayHour, 'YYYYMMDDHH24')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDayHour " +
			"ORDER BY " +
			"    s.yearMonthDayHour ASC";
	String INFORME_LATENCIA_MINUT = "SELECT " +
			"new " + SALUT_INFORME_LATENCIA_ITEM_CLASS_NAME + "(" +
			"    TO_DATE(s.yearMonthDayHourMinute, 'YYYYMMDDHH24MI')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.entornAppId = :entornAppId " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDayHourMinute " +
			"ORDER BY " +
			"    s.yearMonthDayHourMinute ASC";

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
