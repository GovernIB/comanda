package es.caib.comanda.salut.logic.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Esdeveniment que indica que s'ha guardat una nova informació de salut per un entorn.
 */
@Getter
@RequiredArgsConstructor
public class SalutInfoUpdatedEvent {
    private final Long entornAppId;
    private final Long salutId;
    private final int numeroDiesAgrupacio;
}
