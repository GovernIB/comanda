package es.caib.comanda.estadistica.persist.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Commented out as part of the MongoDB removal
/*
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumPeriode {
    private ResumPeriodeId _id;
    private int numRegistres;
    private List<IndicadorEstadistic> estadistiques;

    @Data
    public static class ResumPeriodeId {
        private PeriodeAgrupat periode;
        private Map<String, String> dimensions;
    }

    @Data
    public static class PeriodeAgrupat {
        private Integer anualitat;
        private Integer trimestre;
        private Integer mes;
        private Integer setmana;
        private LocalDate data;
    }

    @Data
    public static class IndicadorEstadistic {
        private String indicador;
        private EstadisticValues estadistiques;
    }

    @Data
    public static class EstadisticValues {
        private Double suma;
        private Double mitja;
        private Double max;
        private Double min;
    }
}
*/
