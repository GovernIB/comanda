package es.caib.comanda.ms.broker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Permis implements Serializable {

    private String appCodi;
    private String entornCodi;
    private Usuari usuari;
    private String grup;
    private List<String> permisos;
    private Objecte objecte;
    private List<Objecte> objectesHereus;

}
