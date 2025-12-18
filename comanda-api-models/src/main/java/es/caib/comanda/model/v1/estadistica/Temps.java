package es.caib.comanda.model.v1.estadistica;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import es.caib.comanda.model.v1.deserializer.OffsetDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.util.Calendar;
import java.util.Date;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Temps", description = "Desglossament temporal associat als registres d'estadística")
public class Temps {
    @Schema(description = "Instant temporal de referència", type = "string", format = "date-time", example = "2025-11-25T00:00:00.000Z")
    @NotNull
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime data;
    @Schema(description = "Any corresponent a la data", example = "2025")
    @Builder.ObtainVia(field = "data")
    private int anualitat;
    @Schema(description = "Trimestre (1-4) derivat de la data", example = "4")
    @Builder.ObtainVia(field = "data")
    private int trimestre;
    @Schema(description = "Mes (1-12) derivat de la data", example = "11")
    @Builder.ObtainVia(field = "data")
    private int mes;
    @Schema(description = "Setmana de l'any derivada de la data", example = "47")
    @Builder.ObtainVia(field = "data")
    private int setmana;
    @Schema(description = "Dia de la setmana", implementation = DiaSetmanaEnum.class, example = "DT")
    @Builder.ObtainVia(field = "data")
    private DiaSetmanaEnum diaSetmana;
    @Schema(description = "Dia del mes", example = "25")
    @Builder.ObtainVia(field = "data")
    private int dia;

    public Temps(Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        int mes = cal.get(Calendar.MONTH) + 1;

        this.data = data.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        this.anualitat = cal.get(Calendar.YEAR);
        this.trimestre = mes / 3;
        this.mes = mes;
        this.setmana = cal.get(Calendar.WEEK_OF_YEAR);
        this.diaSetmana = DiaSetmanaEnum.valueOf(cal.get(Calendar.DAY_OF_WEEK));
        this.dia = cal.get(Calendar.DAY_OF_MONTH);
    }

    public Temps(OffsetDateTime data) {
        this.data = data;
        this.anualitat = data.getYear();
        this.mes = data.getMonthValue();
        this.trimestre = (this.mes - 1) / 3 + 1;
        this.setmana = data.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        this.diaSetmana = DiaSetmanaEnum.valueOf(data.getDayOfWeek().ordinal() + 1);
        this.dia = data.getDayOfMonth();
    }

    // Custom builder
    public static class TempsBuilder {

        public TempsBuilder data(Date data) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(data);
            int mes = cal.get(Calendar.MONTH) + 1;

            this.data = data.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
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
