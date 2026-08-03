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
public class MonitorDir3 {

    private static final String DIR3_INFO_ACCIO = "Obtenir unitats organitzatives";
    private static final String DIR3_INFO_ERROR = "S'ha produït un error obtenint les unitats organitzatives";
    private static final String DIR3_ORGANIGRAMA_ACCIO = "Obtenir organigrama d'unitats organitzatives";
    private static final String DIR3_ORGANIGRAMA_ERROR = "S'ha produït un error obtenint l'organigrama de les unitats organitzatives";

    private EstadisticaClientHelper estadisticaClientHelper;

    private Long startInfoTime;
    private Long startOrganigramaTime;
    private Monitor monitorInfo;
    private Monitor monitorOrganigrama;
    private boolean finishedInfoAction;
    private boolean finishedOrganigramaAction;

    public MonitorDir3(
            String url,
            EstadisticaClientHelper estadisticaClientHelper) {
        String usuariCodi = getAuthenticatedUserCode();
        this.monitorInfo = createMonitor(url, DIR3_INFO_ACCIO, usuariCodi);
        this.monitorOrganigrama = createMonitor(url, DIR3_ORGANIGRAMA_ACCIO, usuariCodi);
        this.estadisticaClientHelper = estadisticaClientHelper;
    }

    private Monitor createMonitor(String url, String operacio, String codiUsuari) {
        return Monitor.builder()
                .entornAppId(null)
                .modul(ModulEnum.DIR3)
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

    public void endInfoAction() {
        this.finishedInfoAction = true;
        finalitzarOK(monitorInfo, this.startInfoTime);
    }

    public void endInfoAction(Throwable t) {
        finalitzarError(monitorInfo, this.startInfoTime, DIR3_INFO_ERROR, t);
    }

    public void startOrganigramaAction() {
        monitorOrganigrama.setData(LocalDateTime.now());
        this.startOrganigramaTime = System.currentTimeMillis();
    }

    public void endOrganigramaAction() {
        this.finishedOrganigramaAction = true;
        finalitzarOK(monitorOrganigrama, this.startOrganigramaTime);

    }

    public void endOrganigramaAction(Throwable t) {
        finalitzarError(monitorOrganigrama, this.startOrganigramaTime, DIR3_ORGANIGRAMA_ERROR, t);
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
