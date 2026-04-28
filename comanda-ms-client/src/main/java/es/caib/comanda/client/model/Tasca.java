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
public class Tasca implements Serializable {

    private Long id;
    private String appCodi;
    private String entornCodi;

    private String identificador;
    private String tipus;
    private String nom;
    private String descripcio;
    private String estat;
    private String estatDescripcio;
    private String numeroExpedient;

    private String prioritat;

    private LocalDateTime dataInici;
    private LocalDateTime dataFi;
    private LocalDateTime dataCaducitat;

    private URL url;
    private String responsable;
    private String grup;
}
