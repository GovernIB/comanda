package es.caib.comanda.monitor.logic.intf.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaulaStorageDto {
    private String taula;
    private long numFiles;
    private long bytesReservats;
    private long bytesEstimats;
    private Date ultimaAnalisi;
}
