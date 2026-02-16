package es.caib.comanda.client.model.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Monitor {

    private static int ERROR_DESC_MAX_LENGTH = 900;
    private static int ERROR_STACK_MAX_LENGTH = 3800;

    @Setter
    private Long entornAppId;
    private ModulEnum modul;
    private AccioTipusEnum tipus;
    @Setter
    private LocalDateTime data;
    private String url;
    @Setter
    private String operacio;
    @Setter
    private Long tempsResposta;
    @Setter
    private EstatEnum estat;
    private String codiUsuari;
    private String errorDescripcio;
    private String excepcioMessage;
    private String excepcioStacktrace;

    public void setErrorDescripcio(String errorDescripcio) {
        this.errorDescripcio = StringUtils.abbreviate(errorDescripcio, ERROR_DESC_MAX_LENGTH);
    }
    public void setExcepcioMessage(String excepcioMessage) {
        this.excepcioMessage = StringUtils.abbreviate(excepcioMessage, ERROR_DESC_MAX_LENGTH);
    }
    public void setExcepcioStacktrace(String excepcioStacktrace) {
        this.excepcioStacktrace = StringUtils.abbreviate(excepcioStacktrace, ERROR_STACK_MAX_LENGTH);
    }

    // Custom builder setters
    public static class MonitorBuilder {
        public Monitor.MonitorBuilder errorDescripcio(String errorDescripcio){
            this.errorDescripcio = StringUtils.abbreviate(errorDescripcio, ERROR_DESC_MAX_LENGTH);
            return this;
        }

        public Monitor.MonitorBuilder excepcioMessage(String excepcioMessage){
            this.excepcioMessage = StringUtils.abbreviate(excepcioMessage, ERROR_DESC_MAX_LENGTH);
            return this;
        }

        public Monitor.MonitorBuilder excepcioStacktrace(String excepcioStacktrace){
            this.excepcioStacktrace = StringUtils.abbreviate(excepcioStacktrace, ERROR_STACK_MAX_LENGTH);
            return this;
        }
    }
}
