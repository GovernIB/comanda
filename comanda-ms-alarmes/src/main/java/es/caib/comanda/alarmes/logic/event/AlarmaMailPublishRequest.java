package es.caib.comanda.alarmes.logic.event;

import es.caib.comanda.alarmes.persist.entity.AlarmaEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AlarmaMailPublishRequest {
    private final AlarmaEntity alarma;
}
