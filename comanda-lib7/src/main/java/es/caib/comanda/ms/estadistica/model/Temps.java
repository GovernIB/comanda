package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Temps {
    @NotNull
    private Date data;
    @Builder.ObtainVia(field = "data")
    private int anualitat;
    @Builder.ObtainVia(field = "data")
    private int trimestre;
    @Builder.ObtainVia(field = "data")
    private int mes;
    @Builder.ObtainVia(field = "data")
    private int setmana;
    @Builder.ObtainVia(field = "data")
    private DiaSetmanaEnum diaSetmana;
    @Builder.ObtainVia(field = "data")
    private int dia;

    public Temps(Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        int mes = cal.get(Calendar.MONTH) + 1;

        this.data = data;
        this.anualitat = cal.get(Calendar.YEAR);
        this.trimestre = mes / 3;
        this.mes = mes;
        this.setmana = cal.get(Calendar.WEEK_OF_YEAR);
        this.diaSetmana = DiaSetmanaEnum.valueOf(cal.get(Calendar.DAY_OF_WEEK));
        this.dia = cal.get(Calendar.DAY_OF_MONTH);
    }

    // Custom builder
    public static class TempsBuilder {

        public Temps.TempsBuilder data(Date data) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(data);
            int mes = cal.get(Calendar.MONTH) + 1;

            this.data = data;
            this.anualitat = cal.get(Calendar.YEAR);
            this.trimestre = mes / 3;
            this.mes = mes;
            this.setmana = cal.get(Calendar.WEEK_OF_YEAR);
            this.diaSetmana = DiaSetmanaEnum.valueOf(cal.get(Calendar.DAY_OF_WEEK));
            this.dia = cal.get(Calendar.DAY_OF_MONTH);

            return this;
        }

    }

}
