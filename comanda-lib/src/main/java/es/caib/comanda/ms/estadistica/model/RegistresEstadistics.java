package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistresEstadistics {
    private Temps temps;
//    private List<Dimensio> dimensionsDescripcio;
    private List<RegistreEstadistic> fets;
}
