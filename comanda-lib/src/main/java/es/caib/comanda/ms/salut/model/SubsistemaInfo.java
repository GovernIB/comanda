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
public class SubsistemaInfo {
    @NotNull @Size(min = 1, max = 10)
    private String codi;
    @NotNull @Size(min = 1, max = 100)
    private String nom;
}
