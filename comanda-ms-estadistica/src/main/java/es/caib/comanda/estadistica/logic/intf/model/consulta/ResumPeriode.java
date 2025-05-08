package es.caib.comanda.estadistica.logic.intf.model.consulta;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class ResumPeriode {
    private Periode periode;
    private Map<String, String> dimensions;
    private int numRegistres;
    private List<IndicadorEstadistic> estadistiques;

    @Data
    public static class Periode {
        private Integer any;
        private Integer trimestre;
        private Integer mes;
        private Integer setmana;
        private LocalDate data;
        private String descripcio;  // Ex: "2024-T1", "2024-03", etc.
    }

    @Data
    public static class IndicadorEstadistic {
        private String indicador;
        private Double suma;
        private Double mitja;
        private Double maxim;
        private Double minim;
    }
}
