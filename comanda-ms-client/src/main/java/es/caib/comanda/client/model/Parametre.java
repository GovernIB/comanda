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
public class Parametre implements Serializable {

    private String grup;
    private String subGrup;
    private ParamTipus tipus;
    private String codi;
    private String nom;
    private String descripcio;
    private String valor;
    private boolean editable;

    private String grupI18Key;
    private String subGrupI18Key;
    private String nomI18Key;
    private String descripcioI18Key;

}
