package es.caib.comanda.client.model.permis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Usuari implements Serializable {
    private String codi;
    private String nom;
    private String nif;
    private String email;
}
