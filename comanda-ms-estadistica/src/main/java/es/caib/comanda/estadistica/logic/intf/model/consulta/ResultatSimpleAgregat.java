package es.caib.comanda.estadistica.logic.intf.model.consulta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultatSimpleAgregat {
    private Double doubleResult;
    private LocalDate dateResult;

    public ResultatSimpleAgregat(Double doubleResult) {
        this.doubleResult = doubleResult;
    }

    public ResultatSimpleAgregat(LocalDate dateResult) {
        this.dateResult = dateResult;
    }

    public Object getResult() {
        return doubleResult != null ? doubleResult : dateResult;
    }
}
