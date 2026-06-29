package es.caib.comanda.monitor.logic.intf.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSqlDto {
    private String sqlId;
    private double tempsTotalS;
    private long execucions;
    private double msPerExec;
    private long buffersPerExec;
    private String sqlText;
}
