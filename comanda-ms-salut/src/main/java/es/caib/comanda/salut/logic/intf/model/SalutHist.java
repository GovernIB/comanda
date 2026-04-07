package es.caib.comanda.salut.logic.intf.model;

import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Històric de canvis d'estat de salut d'un entorn d'aplicació.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalutHist extends BaseResource<Long> {

    @NotNull
    private Long entornAppId;
    @NotNull
    private LocalDateTime data;
    @NotNull
    private SalutEstat appEstat;
    private boolean peticioError;
}
