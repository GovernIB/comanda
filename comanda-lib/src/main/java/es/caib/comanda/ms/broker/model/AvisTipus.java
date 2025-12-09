package es.caib.comanda.ms.broker.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AvisTipus", description = "Tipus d'avís publicat a COMANDA")
public enum AvisTipus {
    NOTICIA,    // Novetat a l'aplicació    - Verd suau

    INFO,       // Informació general       - Blau cel
    ALERTA,     // Advertència              - Groc suau
    ERROR,      // Error general            - Vermell suau
    CRITIC      // Error crític             - Vermell fosc
}
