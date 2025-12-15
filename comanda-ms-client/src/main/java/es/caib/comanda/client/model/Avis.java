package es.caib.comanda.client.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class Avis implements Serializable {
    private Long id;
    private String identificador;
    private String tipus;
    private String nom;
    private String descripcio;
    private Date dataInici;
    private Date dataFi;
    private URL redireccio;
    private String responsable;
    private String grup;
}
