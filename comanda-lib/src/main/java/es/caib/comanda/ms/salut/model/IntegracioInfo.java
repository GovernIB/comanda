package es.caib.comanda.ms.salut.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IntegracioInfo {
    @NotNull @Size(min = 1)
    private String codi;
    @NotNull @Size(min = 1)
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
