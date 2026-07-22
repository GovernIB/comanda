package es.caib.comanda.salut.logic.service;

import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Payload de l'event SSE que s'envia quan canvia l'estat de salut d'un entorn-app.
 */
@Getter
@Builder
public class SalutChangedSsePayload implements Serializable {

    private Long entornAppId;
    private LocalDateTime data;
    private SalutEstat appEstat;
    private Integer appLatencia;
    private SalutEstat bdEstat;
    private Integer bdLatencia;
    private boolean peticioError;

    private Integer integracioUpCount;
    private Integer integracioWarnCount;
    private Integer integracioDownCount;
    private Integer integracioDesconegutCount;

    private Integer subsistemaUpCount;
    private Integer subsistemaWarnCount;
    private Integer subsistemaDownCount;
    private Integer subsistemaDesconegutCount;

    private Integer missatgeErrorCount;
    private Integer missatgeWarnCount;
    private Integer missatgeInfoCount;

    /** Sèrie temporal d'estats per a cada agrupació (clau = nom de SalutInformeAgrupacio). */
    private Map<String, List<SalutInformeEstatItem>> estatItemsPerAgrupacio;

}
