package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntegracioInfo {
    private String codi;
    private String nom;

    // Custom builder
    public static class IntegracioInfoBuilder {

        public IntegracioInfoBuilder integracioApp(IntegracioApp app) {
            this.codi = app.name();
            this.nom = app.getNom();
            return this;
        }

    }
}
