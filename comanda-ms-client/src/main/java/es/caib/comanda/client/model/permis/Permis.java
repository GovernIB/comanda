package es.caib.comanda.client.model.permis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Permis implements Serializable {
    private Long id;
    private Usuari usuari;
    private String grup;
    private List<String> permisos;
    private Objecte objecte;
}
