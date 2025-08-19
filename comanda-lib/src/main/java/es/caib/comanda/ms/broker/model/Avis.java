package es.caib.comanda.ms.broker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Avis implements Serializable {

    private String appCodi;
    private String entornCodi;

    private Long identificador;
    private AvisTipus tipus;
    private String nom;
    private String descripcio;
    private LocalDate dataInici;
    private LocalDate dataFi;

}
