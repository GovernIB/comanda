package es.caib.comanda.ms.salut.model;

import es.caib.comanda.ms.generic.model.ParellaCodiValor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorSalut {
    @NotNull @Size(min = 1)
    private String codi;
    @NotNull @Size(min = 1)
    private String nom;
    private String descripcio;
    private String valorGlobal;

    @Valid
    private Map<List<ParellaCodiValor>, String> dimensionsValor;
}
