package es.caib.comanda.monitor.logic.intf.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaulaActivitatDto {
    private String taula;
    private long lecturesFisiques;
    private long lecturesLogiques;
    private long esperesBuffer;
    private long esperesFila;
}
