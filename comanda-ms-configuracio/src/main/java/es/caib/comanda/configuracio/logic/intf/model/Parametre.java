package es.caib.comanda.configuracio.logic.intf.model;

import es.caib.comanda.client.model.ParamTipus;
import es.caib.comanda.ms.logic.intf.annotation.ResourceConfig;
import es.caib.comanda.ms.logic.intf.model.BaseResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@ResourceConfig(
        quickFilterFields = { "codi", "nom" },
        descriptionField = "nom")
public class Parametre extends BaseResource<Long> {

    @Size(max = 128)
    private String grup;
    @Size(max = 128)
    private String subGrup;
    private ParamTipus tipus;
    @NotNull
    @Size(max = 128)
    private String codi;
    @NotNull
    @Size(max = 128)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    @Size(max = 255)
    private String valor;
    private boolean editable;

    @Size(max = 128)
    private String grupI18Key;
    @Size(max = 128)
    private String subGrupI18Key;
    @Size(max = 128)
    private String nomI18Key;
    @Size(max = 128)
    private String descripcioI18Key;

}
