package es.caib.comanda.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Usuari implements Serializable {

    private String codi;
    private String nom;
    private String nif;
    private String email;

    private boolean alarmaMail;
    private boolean alarmaMailAgrupar;

}
