package es.caib.comanda.ms.broker.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Prioritat", description = "Nivell de prioritat d'una tasca")
public enum Prioritat {
    NONE,
    BAIXA,
    NORMAL,
    ALTA,
    MAXIMA
}
