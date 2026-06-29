package es.caib.comanda.monitor.logic.intf.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionsResumDto {
    private String estat;
    private long quantitat;
}
