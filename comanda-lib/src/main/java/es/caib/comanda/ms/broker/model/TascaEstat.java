package es.caib.comanda.ms.broker.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TascaEstat", description = "Estat de processament d'una tasca")
public enum TascaEstat {
    PENDENT,
    INICIADA,
    FINALITZADA,
    CANCELADA,
    ERROR
}
