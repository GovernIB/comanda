package es.caib.comanda.client.model.permis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Objecte implements Serializable {
    private String tipus;
    private String nom;
    private String identificador;
}
