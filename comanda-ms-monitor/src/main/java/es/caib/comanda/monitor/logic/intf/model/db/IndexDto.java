package es.caib.comanda.monitor.logic.intf.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexDto {
    private String indexName;
    private String tableName;
    private String status;
    private String uniqueness;
    private long   numRows;
    private Date   lastAnalyzed;
    private int    blevel;
    private long   leafBlocks;
}
