package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Temps extends BaseResource<Long> {

    @NotNull
    private LocalDate data;
    @NotNull
    private int anualitat;
    @NotNull
    private int trimestre;
    @NotNull
    private int mes;
    @NotNull
    private int setmana;
    @NotNull
    private DiaSetmanaEnum diaSetmana;
    @NotNull
    private int dia;

}
