package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissatgeSalut {
    @NotNull
    private Date data;
    @NotNull
    private SalutNivell nivell;
    @NotNull @Size(min = 1, max = 2048)
    private String missatge;
}
