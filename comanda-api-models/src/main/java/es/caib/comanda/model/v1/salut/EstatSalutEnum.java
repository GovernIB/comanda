package es.caib.comanda.model.v1.salut;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EstatSalutEnum", description = "Enumerat d'estats possibles de salut d'un component")
public enum EstatSalutEnum {
    UP,
    WARN,
    DEGRADED,
    DOWN,
    MAINTENANCE,
    UNKNOWN,
    ERROR
}
