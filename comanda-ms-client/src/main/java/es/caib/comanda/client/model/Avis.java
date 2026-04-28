package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Avis implements Serializable {
    private Long id;
    private String identificador;
    private String tipus;
    private String nom;
    private String descripcio;
    private LocalDateTime dataInici;
    private LocalDateTime dataFi;
    private URL redireccio;
    private String responsable;
    private String grup;
    private String appCodi;
    private String entornCodi;
}
