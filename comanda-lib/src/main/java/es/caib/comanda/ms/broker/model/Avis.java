package es.caib.comanda.ms.broker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

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

}
