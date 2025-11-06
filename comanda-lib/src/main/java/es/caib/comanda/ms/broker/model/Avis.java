package es.caib.comanda.ms.broker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Avis implements Serializable {

    private String appCodi;
    private String entornCodi;

    private String identificador;
    private AvisTipus tipus;
    private String nom;
    private String descripcio;
    private Date dataInici;
    private Date dataFi;

    private URL redireccio;
    private String responsable;
    private String grup;
    private List<String> usuarisAmbPermis;
    private List<String> grupsAmbPermis;
}
