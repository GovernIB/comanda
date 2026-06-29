package es.caib.comanda.monitor.logic.intf.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BloqueigDto {
    private long   sid;
    private long   serialNum;
    private String username;
    private String status;
    private String objectName;
    private String objectType;
    private String lockMode;
    private String lockRequest;
    private boolean blocking;
}
