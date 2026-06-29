package es.caib.comanda.monitor.logic.intf.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TablespaceDto {
    private String nom;
    private double totalMb;
    private double maxMb;
    private double usatMb;
    private double lliureMb;
    private double pctUsat;
}
