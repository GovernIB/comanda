package es.caib.comanda.usuaris.logic.intf.model;

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
@ResourceConfig(descriptionField = "nom")
public class Usuari extends BaseResource<Long> {

    /************************************************ DATOS DEL USUARIO ***********************************************/
    @NotNull @Size(max = 64)
    private String codi;
    @NotNull @Size(max = 255)
    private String nom;
    @Size(max = 10)
    private String nif;
    @Size(max = 255)
    private String email;

    @Size(max = 200)
    private String emailAlternatiu;
    @NotNull
    private LanguageEnum idioma;
    private String[] rols;

    /************************************************ CONFIGURACIÓN GENÉRICA ******************************************/
    @NotNull
    private NumOfElementsPerPageENum numElementsPagina;
}
