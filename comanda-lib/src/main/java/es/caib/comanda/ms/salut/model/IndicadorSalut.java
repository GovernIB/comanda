package es.caib.comanda.ms.salut.model;

import es.caib.comanda.ms.generic.model.ParellaCodiValor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorSalut {
    private String codi;
    private String nom;
    private String descripcio;
    private String valorGlobal;

    private Map<List<ParellaCodiValor>, String> dimensionsValor;
}
