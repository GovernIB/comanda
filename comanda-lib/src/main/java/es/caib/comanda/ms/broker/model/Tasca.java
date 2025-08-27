package es.caib.comanda.ms.broker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tasca implements Serializable {

    private String appCodi;
    private String entornCodi;

    private String identificador;
    private String tipus;
    private String nom;
    private String descripcio;
    private TascaEstat estat;
    private String estatDescripcio;
    private String numeroExpedient;
    private Prioritat prioritat;
    private LocalDateTime dataInici;
    private LocalDateTime dataFi;
    private LocalDateTime dataCaducitat;
    private URL redireccio;
    private String responsable;
    private String grup;
    private List<String> usuarisAmbPermis;
    private List<String> grupsAmbPermis;

}
