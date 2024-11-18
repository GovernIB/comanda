package es.caib.comanda.client.configuracio.model;

import es.caib.comanda.client.model.PagedSortedRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracioFiltre extends PagedSortedRequest {

    @NotNull
    private String codi;
    private String nom;
    private String versio;
    private String filtre;

}