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
public class Manual {
    @NotNull @Size(min = 1, max = 128)
    private String nom;
    @NotNull @Size(min = 1, max = 255)
    private String path;
}
