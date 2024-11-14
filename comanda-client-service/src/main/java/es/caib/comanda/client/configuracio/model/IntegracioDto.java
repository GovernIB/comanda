package es.caib.comanda.client.configuracio.model;

import es.caib.comanda.salut.model.IntegracioApp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntegracioDto {
    private String codi;
    private String nom;


    // Custom builder
    public static class IntegracioDtoBuilder {

        public IntegracioDtoBuilder integracioApp(IntegracioApp app) {
            this.codi = app.name();
            this.nom = app.getNom();
            return this;
        }

    }
}
