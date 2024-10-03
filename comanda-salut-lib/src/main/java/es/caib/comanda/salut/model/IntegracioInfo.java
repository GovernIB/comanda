package es.caib.comanda.salut.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IntegracioInfo {
    private final String codi;
    private final String nom;


    // Custom builder
    public static class IntegracioInfoBuilder {

        public IntegracioInfoBuilder integracioApp(IntegracioApp app) {
            this.codi = app.name();
            this.nom = app.getNom();
            return this;
        }

    }
}
