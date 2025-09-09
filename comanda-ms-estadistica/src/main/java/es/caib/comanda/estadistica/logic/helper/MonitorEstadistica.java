package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Slf4j
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonitorEstadistica {

    private static final String ESTADISTICA_INFO_ACCIO = "Obtenir dades estadístiques";
    private static final String ESTADISTICA_DADES_ACCIO = "Obtenir informació de l'estructura de les estadístiques";
    private static final String ESTADISTICA_COMPACTAR_ACCIO = "Compactar dades estadístiques";
    private static final String ESTADISTICA_INFO_ERROR = "S'ha produït un error obtenint la informació de l'estructura de les estadístiques";
    private static final String ESTADISTICA_DADES_ERROR = "S'ha produït un error obtenint les dades estadístiques";
    private static final String ESTADISTICA_COMPACTAR_ERROR = "S'ha produït un error compactant les dades estadístiques";

    private EstadisticaClientHelper estadisticaClientHelper;

    private Long startInfoTime;
    private Long startDadesTime;
    private Long startCompactarTime;
    private Monitor monitorInfo;
    private Monitor monitorDades;
    private Monitor monitorCompactar;
    private boolean finishedInfoAction;
    private boolean finishedDadesAction;
    private boolean finishedCompactarAction;

    public MonitorEstadistica(
            Long entornAppId,
            String estadisticaInfoUrl,
            String estadisticaDadesUrl,
            EstadisticaClientHelper estadisticaClientHelper) {
        String usuariCodi = getAuthenticatedUserCode();
        this.monitorInfo = createMonitor(entornAppId, estadisticaInfoUrl, ESTADISTICA_INFO_ACCIO, usuariCodi);
        this.monitorDades = createMonitor(entornAppId, estadisticaDadesUrl, ESTADISTICA_DADES_ACCIO, usuariCodi);
        this.monitorCompactar = createMonitor(entornAppId, "--", ESTADISTICA_COMPACTAR_ACCIO, usuariCodi);
        this.estadisticaClientHelper = estadisticaClientHelper;
    }

    private Monitor createMonitor(Long entornAppId, String url, String operacio, String codiUsuari) {
        return Monitor.builder()
                .entornAppId(entornAppId)
                .modul(ModulEnum.ESTADISTICA)
                .tipus(AccioTipusEnum.SORTIDA)
                .url(url)
                .operacio(operacio)
                .codiUsuari(codiUsuari)
                .build();
    }


    public void startInfoAction() {
        monitorInfo.setData(LocalDateTime.now());
        this.startInfoTime = System.currentTimeMillis();
    }

    public void startDadesAction() {
        monitorDades.setData(LocalDateTime.now());
        this.startDadesTime = System.currentTimeMillis();
    }

    public void startCompactarAction() {
        monitorCompactar.setData(LocalDateTime.now());
        this.startCompactarTime = System.currentTimeMillis();
    }

    public void endInfoAction() {
        this.finishedInfoAction = true;
        finalitzarOK(monitorInfo, this.startInfoTime);
    }

    public void endInfoAction(Throwable t) {
        finalitzarError(monitorInfo, this.startInfoTime, ESTADISTICA_INFO_ERROR, t);
    }

    public void endDadesAction() {
        this.finishedDadesAction = true;
        finalitzarOK(monitorDades, this.startInfoTime);

    }

    public void endDadesAction(Throwable t) {
        finalitzarError(monitorDades, this.startDadesTime, ESTADISTICA_DADES_ERROR, t);
    }

    public void endCompactarAction() {
        this.finishedCompactarAction = true;
        finalitzarOK(monitorCompactar, this.startInfoTime);

    }

    public void endCompactarAction(Throwable t) {
        finalitzarError(monitorCompactar, this.startCompactarTime, ESTADISTICA_COMPACTAR_ERROR, t);
    }

    // Helpers privats per eliminar duplicació
    private void finalitzarOK(Monitor monitor, long startTime) {
        monitor.setEstat(EstatEnum.OK);
        monitor.setTempsResposta(System.currentTimeMillis() - startTime);
        estadisticaClientHelper.monitorCreate(monitor);
    }

    private void finalitzarError(Monitor monitor, long startTime, String errorDescripcio, Throwable t) {
        monitor.setEstat(EstatEnum.ERROR);
        monitor.setTempsResposta(System.currentTimeMillis() - startTime);
        monitor.setErrorDescripcio(errorDescripcio);
        monitor.setExcepcioMessage(ExceptionUtils.getMessage(t));
        monitor.setExcepcioStacktrace(ExceptionUtils.getStackTrace(t));
        estadisticaClientHelper.monitorCreate(monitor);
    }


    /**
     * Obté el codi de l'usuari autenticat o retorna "SCHEDULER" si no hi ha cap usuari autenticat o és anònim
     *
     * @return String amb el codi d'usuari o "SCHEDULER"
     */
    private String getAuthenticatedUserCode() {
        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return "SCHEDULER";
            }
            String userName = authentication.getName();
            return userName != null && !userName.isEmpty() ? userName : "SCHEDULER";
        } catch (Exception e) {
            return "SCHEDULER";
        }
    }

}