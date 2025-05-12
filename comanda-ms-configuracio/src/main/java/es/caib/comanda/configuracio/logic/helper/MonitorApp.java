package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.client.MonitorServiceClient;
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
public class MonitorApp {

    private static final String APP_INFO_ACCIO = "Obtenir dades de l'aplicació";
    private static final String APP_INFO_ERROR = "S'ha produït un error obtenint la informació de l'aplicació";

    private MonitorServiceClient monitorServiceClient;

    private Long startTime;
    private Monitor monitor;
    private boolean finishedAction;
    private String authorizationHeader;

    public MonitorApp(
            Long entornAppId,
            String appInfoUrl,
            MonitorServiceClient monitorServiceClient,
            String authorizationHeader) {
        String usuariCodi = getAuthenticatedUserCode();
        this.monitor = createMonitor(entornAppId, appInfoUrl, APP_INFO_ACCIO, usuariCodi);
        this.monitorServiceClient = monitorServiceClient;
        this.authorizationHeader = authorizationHeader;
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


    public void startAction() {
        monitor.setData(LocalDateTime.now());
        this.startTime = System.currentTimeMillis();
    }

    public void endAction() {
        monitor.setEstat(EstatEnum.OK);
        monitor.setTempsResposta(System.currentTimeMillis() - this.startTime);
        this.finishedAction = true;
        saveMonitor(monitor);
    }

    public void endAction(Throwable t) {
        monitor.setEstat(EstatEnum.ERROR);
        monitor.setTempsResposta(System.currentTimeMillis() - this.startTime);
        monitor.setErrorDescripcio(APP_INFO_ERROR);
        monitor.setExcepcioMessage(ExceptionUtils.getMessage(t));
        monitor.setExcepcioStacktrace(ExceptionUtils.getStackTrace(t));
        saveMonitor(monitor);
    }

    private void saveMonitor(Monitor monitor) {
        try {
            monitorServiceClient.create(monitor, authorizationHeader);
        } catch (Exception e) {
            log.error("Error al guardar el monitor: " + monitor, e);
        }
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