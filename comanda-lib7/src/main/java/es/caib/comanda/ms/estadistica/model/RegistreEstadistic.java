package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistreEstadistic {
    @Valid
    private List<Dimensio> dimensions;
    @Valid
    private List<Fet> fets;
}
