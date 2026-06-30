package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;

public class MonitorUserInformation {

    public static final String FIND_BY_ROLE = "Cercar usuaris per rol";
    public static final String FIND_BY_USERNAME = "Cercar usuari per nom d'usuari";
    private static final String ERROR_PER_DEFECTE = "S'ha produït un error consultant la informació d'usuari";

    private final AlarmaClientHelper alarmaClientHelper;
    private final Monitor monitor;
    private Long startTime;

    public MonitorUserInformation(
            String operacio,
            String url,
            AlarmaClientHelper alarmaClientHelper) {
        this.monitor = Monitor.builder()
                .modul(ModulEnum.USUARIS)
                .tipus(AccioTipusEnum.SORTIDA)
                .url(url)
                .operacio(operacio)
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
