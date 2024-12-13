package es.caib.comanda.configuracio.persist.repository;

import es.caib.comanda.configuracio.logic.intf.model.SalutInformeEstatItem;
import es.caib.comanda.configuracio.logic.intf.model.SalutInformeLatenciaItem;
import es.caib.comanda.configuracio.persist.entity.SalutEntity;
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

	@Query("FROM " +
			"    SalutEntity s1 " +
			"WHERE " +
			"    s1.data in (SELECT MAX(data) from SalutEntity s2 where s1.codi = s2.codi AND s2.data < :data) " +
			"AND (:codi IS NULL or s1.codi = :codi) " +
			"ORDER BY " +
			"    s1.codi ASC")
	List<SalutEntity> informeSalutLast(
			@Param("codi") String codi,
			@Param("data") LocalDateTime data);

	String INFORME_ESTAT_ANY = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeEstatItem(" +
			"    TO_DATE(s.year, 'YYYY')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.year " +
			"ORDER BY " +
			"    s.year ASC";
	String INFORME_ESTAT_MES = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeEstatItem(" +
			"    TO_DATE(s.yearMonth, 'YYYYMM')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonth " +
			"ORDER BY " +
			"    s.yearMonth ASC";
	String INFORME_ESTAT_DIA = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeEstatItem(" +
			"    TO_DATE(s.yearMonthDay, 'YYYYMMDD')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDay " +
			"ORDER BY " +
			"    s.yearMonthDay ASC";
	String INFORME_ESTAT_HORA = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeEstatItem(" +
			"    TO_DATE(s.yearMonthDayHour, 'YYYYMMDDHH24')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDayHour " +
			"ORDER BY " +
			"    s.yearMonthDayHour ASC";
	String INFORME_ESTAT_MINUT = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeEstatItem(" +
			"    TO_DATE(s.yearMonthDayHourMinute, 'YYYYMMDDHH24MI')," +
			"    COUNT((CASE WHEN (s.appEstat = 'UP') THEN 'UP' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'DOWN') THEN 'DOWN' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'OUT_OF_SERVICE') THEN 'OUT_OF_SERVICE' ELSE NULL END)), " +
			"    COUNT((CASE WHEN (s.appEstat = 'UNKNOWN') THEN 'UNKNOWN' ELSE NULL END))) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDayHourMinute " +
			"ORDER BY " +
			"    s.yearMonthDayHourMinute ASC";

	@Query(INFORME_ESTAT_ANY)
	List<SalutInformeEstatItem> informeEstatAny(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_ESTAT_MES)
	List<SalutInformeEstatItem> informeEstatMes(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_ESTAT_DIA)
	List<SalutInformeEstatItem> informeEstatDia(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_ESTAT_HORA)
	List<SalutInformeEstatItem> informeEstatHora(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_ESTAT_MINUT)
	List<SalutInformeEstatItem> informeEstatMinut(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);

	String INFORME_LATENCIA_ANY = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeLatenciaItem(" +
			"    TO_DATE(s.year, 'YYYY')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.year " +
			"ORDER BY " +
			"    s.year ASC";
	String INFORME_LATENCIA_MES = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeLatenciaItem(" +
			"    TO_DATE(s.yearMonth, 'YYYYMM')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonth " +
			"ORDER BY " +
			"    s.yearMonth ASC";
	String INFORME_LATENCIA_DIA = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeLatenciaItem(" +
			"    TO_DATE(s.yearMonthDay, 'YYYYMMDD')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDay " +
			"ORDER BY " +
			"    s.yearMonthDay ASC";
	String INFORME_LATENCIA_HORA = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeLatenciaItem(" +
			"    TO_DATE(s.yearMonthDayHour, 'YYYYMMDDHH24')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDayHour " +
			"ORDER BY " +
			"    s.yearMonthDayHour ASC";
	String INFORME_LATENCIA_MINUT = "SELECT " +
			"new es.caib.comanda.configuracio.logic.intf.model.SalutInformeLatenciaItem(" +
			"    TO_DATE(s.yearMonthDayHourMinute, 'YYYYMMDDHH24MI')," +
			"    AVG(s.appLatencia)) " +
			"FROM " +
			"    SalutEntity AS s " +
			"WHERE " +
			"    s.codi = :codi " +
			"AND s.data >= :dataInici " +
			"AND s.data <= :dataFi " +
			"GROUP BY " +
			"    s.yearMonthDayHourMinute " +
			"ORDER BY " +
			"    s.yearMonthDayHourMinute ASC";

	@Query(INFORME_LATENCIA_ANY)
	List<SalutInformeLatenciaItem> informeLatenciaAny(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_LATENCIA_MES)
	List<SalutInformeLatenciaItem> informeLatenciaMes(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_LATENCIA_DIA)
	List<SalutInformeLatenciaItem> informeLatenciaDia(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_LATENCIA_HORA)
	List<SalutInformeLatenciaItem> informeLatenciaHora(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);
	@Query(INFORME_LATENCIA_MINUT)
	List<SalutInformeLatenciaItem> informeLatenciaMinut(
			@Param("codi") String codi,
			@Param("dataInici") LocalDateTime dataInici,
			@Param("dataFi") LocalDateTime dataFi);

}
