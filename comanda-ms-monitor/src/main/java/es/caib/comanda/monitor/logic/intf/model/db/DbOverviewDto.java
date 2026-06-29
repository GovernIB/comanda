package es.caib.comanda.monitor.logic.intf.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbOverviewDto {
    private long sessionsActives;
    private long sessionsTotal;
    private Double hitRatioCache;
    private long taulesCom;
    private long totalBytesReservats;
    private LocalDateTime ultimaActualitzacio;
    private boolean sessionsDisponibles;
    private boolean activitatDisponible;
    private boolean sqlDisponible;
    private boolean tablespacesDisponibles;
    private boolean bloqueigsDisponibles;
    private int     bloqueigsActius;
    private int     indexosInvalidsCount;
}
