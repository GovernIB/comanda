package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;

@Getter
public class MonitorAlarmes {

    public static final String MAIL_URL = "MAIL";
    public static final String ENVIAMENT_CORREU_USUARI = "Enviar correu d'alarma a usuari";
    public static final String ENVIAMENT_CORREU_GENERIC = "Enviar correu generic d'alarma";
    public static final String ENVIAMENT_CORREU_AGRUPAT = "Enviar resum d'alarmes";
    private static final String ERROR_PER_DEFECTE = "S'ha produït un error enviant el correu d'alarma";

    private final AlarmaClientHelper alarmaClientHelper;
    private final Monitor monitor;
    private Long startTime;

    public MonitorAlarmes(
            Long entornAppId,
            String operacio,
            String url,
            String codiUsuari,
            AlarmaClientHelper alarmaClientHelper) {
        this.monitor = Monitor.builder()
                .entornAppId(entornAppId)
                .modul(ModulEnum.ALARMES)
                .tipus(AccioTipusEnum.SORTIDA)
                .url(url)
                .operacio(operacio)
                .codiUsuari(codiUsuari)
                .build();
        this.alarmaClientHelper = alarmaClientHelper;
    }

    public void startAction() {
        monitor.setData(LocalDateTime.now());
        this.startTime = System.currentTimeMillis();
    }

    public void endAction() {
        monitor.setEstat(EstatEnum.OK);
        monitor.setTempsResposta(getElapsedTime());
        alarmaClientHelper.monitorCreate(monitor);
    }

    public void endAction(Throwable t, String errorDescripcio) {
        monitor.setEstat(EstatEnum.ERROR);
        monitor.setTempsResposta(getElapsedTime());
        monitor.setErrorDescripcio(Strings.isBlank(errorDescripcio) ? ERROR_PER_DEFECTE : errorDescripcio);
        monitor.setExcepcioMessage(ExceptionUtils.getMessage(t));
        monitor.setExcepcioStacktrace(ExceptionUtils.getStackTrace(t));
        alarmaClientHelper.monitorCreate(monitor);
    }

    private long getElapsedTime() {
        if (monitor.getData() == null) {
            monitor.setData(LocalDateTime.now());
        }
        long effectiveStartTime = this.startTime != null ? this.startTime : System.currentTimeMillis();
        return System.currentTimeMillis() - effectiveStartTime;
    }
}
