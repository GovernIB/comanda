package es.caib.comanda.estadistica.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fet extends BaseResource<Long> {

    @NotNull
    private Temps temps;
    @NotNull
    private Map<String, String> dimensionsJson;
    @NotNull
    private Map<String, Double> indicadorsJson;
    @NotNull
    private Long entornAppId;
}
