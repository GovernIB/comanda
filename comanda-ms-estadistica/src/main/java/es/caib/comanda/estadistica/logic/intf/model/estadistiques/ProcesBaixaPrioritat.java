package es.caib.comanda.estadistica.logic.intf.model.estadistiques;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un procés de recuperació de dades estadístiques en segon pla de baixa prioritat.
 *
 * @author Límit Tecnologies
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcesBaixaPrioritat implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ESTAT_PENDENT = "PENDENT";
    public static final String ESTAT_EN_EXECUCIO = "EN_EXECUCIO";
    public static final String ESTAT_FINALITZAT = "FINALITZAT";
    public static final String ESTAT_ERROR = "ERROR";
    public static final String ESTAT_CANCELAT = "CANCELAT";

    private String id;
    private Long entornAppId;
    private String entornAppNom;
    private LocalDate dataInici;
    private LocalDate dataFi;
    private Long pausaMs;

    private volatile String estat;
    private LocalDateTime dataIniciExecucio;
    private volatile LocalDateTime dataFiExecucio;

    private int totalDies;
    private volatile int diesProcessats;
    private volatile int diesAmbDades;
    private volatile int diesAmbErrors;
    private volatile int percentatge;
    private volatile LocalDate diaActual;
    private volatile String missatge;

    @JsonIgnore
    private transient volatile boolean cancelled;

    @JsonIgnore
    private transient volatile Thread runningThread;
}
