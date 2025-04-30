package es.caib.comanda.ms.estadistica.model;

import es.caib.comanda.ms.salut.model.IntegracioApp;
import es.caib.comanda.ms.salut.model.IntegracioInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Temps {
    private LocalDate data;
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

    public Temps(LocalDate data) {
        this.data = data;
        this.anualitat = data.getYear();
        this.trimestre = data.getMonthValue() / 3;
        this.mes = data.getMonthValue();
        this.setmana = data.get(WeekFields.ISO.weekOfWeekBasedYear());
        this.diaSetmana = DiaSetmanaEnum.valueOf(data.getDayOfWeek());
        this.dia = data.getDayOfMonth();
    }

    // Custom builder
    public static class TempsBuilder {

        public Temps.TempsBuilder data(LocalDate data) {
            this.data = data;
            this.anualitat = data.getYear();
            this.trimestre = data.getMonthValue() / 3;
            this.mes = data.getMonthValue();
            this.setmana = data.get(WeekFields.ISO.weekOfWeekBasedYear());
            this.diaSetmana = DiaSetmanaEnum.valueOf(data.getDayOfWeek());
            this.dia = data.getDayOfMonth();
            return this;
        }

    }
}
