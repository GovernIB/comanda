package es.caib.comanda.ms.estadistica.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IndicadorDesc {
    @NotNull @Size(min = 1, max = 32)
    private String codi;
    @NotNull @Size(min = 1, max = 64)
    private String nom;
    @Size(max = 1024)
    private String descripcio;
    @Valid
    private Format format;
}
