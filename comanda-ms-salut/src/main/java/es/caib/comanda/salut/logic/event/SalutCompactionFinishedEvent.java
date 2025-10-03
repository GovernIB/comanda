package es.caib.comanda.salut.logic.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Esdeveniment que indica que s'ha finalitzat l'agrupació de salut.
 */
@Getter
@RequiredArgsConstructor
public class SalutCompactionFinishedEvent {
    private final Long entornAppId;
    private final Long salutId;
}
